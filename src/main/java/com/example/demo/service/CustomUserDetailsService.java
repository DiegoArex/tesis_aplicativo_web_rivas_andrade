package com.example.demo.service;

import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Servicio para cargar usuarios desde la base de datos para autenticación Form Login
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar usuario por email o username
        Usuario usuario = usuarioRepository.findByEmail(username)
                .or(() -> usuarioRepository.findByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Verificar si está activo
        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        // Obtener roles del usuario (desde campo roles)
        var authorities = new java.util.ArrayList<org.springframework.security.core.GrantedAuthority>();
        if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
            String[] roles = usuario.getRoles().split(",");
            for (String role : roles) {
                String trimmedRole = role.trim();
                // Si el rol ya tiene ROLE_ no lo agregamos de nuevo
                if (!trimmedRole.startsWith("ROLE_")) {
                    trimmedRole = "ROLE_" + trimmedRole;
                }
                authorities.add(new SimpleGrantedAuthority(trimmedRole));
            }
        } else {
            // Si no tiene roles, dar ROLE_USUARIO por defecto
            authorities.add(new SimpleGrantedAuthority("ROLE_USUARIO"));
        }
        
        // Contraseña = mismo username (profesor1/profesor1, profesor2/profesor2, etc.)
        return User.builder()
                .username(usuario.getUsername())
                .password("{noop}" + usuario.getUsername()) // {noop} = sin encriptar, password = username
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.getActivo())
                .build();
    }
}
