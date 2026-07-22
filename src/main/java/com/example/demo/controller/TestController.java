package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/public")
    public String publico() {
        return "Endpoint público - No requiere autenticación";
    }

    @GetMapping("/admin")
    public Map<String, Object> admin(@AuthenticationPrincipal Jwt jwt) {
        return getUserInfo(jwt, "ADMIN");
    }

    @GetMapping("/profesor")
    public Map<String, Object> profesor(@AuthenticationPrincipal Jwt jwt) {
        return getUserInfo(jwt, "PROFESOR");
    }

    @GetMapping("/notas")
    public Map<String, Object> notas(@AuthenticationPrincipal Jwt jwt) {
        return getUserInfo(jwt, "NOTAS (PROFESOR o FACULTAD_DIRECTOR_CARRERA)");
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> info = new HashMap<>();
        info.put("username", jwt.getClaim("preferred_username"));
        info.put("email", jwt.getClaim("email"));
        info.put("nombre", jwt.getClaim("name"));
        info.put("roles", jwt.getClaim("realm_access"));
        info.put("subject", jwt.getSubject());
        info.put("allClaims", jwt.getClaims());
        return info;
    }

    private Map<String, Object> getUserInfo(Jwt jwt, String endpoint) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Acceso exitoso a " + endpoint);
        response.put("usuario", jwt.getClaim("preferred_username"));
        response.put("roles", jwt.getClaim("realm_access"));
        return response;
    }
}

@Controller
class CallbackController {
    @GetMapping("/callback")
    public String callback() {
        // Keycloak redirige aquí después de autenticación
        // El JavaScript en index.html maneja el code
        return "forward:/index.html";
    }
}
