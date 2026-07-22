package com.example.demo.controller;

import com.example.demo.dto.LaboratorioRequestDTO;
import com.example.demo.dto.LaboratorioResponseDTO;
import com.example.demo.security.PermissionConstants;
import com.example.demo.service.LaboratorioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Controlador REST para gestión de Laboratorios
//Base URL: /api/laboratorios
@RestController
@RequestMapping("/api/laboratorios")
@RequiredArgsConstructor
public class LaboratorioController {

    private final LaboratorioService laboratorioService;

    // GET /api/laboratorios - Listar todos los laboratorios
    // Acceso: Todos los roles autenticados

    @GetMapping
    @PreAuthorize(PermissionConstants.LAB_READ)
    public ResponseEntity<List<LaboratorioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(laboratorioService.listarTodos());
    }

    // GET /api/laboratorios/{id} - Obtener laboratorio por ID
    // Acceso: Todos los roles autenticados

    @GetMapping("/{id}")
    @PreAuthorize(PermissionConstants.LAB_READ)
    public ResponseEntity<LaboratorioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(laboratorioService.obtenerPorId(id));
    }

    // POST /api/laboratorios - Crear nuevo laboratorio
    // Acceso: Solo ADMIN

    @PostMapping
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public ResponseEntity<LaboratorioResponseDTO> crear(@Valid @RequestBody LaboratorioRequestDTO requestDTO) {
        LaboratorioResponseDTO creado = laboratorioService.crear(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // PUT /api/laboratorios/{id} - Actualizar laboratorio
    // Acceso: Solo ADMIN

    @PutMapping("/{id}")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public ResponseEntity<LaboratorioResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody LaboratorioRequestDTO requestDTO) {
        return ResponseEntity.ok(laboratorioService.actualizar(id, requestDTO));
    }

    // DELETE /api/laboratorios/{id} - Soft delete (marcar como eliminado)
    // Acceso: Solo ADMIN

    @DeleteMapping("/{id}")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        laboratorioService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/laboratorios/{id}/restore - Restaurar laboratorio eliminado
    // Acceso: Solo ADMIN

    @PostMapping("/{id}/restore")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public ResponseEntity<LaboratorioResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(laboratorioService.restore(id));
    }

    // GET /api/laboratorios/activos - Listar laboratorios activos (no eliminados)
    // Acceso: Todos los roles autenticados

    @GetMapping("/activos")
    @PreAuthorize(PermissionConstants.LAB_READ)
    public ResponseEntity<List<LaboratorioResponseDTO>> listarActivos() {
        return ResponseEntity.ok(laboratorioService.listarActivosNoEliminados());
    }

    // GET /api/laboratorios/eliminados - Listar laboratorios eliminados (papelera)
    // Acceso: Solo ADMIN

    @GetMapping("/eliminados")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public ResponseEntity<List<LaboratorioResponseDTO>> listarEliminados() {
        return ResponseEntity.ok(laboratorioService.listarEliminados());
    }

    // DELETE /api/laboratorios/{id}/permanent - Hard delete (eliminación
    // permanente)
    // Acceso: Solo ADMIN

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        laboratorioService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
