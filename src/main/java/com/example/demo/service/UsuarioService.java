package com.example.demo.service;

import com.example.demo.dto.UsuarioRequestDTO;
import com.example.demo.dto.UsuarioResponseDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//Servicio para gestión de Usuarios

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EntityLookupService entityLookup;
    private final MapperService mapperService;

    // Obtiene o crea un usuario basado en el JWT de Keycloak
    // Este método sincroniza automáticamente los usuarios de Keycloak con la BD
    // local
    @Transactional
    public Usuario obtenerOCrearUsuarioDesdeJWT(String username, String keycloakId, String email, String nombreCompleto, String roles) {
        // Buscar usuario existente
        Usuario usuario = usuarioRepository.findByUsername(username)
                .or(() -> keycloakId != null ? usuarioRepository.findByKeycloakId(keycloakId) : java.util.Optional.empty())
                .orElse(null);

        if (usuario == null) {
            // Crear nuevo usuario automáticamente
            usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setKeycloakId(keycloakId);
            usuario.setEmail(email != null ? email : username + "@uce.edu.ec");
            usuario.setNombreCompleto(nombreCompleto != null ? nombreCompleto : username);
            usuario.setRoles(roles != null ? roles : "ROLE_PROFESOR");
            usuario.setActivo(true);

            usuario = usuarioRepository.save(usuario);
            System.out.println("✓ Usuario creado automáticamente desde Keycloak: " + username);
        } else {
            if (keycloakId != null && (usuario.getKeycloakId() == null || usuario.getKeycloakId().isBlank())) {
                usuario.setKeycloakId(keycloakId);
            }

            // Actualizar roles si cambiaron en Keycloak
            if (roles != null && !roles.equals(usuario.getRoles())) {
                usuario.setRoles(roles);
                usuario = usuarioRepository.save(usuario);
                System.out.println("✓ Roles actualizados para usuario: " + username);
            } else if (keycloakId != null && (usuario.getKeycloakId() == null || usuario.getKeycloakId().isBlank())) {
                usuario = usuarioRepository.save(usuario);
            }
        }

        return usuario;
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerUsuarioActual(String username) {
        Usuario usuario = entityLookup.findUsuarioByUsernameOrThrow(username);
        return convertirAResponseDTO(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return mapperService.mapList(
                usuarioRepository.findAll(),
                this::convertirAResponseDTO
        );
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = entityLookup.findUsuarioOrThrow(id);
        return convertirAResponseDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO requestDTO) {
        // Validar username único
        if (usuarioRepository.findByUsername(requestDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con el username: " + requestDTO.getUsername());
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(requestDTO.getUsername());
        usuario.setEmail(requestDTO.getEmail());
        usuario.setNombreCompleto(requestDTO.getNombreCompleto());
        usuario.setKeycloakId(requestDTO.getKeycloakId());
        usuario.setRoles(requestDTO.getRoles());
        usuario.setCarrera(requestDTO.getCarrera());
        usuario.setFacultad(requestDTO.getFacultad());
        usuario.setActivo(requestDTO.getActivo() != null ? requestDTO.getActivo() : true);

        Usuario guardado = usuarioRepository.save(usuario);
        return convertirAResponseDTO(guardado);
    }

    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO requestDTO) {
        Usuario usuario = entityLookup.findUsuarioOrThrow(id);

        // Validar username único (excepto el mismo usuario)
        usuarioRepository.findByUsername(requestDTO.getUsername())
                .ifPresent(u -> {
                    if (!u.getId().equals(id)) {
                        throw new RuntimeException(
                                "Ya existe otro usuario con el username: " + requestDTO.getUsername());
                    }
                });

        usuario.setUsername(requestDTO.getUsername());
        usuario.setEmail(requestDTO.getEmail());
        usuario.setNombreCompleto(requestDTO.getNombreCompleto());
        usuario.setKeycloakId(requestDTO.getKeycloakId());
        usuario.setRoles(requestDTO.getRoles());
        usuario.setCarrera(requestDTO.getCarrera());
        usuario.setFacultad(requestDTO.getFacultad());
        usuario.setActivo(requestDTO.getActivo());

        Usuario actualizado = usuarioRepository.save(usuario);
        return convertirAResponseDTO(actualizado);
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = entityLookup.findUsuarioOrThrow(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    // Soft delete: marca el usuario como eliminado sin borrarlo físicamente
    @Transactional
    public void softDelete(Long id) {
        Usuario usuario = entityLookup.findUsuarioOrThrow(id);

        usuario.softDelete();
        usuarioRepository.save(usuario);
    }

    // Restaura un usuario eliminado (soft delete)
    @Transactional
    public UsuarioResponseDTO restore(Long id) {
        Usuario usuario = entityLookup.findUsuarioOrThrow(id);

        if (!usuario.isDeleted()) {
            throw new RuntimeException("El usuario no está eliminado, no se puede restaurar");
        }

        usuario.restore();
        Usuario restaurado = usuarioRepository.save(usuario);
        return convertirAResponseDTO(restaurado);
    }

    // Obtiene todos los usuarios activos (no eliminados)
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarActivos() {
        return mapperService.mapList(
                usuarioRepository.findAllActive(),
                this::convertirAResponseDTO
        );
    }

    // Obtiene todos los usuarios eliminados (papelera)
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarEliminados() {
        return mapperService.mapList(
                usuarioRepository.findAllDeleted(),
                this::convertirAResponseDTO
        );
    }

    // Hard delete: elimina permanentemente el usuario de la base de datos
    // PRECAUCIÓN: Esta operación NO se puede deshacer
    @Transactional
    public void hardDelete(Long id) {
        Usuario usuario = entityLookup.findUsuarioOrThrow(id);

        usuarioRepository.delete(usuario);
    }

    private UsuarioResponseDTO convertirAResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setKeycloakId(usuario.getKeycloakId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setRoles(usuario.getRoles());
        dto.setCarrera(usuario.getCarrera());
        dto.setFacultad(usuario.getFacultad());
        dto.setActivo(usuario.getActivo());
        dto.setCreatedAt(usuario.getCreatedAt());
        dto.setUpdatedAt(usuario.getUpdatedAt());
        return dto;
    }
}
