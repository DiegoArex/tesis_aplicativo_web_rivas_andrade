package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * SecurityFilterChain para Thymeleaf (con sesiones)
     * Rutas que renderizan vistas HTML
     */
    @Bean
    @Order(1)
    SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
                                .securityMatcher("/web/**", "/login", "/logout", "/", "/index.html", "/css/**", "/js/**", "/images/**")
                                .csrf(csrf -> csrf.ignoringRequestMatchers("/logout", "/login"))
                .authorizeHttpRequests(auth -> auth
                        // === Recursos públicos ===
                        .requestMatchers("/", "/index.html", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                        
                        // === Gestión de Usuarios (Solo ADMIN) ===
                        .requestMatchers("/web/usuarios/**").hasRole("ADMIN")
                        
                        // === Gestión de Laboratorios (Solo ADMIN para crear/editar/eliminar) ===
                        .requestMatchers("/web/laboratorios/nuevo", "/web/laboratorios/*/editar", "/web/laboratorios/*/eliminar")
                        .hasRole("ADMIN")
                        .requestMatchers("/web/laboratorios/**").authenticated()
                        
                        // === Gestión de Equipos (Solo ADMIN para crear/editar/eliminar) ===
                        .requestMatchers("/web/equipos/nuevo", "/web/equipos/*/editar", "/web/equipos/*/eliminar")
                        .hasRole("ADMIN")
                        .requestMatchers("/web/equipos/**").authenticated()
                        
                        // === Registros de Uso (Profesores y superiores) ===
                        .requestMatchers("/web/registros/entrada", "/web/registros/salida", "/web/registros/mis-registros")
                        .hasAnyRole("PROFESOR", "FACULTAD_DIRECTOR_CARRERA", "ADMIN")
                        .requestMatchers("/web/registros/**").hasAnyRole("PROFESOR", "FACULTAD_DIRECTOR_CARRERA", "ADMIN")
                        
                        // === Novedades (Todos pueden reportar, solo ADMIN cambia estado) ===
                        .requestMatchers("/web/novedades/nueva").hasAnyRole("USUARIO", "PROFESOR", "FACULTAD_DIRECTOR_CARRERA", "ADMIN")
                        .requestMatchers("/web/novedades/*/estado").hasRole("ADMIN")
                        .requestMatchers("/web/novedades/**").authenticated()
                        
                        // === Auditoría (Directores y ADMIN) ===
                        .requestMatchers("/web/auditoria/**").hasAnyRole("FACULTAD_DIRECTOR_CARRERA", "ADMIN")
                        
                        // === Dashboard y otras rutas web ===
                        .requestMatchers("/web/**").authenticated()
                        
                        .anyRequest().authenticated())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1));

        return http.build();
    }

    /**
     * SecurityFilterChain para API REST (con JWT) - Mantener para compatibilidad
     */
    @Bean
    @Order(2)
    SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

        http
                // Incluye endpoints REST de prueba fuera de /api para que también usen JWT.
                .securityMatcher("/api/**", "/public", "/admin", "/profesor", "/notas", "/me")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

                        // === Autenticación pública ===
                        .requestMatchers("/api/auth/**").permitAll()

                        // === Endpoints de prueba (mantener para testing) ===
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/profesor").hasRole("PROFESOR")
                        .requestMatchers("/notas").hasAnyRole("PROFESOR", "FACULTAD_DIRECTOR_CARRERA")
                        .requestMatchers("/me").authenticated()

                        // === API Laboratorios ===
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/laboratorios/**")
                        .authenticated()
                        .requestMatchers("/api/laboratorios/**").hasRole("ADMIN")

                        // === API Equipos ===
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/equipos/**").authenticated()
                        .requestMatchers("/api/equipos/**").hasRole("ADMIN")

                        // === API Registros de Uso ===
                        .requestMatchers("/api/registros/entrada", "/api/registros/salida",
                                "/api/registros/mis-registros")
                        .hasAnyRole("PROFESOR", "FACULTAD_DIRECTOR_CARRERA", "ADMIN")
                        .requestMatchers("/api/registros/activos").hasRole("ADMIN")
                        .requestMatchers("/api/registros/**").hasAnyRole("ADMIN", "FACULTAD_DIRECTOR_CARRERA")

                        // === API Novedades ===
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/novedades")
                        .hasAnyRole("PROFESOR", "FACULTAD_DIRECTOR_CARRERA", "ADMIN", "USUARIO")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/novedades/*/estado")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/novedades/**").authenticated()

                        // === API Usuarios ===
                        .requestMatchers("/api/usuarios/me").authenticated()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // === API Auditoría ===
                        .requestMatchers("/api/audit/**").hasAnyRole("ADMIN", "FACULTAD_DIRECTOR_CARRERA")
                        .requestMatchers("/api/auditoria/**").hasAnyRole("ADMIN", "FACULTAD_DIRECTOR_CARRERA")

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(apiBearerTokenResolver())
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

        @Bean
        BearerTokenResolver apiBearerTokenResolver() {
                DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
                return request -> {
                        String path = request.getRequestURI();
                        if (path != null && path.startsWith("/api/auth/")) {
                                return null;
                        }
                        return delegate.resolve(request);
                };
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(List.of("*"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
                configuration.setAllowCredentials(false);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/api/**", configuration);
                return source;
        }

    /**
     * Convierte los claims del JWT de Keycloak a GrantedAuthorities de Spring
     * Security
     * Extrae roles desde realm_access.roles y filtra solo los roles reales (ignora
     * offline_access, default-roles-*, etc)
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            log.debug("Convirtiendo JWT - subject: {}", jwt.getSubject());

            List<GrantedAuthority> authorities = new ArrayList<>();

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                log.debug("Roles brutos de realm_access: {}", roles);

                authorities = roles.stream()
                        .filter(this::shouldIncludeRole)
                        .map(role -> {
                            String normalizedRole = role.toUpperCase().startsWith("ROLE_")
                                    ? role.toUpperCase()
                                    : "ROLE_" + role.toUpperCase();
                            return (GrantedAuthority) new SimpleGrantedAuthority(normalizedRole);
                        })
                        .collect(Collectors.toList());
            } else {
                log.warn("No se encontró 'realm_access.roles' en el JWT para subject: {}", jwt.getSubject());
            }

            log.debug("Authorities asignadas: {}", authorities);
            return authorities;
        });

        return converter;
    }

    /**
     * Filtra roles internos de Keycloak que no deberían ser autoridades de
     * aplicación
     */
    private boolean shouldIncludeRole(String role) {
        // Ignorar roles internos de Keycloak
        return !role.equals("offline_access")
                && !role.startsWith("default-roles-")
                && !role.equals("uma_authorization")
                && !role.equals("account")
                && !role.isEmpty();
    }
}
