package com.utd.ti.soa.esb_service.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import java.util.List;

import com.utd.ti.soa.esb_service.utils.Auth;
import com.utd.ti.soa.esb_service.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utd.ti.soa.esb_service.model.Client;
import com.utd.ti.soa.esb_service.model.Product;
import com.utd.ti.soa_esb_service.model.CreateOrderRequest;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@RestController
@RequestMapping("/app/esb")
public class ESBController {

    private final WebClient webClient; // Solo declaramos el campo
    private final Auth auth;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ESBController(Auth auth) throws SSLException { // Agregamos throws SSLException
        this.auth = auth;

        // Configurar SSL para ignorar verificación de certificados (solo desarrollo)
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE) // Ignora validación SSL
                .protocols("TLSv1.2", "TLSv1.3")
                .build();

        HttpClient httpClient = HttpClient.create()
                .secure(t -> t.sslContext(sslContext));

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // ---------- USERS ----------

    @PostMapping("/users")
    public ResponseEntity<String> createUser(
            @RequestBody User user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            if (auth.validateToken(token) == null) {
                return ResponseEntity.status(401).body("Token inválido o expirado");
            }
            String response = webClient.post()
                    .uri("https://userspf-production.up.railway.app/users/newUser")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(user)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al crear usuario: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<String> getUsers(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (!auth.hasRole(token, "admin")) {
            return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
        }
        try {
            String response = webClient.get()
                    .uri("https://userspf-production.up.railway.app/users/getUsers")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @PatchMapping("/users/update/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable Long id,
            @RequestBody User user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (!auth.hasRole(token, "admin")) {
            return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
        }
        try {
            String response = webClient.patch()
                    .uri("https://userspf-production.up.railway.app/users/update/" + id)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(user)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al actualizar usuario: " + e.getMessage());
        }
    }

    @PatchMapping("/users/delete/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (!auth.hasRole(token, "admin")) {
            return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
        }
        try {
            String response = webClient.patch()
                    .uri("https://userspf-production.up.railway.app/users/deleteUser/" + id)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al eliminar usuario: " + e.getMessage());
        }
    }

    @PostMapping("/users/login")
    public ResponseEntity<String> login(
            @RequestBody User user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            String response = webClient.post()
                    .uri("https://userspf-production.up.railway.app/users/login")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(user)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al iniciar sesión: " + e.getMessage());
        }
    }

    // ---------- CLIENTS ----------

    @PostMapping("/clients")
    public ResponseEntity<String> createClient(
            @RequestBody Client client,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            if (auth.validateToken(token) == null) {
                return ResponseEntity.status(401).body("Token inválido o expirado");
            }
            String response = webClient.post()
                    .uri("https://clientspf-production.up.railway.app/api/clients/new")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(client)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al crear cliente: " + e.getMessage());
        }
    }

    @GetMapping("/clients")
    public ResponseEntity<String> getAllClients(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (!auth.hasRole(token, "admin")) {
            return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
        }
        try {
            List<Client> clients = webClient.get()
                    .uri("https://clientspf-production.up.railway.app/api/clients/all")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Client>>() {})
                    .block();
            return ResponseEntity.ok(objectMapper.writeValueAsString(clients));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<String> getClientById(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (!auth.hasRole(token, "admin")) {
            return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
        }
        try {
            String response = webClient.get()
                    .uri("https://clientspf-production.up.railway.app/clients/getClientid/" + id)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al obtener cliente: " + e.getMessage());
        }
    }

    @PutMapping("/clients/update/{id}")
    public ResponseEntity<String> updateClient(
            @PathVariable Long id,
            @RequestBody Client client,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (!auth.hasRole(token, "admin")) {
            return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
        }
        try {
            String response = webClient.put()
                    .uri("https://clientspf-production.up.railway.app/clients/updateClient/" + id)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(client)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al actualizar cliente: " + e.getMessage());
        }
    }

    @PatchMapping("/clients/delete/{id}")
    public ResponseEntity<String> deleteClient(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (!auth.hasRole(token, "admin")) {
            return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
        }
        try {
            String response = webClient.patch()
                    .uri("https://clientspf-production.up.railway.app/clients/deleteClient/" + id)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al eliminar cliente: " + e.getMessage());
        }
    }

    // ---------- PRODUCTS ----------

    @PostMapping("/products")
    public ResponseEntity<String> createProduct(
            @RequestBody Product product,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (!auth.hasRole(token, "admin", "seller")) {
            return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de admin o seller");
        }
        try {
            String response = webClient.post()
                    .uri("https://productspf-production.up.railway.app/products/newProduct")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(product)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al crear producto: " + e.getMessage());
        }
    }

    @GetMapping("/products")
    public ResponseEntity<String> getAllProducts(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (auth.validateToken(token) == null) {
            return ResponseEntity.status(401).body("Token inválido o expirado");
        }
        try {
            List<Product> products = webClient.get()
                    .uri("https://productspf-production.up.railway.app/products/allProducts")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Product>>() {})
                    .block();
            return ResponseEntity.ok(objectMapper.writeValueAsString(products));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al obtener productos: " + e.getMessage());
        }
    }

    @PatchMapping("/products/update/{id}")
    public ResponseEntity<String> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (!auth.hasRole(token, "admin", "seller")) {
            return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de admin o seller");
        }
        try {
            String response = webClient.patch()
                    .uri("https://productspf-production.up.railway.app/products/updateProduct/" + id)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(product)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al actualizar producto: " + e.getMessage());
        }
    }

    @PatchMapping("/products/delete/{id}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (!auth.hasRole(token, "admin")) {
            return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
        }
        try {
            String response = webClient.patch()
                    .uri("https://productspf-production.up.railway.app/products/deleteProduct/" + id)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al dar de baja el producto: " + e.getMessage());
        }
    }

    // ---------- PAYMENTS ----------

    @PostMapping("/payments/create-order")
    public ResponseEntity<String> createOrder(
            @RequestBody CreateOrderRequest orderRequest,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (auth.validateToken(token) == null) {
            return ResponseEntity.status(401).body("Token inválido o expirado");
        }
        try {
            String response = webClient.post()
                    .uri("https://payment-production-bec3.up.railway.app/api/payments/create-order")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(orderRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al crear la orden: " + e.getMessage());
        }
    }
}
