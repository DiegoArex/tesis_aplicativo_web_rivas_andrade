package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PARTE 2: UsuarioListDTO - Proyección para tablas DataTables
 * 
 * Campos MÍNIMOS para listas:
 * - id, username, nombreCompleto, email
 * - carrera, facultad, activo
 * 
 * Reduce payload JSON ~80% vs respuesta completa
 * SIN campos innecesarios: roles, keycloakId, createdAt, updatedAt
 * 
 * Usado para dropdown/selects y listas administrativas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioListDTO {
    
    private Long id;
    private String username;
    private String nombreCompleto;
    private String email;
    private String carrera;
    private String facultad;
    private Boolean activo;
    
    public String getDisplayName() {
        return nombreCompleto != null ? nombreCompleto : username;
    }
}
