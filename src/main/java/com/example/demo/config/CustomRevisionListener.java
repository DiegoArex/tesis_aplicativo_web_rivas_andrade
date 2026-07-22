package com.example.demo.config;

import com.example.demo.entity.EntidadRevision;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.UUID;

/**
 * Listener personalizado para capturar información del usuario en cada revisión
 */
public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        EntidadRevision customRevision = (EntidadRevision) revisionEntity;

        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = resolveUsername(authentication);
        if (username != null) {
            customRevision.setUsername(username);
        } else {
            customRevision.setUsername("SYSTEM");
        }

        // Obtener la IP del request
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                String ipAddress = attributes.getRequest().getRemoteAddr();
                customRevision.setIpAddress(ipAddress);
            } else {
                customRevision.setIpAddress("UNKNOWN");
            }
        } catch (Exception e) {
            customRevision.setIpAddress("UNKNOWN");
        }
    }

    private String resolveUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String authenticationName = authentication.getName();
        if (authenticationName == null || authenticationName.isBlank() || "anonymousUser".equals(authenticationName)) {
            return null;
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return resolveUsernameFromJwt(jwtAuthenticationToken.getToken(), authenticationName);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return resolveUsernameFromJwt(jwt, authenticationName);
        }

        return authenticationName;
    }

    private String resolveUsernameFromJwt(Jwt jwt, String fallbackUsername) {
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || username.isBlank()) {
            username = jwt.getClaimAsString("name");
        }
        if (username == null || username.isBlank()) {
            username = jwt.getSubject();
        }

        String resolvedFromDatabase = resolveUsernameFromKeycloakId(username);
        if (resolvedFromDatabase != null) {
            return resolvedFromDatabase;
        }

        return (username == null || username.isBlank()) ? fallbackUsername : username;
    }

    private String resolveUsernameFromKeycloakId(String candidate) {
        if (candidate == null || candidate.isBlank() || !looksLikeUuid(candidate)) {
            return null;
        }

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }

            WebApplicationContext applicationContext = WebApplicationContextUtils
                    .getRequiredWebApplicationContext(attributes.getRequest().getServletContext());
            UsuarioRepository usuarioRepository = applicationContext.getBean(UsuarioRepository.class);

            return usuarioRepository.findByKeycloakId(candidate)
                    .map(Usuario::getUsername)
                    .filter(username -> username != null && !username.isBlank())
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean looksLikeUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
