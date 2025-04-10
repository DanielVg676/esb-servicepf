package com.utd.ti.soa.esb_service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class Auth {
    private final Map<String, String> SECRET_KEYS = new HashMap<>();

    public Auth() {
        // Usa las mismas claves largas que en Node.js
        SECRET_KEYS.put("admin", System.getenv("ADMIN_SECRET_KEY") != null ? 
            System.getenv("ADMIN_SECRET_KEY") : "adminSecretKey12345678901234567890123456789012");
        SECRET_KEYS.put("customer", System.getenv("CUSTOMER_SECRET_KEY") != null ? 
            System.getenv("CUSTOMER_SECRET_KEY") : "customerSecretKey1234567890123456789012345678");
        SECRET_KEYS.put("seller", System.getenv("SELLER_SECRET_KEY") != null ? 
            System.getenv("SELLER_SECRET_KEY") : "sellerSecretKey123456789012345678901234567890");
    }

    public Claims validateToken(String token) {
        try {
            String processedToken = token.startsWith("Bearer ") ? token.substring(7) : token;

            for (Map.Entry<String, String> entry : SECRET_KEYS.entrySet()) {
                String role = entry.getKey();
                String secretKey = entry.getValue();

                try {
                    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
                    Jws<Claims> jws = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(processedToken);

                    Claims claims = jws.getBody();
                    String tokenRole = claims.get("rol", String.class);

                    if (role.equals(tokenRole)) {
                        System.out.println("Token válido para rol: " + role + ", usuario: " + claims.getSubject());
                        return claims;
                    }
                } catch (Exception e) {
                    System.out.println("Fallo al validar con rol " + role + ": " + e.getMessage());
                    continue;
                }
            }
            System.out.println("No se encontró una clave válida para el token");
            return null;
        } catch (Exception e) {
            System.out.println("Error al validar el token: " + e.getMessage());
            return null;
        }
    }

    public boolean hasRole(String token, String... allowedRoles) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return false;
        }

        String role = claims.get("rol", String.class);
        for (String allowedRole : allowedRoles) {
            if (allowedRole.equals(role)) {
                return true;
            }
        }
        return false;
    }
}