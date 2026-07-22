package com.example.demo.controller;

import com.example.demo.dto.EquipoRequestDTO;
import com.example.demo.dto.EquipoResponseDTO;
import com.example.demo.security.PermissionConstants;
import com.example.demo.service.EquipoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Controlador REST para gestión de Equipos
//Base URL: /api/equipos
@RestController
@RequestMapping("/api/equipos")
@RequiredArgsConstructor
public class EquipoController {

    private final EquipoService equipoService;

    // GET /api/equipos - Listar todos los equipos
    // Acceso: Todos los roles autenticados
    @GetMapping
    @PreAuthorize(PermissionConstants.EQUIPO_READ)
    public ResponseEntity<List<EquipoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(equipoService.listarTodos());
    }

    // GET /api/equipos/{id} - Obtener equipo por ID
    // Acceso: Todos los roles autenticados
    @GetMapping("/{id}")
    @PreAuthorize(PermissionConstants.EQUIPO_READ)
    public ResponseEntity<EquipoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(equipoService.obtenerPorId(id));
    }

    // GET /api/equipos/laboratorio/{labId} - Listar equipos de un laboratorio
    // Acceso: Todos los roles autenticados
    @GetMapping("/laboratorio/{labId}")
    @PreAuthorize(PermissionConstants.EQUIPO_READ)
    public ResponseEntity<List<EquipoResponseDTO>> listarPorLaboratorio(@PathVariable Long labId) {
        return ResponseEntity.ok(equipoService.listarPorLaboratorio(labId));
    }

    // POST /api/equipos - Crear nuevo equipo
    // Acceso: Solo ADMIN
    @PostMapping
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public ResponseEntity<EquipoResponseDTO> crear(@Valid @RequestBody EquipoRequestDTO requestDTO) {
        EquipoResponseDTO creado = equipoService.crear(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // PUT /api/equipos/{id} - Actualizar equipo
    // Acceso: Solo ADMIN
    @PutMapping("/{id}")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public ResponseEntity<EquipoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EquipoRequestDTO requestDTO) {
        return ResponseEntity.ok(equipoService.actualizar(id, requestDTO));
    }

    // DELETE /api/equipos/{id} - Soft delete (marcar como eliminado)
    // Acceso: Solo ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        equipoService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/equipos/{id}/restore - Restaurar equipo eliminado
    // Acceso: Solo ADMIN
    @PostMapping("/{id}/restore")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public ResponseEntity<EquipoResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(equipoService.restore(id));
    }

    // GET /api/equipos/activos - Listar equipos activos (no eliminados)
    // Acceso: Todos los roles autenticados
    @GetMapping("/activos")
    @PreAuthorize(PermissionConstants.EQUIPO_READ)
    public ResponseEntity<List<EquipoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(equipoService.listarActivos());
    }

    // GET /api/equipos/eliminados - Listar equipos eliminados (papelera)
    // Acceso: Solo ADMIN
    @GetMapping("/eliminados")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public ResponseEntity<List<EquipoResponseDTO>> listarEliminados() {
        return ResponseEntity.ok(equipoService.listarEliminados());
    }

    // DELETE /api/equipos/{id}/permanent - Hard delete (eliminación permanente)
    // Acceso: Solo ADMIN
    // PRECAUCIÓN: Esta operación NO se puede deshacer
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        equipoService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
