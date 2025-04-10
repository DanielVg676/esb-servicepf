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
import com.utd.ti.soa.esb_service.model.CreateOrderRequest;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.util.logging.Logger;

@RestController
@RequestMapping("/app/esb")
public class ESBController {

    private final WebClient webClient;
    private final Auth auth;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(ESBController.class.getName());

    public ESBController(Auth auth) throws SSLException {
        this.auth = auth;

        // Configurar SSL para ignorar verificación de certificados (solo desarrollo)
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE) // Ignora validación SSL
                .protocols("TLSv1.2", "TLSv1.3") // Soporte para TLSv1.2 y TLSv1.3
                .build();

        HttpClient httpClient = HttpClient.create()
                .secure(t -> t.sslContext(sslContext))
                .doOnRequest((req, conn) -> logger.info("Realizando solicitud a: " + req.resourceUrl()))
                .doOnError((req, err) -> logger.severe("Error en solicitud a " + req.resourceUrl() + ": " + err.getMessage()));

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
            logger.info("Iniciando creación de usuario con username: " + user.getUsername());
            if (auth.validateToken(token) == null) {
                logger.warning("Token inválido o expirado");
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
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al crear usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al crear usuario: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<String> getUsers(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Obteniendo lista de usuarios");
            if (!auth.hasRole(token, "admin")) {
                logger.warning("Acceso denegado: Se requiere rol de administrador");
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
            }
            String response = webClient.get()
                    .uri("https://userspf-production.up.railway.app/users/getUsers")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al obtener usuarios: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @PatchMapping("/users/update/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable Long id,
            @RequestBody User user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Actualizando usuario con ID: " + id);
            if (!auth.hasRole(token, "admin")) {
                logger.warning("Acceso denegado: Se requiere rol de administrador");
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
            }
            String response = webClient.patch()
                    .uri("https://userspf-production.up.railway.app/users/update/" + id)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(user)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al actualizar usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al actualizar usuario: " + e.getMessage());
        }
    }

    @PatchMapping("/users/delete/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Eliminando usuario con ID: " + id);
            if (!auth.hasRole(token, "admin")) {
                logger.warning("Acceso denegado: Se requiere rol de administrador");
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
            }
            String response = webClient.patch()
                    .uri("https://userspf-production.up.railway.app/users/deleteUser/" + id)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al eliminar usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al eliminar usuario: " + e.getMessage());
        }
    }

    @PostMapping("/users/login")
    public ResponseEntity<String> login(
            @RequestBody User user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Iniciando sesión para usuario: " + user.getUsername());
            String response = webClient.post()
                    .uri("https://userspf-production.up.railway.app/users/login")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(user)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al iniciar sesión: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al iniciar sesión: " + e.getMessage());
        }
    }

    // ---------- CLIENTS ----------

    @PostMapping("/clients")
    public ResponseEntity<String> createClient(
            @RequestBody Client client,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Creando cliente");
            if (auth.validateToken(token) == null) {
                logger.warning("Token inválido o expirado");
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
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al crear cliente: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al crear cliente: " + e.getMessage());
        }
    }

    @GetMapping("/clients")
    public ResponseEntity<String> getAllClients(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Obteniendo lista de clientes");
            if (!auth.hasRole(token, "admin")) {
                logger.warning("Acceso denegado: Se requiere rol de administrador");
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
            }
            List<Client> clients = webClient.get()
                    .uri("https://clientspf-production.up.railway.app/api/clients/all")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Client>>() {})
                    .block();
            String response = objectMapper.writeValueAsString(clients);
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al obtener clientes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<String> getClientById(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Obteniendo cliente con ID: " + id);
            if (!auth.hasRole(token, "admin")) {
                logger.warning("Acceso denegado: Se requiere rol de administrador");
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
            }
            String response = webClient.get()
                    .uri("https://clientspf-production.up.railway.app/clients/getClientid/" + id)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al obtener cliente: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al obtener cliente: " + e.getMessage());
        }
    }

    @PutMapping("/clients/update/{id}")
    public ResponseEntity<String> updateClient(
            @PathVariable Long id,
            @RequestBody Client client,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Actualizando cliente con ID: " + id);
            if (!auth.hasRole(token, "admin")) {
                logger.warning("Acceso denegado: Se requiere rol de administrador");
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
            }
            String response = webClient.put()
                    .uri("https://clientspf-production.up.railway.app/clients/updateClient/" + id)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(client)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al actualizar cliente: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al actualizar cliente: " + e.getMessage());
        }
    }

    @PatchMapping("/clients/delete/{id}")
    public ResponseEntity<String> deleteClient(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Eliminando cliente con ID: " + id);
            if (!auth.hasRole(token, "admin")) {
                logger.warning("Acceso denegado: Se requiere rol de administrador");
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
            }
            String response = webClient.patch()
                    .uri("https://clientspf-production.up.railway.app/clients/deleteClient/" + id)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al eliminar cliente: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al eliminar cliente: " + e.getMessage());
        }
    }

    // ---------- PRODUCTS ----------

    @PostMapping("/products")
    public ResponseEntity<String> createProduct(
            @RequestBody Product product,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Creando producto");
            if (!auth.hasRole(token, "admin", "seller")) {
                logger.warning("Acceso denegado: Se requiere rol de admin o seller");
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de admin o seller");
            }
            String response = webClient.post()
                    .uri("https://productspf-production.up.railway.app/products/newProduct")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(product)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al crear producto: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al crear producto: " + e.getMessage());
        }
    }

    @GetMapping("/products")
    public ResponseEntity<String> getAllProducts(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Obteniendo lista de productos");
            if (auth.validateToken(token) == null) {
                logger.warning("Token inválido o expirado");
                return ResponseEntity.status(401).body("Token inválido o expirado");
            }
            List<Product> products = webClient.get()
                    .uri("https://productspf-production.up.railway.app/products/allProducts")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Product>>() {})
                    .block();
            String response = objectMapper.writeValueAsString(products);
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al obtener productos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al obtener productos: " + e.getMessage());
        }
    }

    @PatchMapping("/products/update/{id}")
    public ResponseEntity<String> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Actualizando producto con ID: " + id);
            if (!auth.hasRole(token, "admin", "seller")) {
                logger.warning("Acceso denegado: Se requiere rol de admin o seller");
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de admin o seller");
            }
            String response = webClient.patch()
                    .uri("https://productspf-production.up.railway.app/products/updateProduct/" + id)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(product)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al actualizar producto: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al actualizar producto: " + e.getMessage());
        }
    }

    @PatchMapping("/products/delete/{id}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Eliminando producto con ID: " + id);
            if (!auth.hasRole(token, "admin")) {
                logger.warning("Acceso denegado: Se requiere rol de administrador");
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol de administrador");
            }
            String response = webClient.patch()
                    .uri("https://productspf-production.up.railway.app/products/deleteProduct/" + id)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al eliminar producto: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al dar de baja el producto: " + e.getMessage());
        }
    }

    // ---------- PAYMENTS ----------

    @PostMapping("/payments/create-order")
    public ResponseEntity<String> createOrder(
            @RequestBody CreateOrderRequest orderRequest,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            logger.info("Creando orden de pago");
            if (auth.validateToken(token) == null) {
                logger.warning("Token inválido o expirado");
                return ResponseEntity.status(401).body("Token inválido o expirado");
            }
            String response = webClient.post()
                    .uri("https://payment-production-bec3.up.railway.app/api/payments/create-order")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(orderRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            logger.info("Respuesta recibida: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error al crear la orden: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno al crear la orden: " + e.getMessage());
        }
    }
}
