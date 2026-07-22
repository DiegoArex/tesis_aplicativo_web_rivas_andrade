package com.example.demo.controller;

import com.example.demo.auth.AuthService;
import com.example.demo.dto.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controlador principal para páginas web
 */
@Controller
@RequiredArgsConstructor
public class WebHomeController {

    private final AuthService authService;
    private final JwtDecoder jwtDecoder;

    @GetMapping("/")
    public String home() {
        return "redirect:/web/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        try {
            var keycloakResponse = authService.login(username, password);
            if (!keycloakResponse.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addAttribute("error", true);
                return "redirect:/login";
            }

            if (!(keycloakResponse.getBody() instanceof TokenResponse tokenResponse)
                    || tokenResponse.getAccess_token() == null || tokenResponse.getAccess_token().isBlank()) {
                redirectAttributes.addAttribute("error", true);
                return "redirect:/login";
            }

            Jwt jwt = jwtDecoder.decode(tokenResponse.getAccess_token());
            Collection<? extends GrantedAuthority> authorities = mapKeycloakAuthorities(jwt.getClaims());
            String principalName = jwt.getClaimAsString("preferred_username");
            if (principalName == null || principalName.isBlank()) {
                principalName = jwt.getSubject();
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principalName, null, authorities);

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            new HttpSessionSecurityContextRepository().saveContext(securityContext, request, response);

            return "redirect:/web/dashboard";
        } catch (JwtException ex) {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/login";
        }
    }

    @GetMapping("/web/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("titulo", "Dashboard");
        return "dashboard";
    }

    private Collection<? extends GrantedAuthority> mapKeycloakAuthorities(Map<String, Object> claims) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        extractRolesFromClaim(claims.get("realm_access"), authorities);

        Object resourceAccess = claims.get("resource_access");
        if (resourceAccess instanceof Map<?, ?> resourceAccessMap) {
            for (Object clientAccess : resourceAccessMap.values()) {
                extractRolesFromClaim(clientAccess, authorities);
            }
        }

        return authorities;
    }

    private void extractRolesFromClaim(Object claimValue, Set<GrantedAuthority> authorities) {
        if (!(claimValue instanceof Map<?, ?> claimMap)) {
            return;
        }

        Object rolesValue = claimMap.get("roles");
        if (!(rolesValue instanceof Iterable<?> roles)) {
            return;
        }

        for (Object roleObject : roles) {
            if (!(roleObject instanceof String role)) {
                continue;
            }

            String normalizedRole = role.trim();
            if (!shouldIncludeRole(normalizedRole)) {
                continue;
            }

            if (!normalizedRole.toUpperCase().startsWith("ROLE_")) {
                normalizedRole = "ROLE_" + normalizedRole.toUpperCase();
            } else {
                normalizedRole = normalizedRole.toUpperCase();
            }

            authorities.add(new SimpleGrantedAuthority(normalizedRole));
        }
    }

    private boolean shouldIncludeRole(String role) {
        return role != null
                && !role.isBlank()
                && !role.equals("offline_access")
                && !role.startsWith("default-roles-")
                && !role.equals("uma_authorization")
                && !role.equals("account");
    }
}
