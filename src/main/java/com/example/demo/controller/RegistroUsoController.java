package com.example.demo.controller;

import com.example.demo.dto.RegistroUsoEntradaDTO;
import com.example.demo.dto.RegistroUsoResponseDTO;
import com.example.demo.dto.RegistroUsoSalidaDTO;
import com.example.demo.security.PermissionConstants;
import com.example.demo.service.RegistroUsoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//Controlador REST para gestión de Registros de Uso
//Base URL: /api/registros
@RestController
@RequestMapping("/api/registros")
@RequiredArgsConstructor
public class RegistroUsoController {

    private final RegistroUsoService registroUsoService;

    // POST /api/registros/entrada - Registrar entrada a laboratorio
    // Acceso: PROFESOR, FACULTAD_DIRECTOR_CARRERA, ADMIN

    @PostMapping("/entrada")
    @PreAuthorize(PermissionConstants.REGISTRO_ENTRADA_SALIDA)
    public ResponseEntity<RegistroUsoResponseDTO> registrarEntrada(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RegistroUsoEntradaDTO requestDTO) {
        String username = jwt.getClaim("preferred_username");
        String keycloakId = jwt.getSubject();
        String email = jwt.getClaim("email");
        String nombreCompleto = jwt.getClaim("name");

        // Extraer roles del JWT
        String roles = extraerRolesDelJWT(jwt);

        RegistroUsoResponseDTO registro = registroUsoService.registrarEntrada(username, keycloakId, email,
            nombreCompleto, roles, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registro);
    }

    // Extrae los roles del JWT de Keycloak.
    // Devuelve null si no se pueden obtener los roles, para evitar sobreescribir
    // los roles almacenados en la base de datos con un valor incorrecto.
    private String extraerRolesDelJWT(Jwt jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> rolesList = (List<String>) realmAccess.get("roles");
                String roles = rolesList.stream()
                        .filter(role -> !role.equals("offline_access") && !role.startsWith("default-roles-"))
                        .map(role -> role.toUpperCase().startsWith("ROLE_") ? role.toUpperCase()
                                : "ROLE_" + role.toUpperCase())
                        .collect(java.util.stream.Collectors.joining(","));
                return roles.isBlank() ? null : roles;
            }
        } catch (Exception e) {
            // Si falla la extraccion, no asignamos un rol por defecto para no corromper
            // los datos del usuario en la base de datos
            System.err.println("Error extrayendo roles del JWT: " + e.getMessage());
        }
        return null;
    }

    // PUT /api/registros/salida - Registrar salida de laboratorio
    // Acceso: PROFESOR, FACULTAD_DIRECTOR_CARRERA, ADMIN

    @PutMapping("/salida")
    @PreAuthorize(PermissionConstants.REGISTRO_ENTRADA_SALIDA)
    public ResponseEntity<RegistroUsoResponseDTO> registrarSalida(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RegistroUsoSalidaDTO requestDTO) {
        String username = jwt.getClaim("preferred_username");
        return ResponseEntity.ok(registroUsoService.registrarSalida(username, requestDTO));
    }

    // GET /api/registros/mis-registros - Obtener historial propio
    // Acceso: PROFESOR, FACULTAD_DIRECTOR_CARRERA, ADMIN

    @GetMapping("/mis-registros")
    @PreAuthorize(PermissionConstants.REGISTRO_ENTRADA_SALIDA)
    public ResponseEntity<List<RegistroUsoResponseDTO>> obtenerMisRegistros(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("preferred_username");
        return ResponseEntity.ok(registroUsoService.obtenerMisRegistros(username));
    }

    // GET /api/registros/todos - Obtener todos los registros
    // Acceso: ADMIN, FACULTAD_DIRECTOR_CARRERA

    @GetMapping("/todos")
    @PreAuthorize(PermissionConstants.REGISTRO_READ)
    public ResponseEntity<List<RegistroUsoResponseDTO>> obtenerTodosRegistros() {
        return ResponseEntity.ok(registroUsoService.obtenerTodosRegistros());
    }

    // GET /api/registros/usuario/{id} - Obtener historial de un usuario específico
    // Acceso: ADMIN, FACULTAD_DIRECTOR_CARRERA

    @GetMapping("/usuario/{id}")
    @PreAuthorize(PermissionConstants.REGISTRO_READ)
    public ResponseEntity<List<RegistroUsoResponseDTO>> obtenerRegistrosPorUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(registroUsoService.obtenerRegistrosPorUsuario(id));
    }

    // GET /api/registros/laboratorio/{id} - Obtener registros de un laboratorio
    // Acceso: ADMIN, FACULTAD_DIRECTOR_CARRERA

    @GetMapping("/laboratorio/{id}")
    @PreAuthorize(PermissionConstants.REGISTRO_READ)
    public ResponseEntity<List<RegistroUsoResponseDTO>> obtenerRegistrosPorLaboratorio(@PathVariable Long id) {
        return ResponseEntity.ok(registroUsoService.obtenerRegistrosPorLaboratorio(id));
    }

    // GET /api/registros/activos - Obtener registros activos (sin salida)
    // Acceso: Solo ADMIN

    @GetMapping("/activos")
    @PreAuthorize(PermissionConstants.REGISTRO_ADMIN)
    public ResponseEntity<List<RegistroUsoResponseDTO>> obtenerRegistrosActivos() {
        return ResponseEntity.ok(registroUsoService.obtenerRegistrosActivos());
    }
}
