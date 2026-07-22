package com.example.demo.controller;

import com.example.demo.dto.UsuarioRequestDTO;
import com.example.demo.dto.UsuarioResponseDTO;
import com.example.demo.security.PermissionConstants;
import com.example.demo.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Controlador REST para gestión de Usuarios
//Base URL: /api/usuarios
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // GET /api/usuarios/me - Obtener información del usuario actual
    // Acceso: Todos los roles autenticados
    @GetMapping("/me")
    @PreAuthorize(PermissionConstants.USUARIO_ME)
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioActual(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("preferred_username");
        return ResponseEntity.ok(usuarioService.obtenerUsuarioActual(username));
    }

    // GET /api/usuarios - Listar todos los usuarios
    // Acceso: Solo ADMIN
    @GetMapping
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // GET /api/usuarios/{id} - Obtener usuario por ID
    // Acceso: Solo ADMIN
    @GetMapping("/{id}")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // POST /api/usuarios - Crear nuevo usuario
    // Acceso: Solo ADMIN
    @PostMapping
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioRequestDTO requestDTO) {
        UsuarioResponseDTO creado = usuarioService.crear(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // PUT /api/usuarios/{id} - Actualizar usuario
    // Acceso: Solo ADMIN
    @PutMapping("/{id}")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO requestDTO) {
        return ResponseEntity.ok(usuarioService.actualizar(id, requestDTO));
    }

    // DELETE /api/usuarios/{id} - Soft delete (marcar como eliminado)
    // Acceso: Solo ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        usuarioService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/usuarios/{id}/restore - Restaurar usuario eliminado
    // Acceso: Solo ADMIN
    @PostMapping("/{id}/restore")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public ResponseEntity<UsuarioResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.restore(id));
    }

    // GET /api/usuarios/activos - Listar usuarios activos (no eliminados)
    // Acceso: Solo ADMIN
    @GetMapping("/activos")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public ResponseEntity<List<UsuarioResponseDTO>> listarActivos() {
        return ResponseEntity.ok(usuarioService.listarActivos());
    }

    // GET /api/usuarios/eliminados - Listar usuarios eliminados (papelera)
    // Acceso: Solo ADMIN
    @GetMapping("/eliminados")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public ResponseEntity<List<UsuarioResponseDTO>> listarEliminados() {
        return ResponseEntity.ok(usuarioService.listarEliminados());
    }

    // DELETE /api/usuarios/{id}/permanent - Hard delete (eliminación permanente)
    // Acceso: Solo ADMIN
    // PRECAUCIÓN: Esta operación NO se puede deshacer
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        usuarioService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
