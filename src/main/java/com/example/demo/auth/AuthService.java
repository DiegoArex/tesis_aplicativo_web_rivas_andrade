package com.example.demo.auth;

import com.example.demo.dto.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${keycloak.token-url:https://fing-auth.ideasybits.com/auth/realms/siac-realm-test/protocol/openid-connect/token}")
    private String keycloakTokenUrl;

    @Value("${keycloak.client-id:siac-app-movil}")
    private String keycloakClientId;

    @Value("${keycloak.client-secret:}")
    private String keycloakClientSecret;

    public ResponseEntity<?> login(String username, String password) {

        logger.info("=".repeat(80));
        logger.info("🔐 INTENTO DE LOGIN");
        logger.info("Usuario: {}", username);
        logger.info("=".repeat(80));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", keycloakClientId);
        if (StringUtils.hasText(keycloakClientSecret)) {
            body.add("client_secret", keycloakClientSecret);
        }
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.exchange(
                    keycloakTokenUrl,
                    HttpMethod.POST,
                    request,
                    TokenResponse.class
            );

            TokenResponse tokenResponse = response.getBody();

            if (tokenResponse == null) {
                logger.error("❌ Keycloak no devolvió un token válido");
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("Keycloak no devolvió un token válido");
            }

            logger.info("✅ LOGIN EXITOSO para usuario: {}", username);
            logger.info("Token Type: {} | Expires In: {} segundos",
                    tokenResponse.getToken_type(), tokenResponse.getExpires_in());
                logger.info("Refresh Token Expires In: {} segundos", tokenResponse.getRefresh_expires_in());
            // El contenido del token no se registra en logs por seguridad
            logger.info("=".repeat(80));

            return ResponseEntity.status(response.getStatusCode()).body(tokenResponse);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            logger.error("❌ ERROR DE AUTENTICACIÓN");
            logger.error("Status Code: {}", ex.getStatusCode());
            logger.error("Response Body: {}", ex.getResponseBodyAsString());
            logger.info("=".repeat(80));
            return ResponseEntity.status(ex.getStatusCode())
                    .body(ex.getResponseBodyAsString());
        }
    }
}