package com.example.demo.config;

import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Configuración para inicializar usuarios de prueba
 * Credenciales: profesor1/profesor1, profesor2/profesor2, profesor3/profesor3
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;

    // @Bean
    // Deshabilitado: Los usuarios se crean manualmente según sea necesario
    /*
    CommandLineRunner initUsuarios() {
        return args -> {
            if (usuarioRepository.findByUsername("profesor1").isEmpty()) {
                log.info("Creando usuarios de prueba...");

                // profesor1 - ROLE_ADMIN
                Usuario prof1 = new Usuario();
                prof1.setNombreCompleto("Profesor Uno");
                prof1.setUsername("profesor1");
                prof1.setKeycloakId("local-" + UUID.randomUUID());
                prof1.setEmail("profesor1@fing.edu.uy");
                prof1.setCarrera("INGENIERIA_SISTEMAS");
                prof1.setFacultad("FING");
                prof1.setRoles("ADMIN");
                prof1.setActivo(true);
                prof1.setCreatedAt(LocalDateTime.now());
                prof1.setUpdatedAt(LocalDateTime.now());
                usuarioRepository.save(prof1);

                // profesor2 - ROLE_PROFESOR, ROLE_FACULTAD_DIRECTOR_CARRERA
                Usuario prof2 = new Usuario();
                prof2.setNombreCompleto("Profesor Dos");
                prof2.setUsername("profesor2");
                prof2.setKeycloakId("local-" + UUID.randomUUID());
                prof2.setEmail("profesor2@fing.edu.uy");
                prof2.setCarrera("INGENIERIA_ELECTRONICA");
                prof2.setFacultad("FING");
                prof2.setRoles("PROFESOR,FACULTAD_DIRECTOR_CARRERA");
                prof2.setActivo(true);
                prof2.setCreatedAt(LocalDateTime.now());
                prof2.setUpdatedAt(LocalDateTime.now());
                usuarioRepository.save(prof2);

                // profesor3 - ROLE_PROFESOR
                Usuario prof3 = new Usuario();
                prof3.setNombreCompleto("Profesor Tres");
                prof3.setUsername("profesor3");
                prof3.setKeycloakId("local-" + UUID.randomUUID());
                prof3.setEmail("profesor3@fing.edu.uy");
                prof3.setCarrera("INGENIERIA_INDUSTRIAL");
                prof3.setFacultad("FING");
                prof3.setRoles("PROFESOR");
                prof3.setActivo(true);
                prof3.setCreatedAt(LocalDateTime.now());
                prof3.setUpdatedAt(LocalDateTime.now());
                usuarioRepository.save(prof3);

                log.info("✅ Usuarios creados exitosamente");
                log.info("📝 Credenciales:");
                log.info("   → profesor1 / profesor1 (ROLE_ADMIN)");
                log.info("   → profesor2 / profesor2 (ROLE_PROFESOR, ROLE_FACULTAD_DIRECTOR_CARRERA)");
                log.info("   → profesor3 / profesor3 (ROLE_PROFESOR)");
            }
        };
    }
    */
}
