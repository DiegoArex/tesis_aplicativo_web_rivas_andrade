package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.enums.EstadoNovedad;
import com.example.demo.security.PermissionConstants;
import com.example.demo.service.NovedadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Controlador REST para gestión de Novedades
//Base URL: /api/novedades
@RestController
@RequestMapping("/api/novedades")
@RequiredArgsConstructor
public class NovedadController {

    private final NovedadService novedadService;

    // GET /api/novedades - Listar todas las novedades (filtrado por rol en el
    // servicio)
    // Acceso: Todos los roles autenticados

    @GetMapping
    @PreAuthorize(PermissionConstants.NOVEDAD_READ)
    public ResponseEntity<List<NovedadResponseDTO>> listarTodas() {
        return ResponseEntity.ok(novedadService.listarTodas());
    }

    // GET /api/novedades/{id} - Obtener novedad por ID
    // Acceso: Todos los roles autenticados (validación de permisos en servicio)

    @GetMapping("/{id}")
    @PreAuthorize(PermissionConstants.NOVEDAD_READ)
    public ResponseEntity<NovedadResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(novedadService.obtenerPorId(id));
    }

    // GET /api/novedades/mis-novedades - Obtener novedades propias
    // Acceso: PROFESOR, FACULTAD_DIRECTOR_CARRERA, ADMIN

    @GetMapping("/mis-novedades")
    @PreAuthorize(PermissionConstants.USUARIOS_REGISTRADOS)
    public ResponseEntity<List<NovedadResponseDTO>> obtenerMisNovedades(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("preferred_username");
        return ResponseEntity.ok(novedadService.listarMisNovedades(username));
    }

    // GET /api/novedades/estado/{estado} - Filtrar novedades por estado
    // Acceso: ADMIN, FACULTAD_DIRECTOR_CARRERA

    @GetMapping("/estado/{estado}")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    public ResponseEntity<List<NovedadResponseDTO>> listarPorEstado(@PathVariable EstadoNovedad estado) {
        return ResponseEntity.ok(novedadService.listarPorEstado(estado));
    }

    // POST /api/novedades - Reportar nueva novedad
    // Acceso: PROFESOR, FACULTAD_DIRECTOR_CARRERA, ADMIN

    @PostMapping
    @PreAuthorize(PermissionConstants.NOVEDAD_CREATE)
    public ResponseEntity<NovedadResponseDTO> reportarNovedad(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody NovedadRequestDTO requestDTO) {
        String username = jwt.getClaim("preferred_username");
        NovedadResponseDTO novedad = novedadService.reportarNovedad(username, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novedad);
    }

    // PUT /api/novedades/{id}/estado - Cambiar estado de novedad
    // Acceso: Solo ADMIN

    @PutMapping("/{id}/estado")
    @PreAuthorize(PermissionConstants.NOVEDAD_CHANGE_STATUS)
    public ResponseEntity<NovedadResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CambiarEstadoNovedadDTO requestDTO) {
        String username = jwt.getClaim("preferred_username");
        return ResponseEntity.ok(novedadService.cambiarEstado(id, username, requestDTO));
    }

    // POST /api/novedades/{id}/imagenes - Adjuntar imagen a novedad
    // Acceso: Solo USUARIO y ADMIN

    @PostMapping("/{id}/imagenes")
    @PreAuthorize(PermissionConstants.NOVEDAD_UPLOAD_IMAGES)
    public ResponseEntity<ImagenNovedadResponseDTO> adjuntarImagen(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ImagenNovedadRequestDTO requestDTO) {
        String username = jwt.getClaim("preferred_username");
        ImagenNovedadResponseDTO imagen = novedadService.adjuntarImagen(id, username, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(imagen);
    }

    // GET /api/novedades/{id}/imagenes - Obtener imágenes de una novedad
    // Acceso: Todos los roles autenticados

    @GetMapping("/{id}/imagenes")
    @PreAuthorize(PermissionConstants.NOVEDAD_READ)
    public ResponseEntity<List<ImagenNovedadResponseDTO>> obtenerImagenes(@PathVariable Long id) {
        return ResponseEntity.ok(novedadService.obtenerImagenes(id));
    }
}
