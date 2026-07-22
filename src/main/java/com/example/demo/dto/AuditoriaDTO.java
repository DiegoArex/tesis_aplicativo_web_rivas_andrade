package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO para registros de auditoría obtenidos de Hibernate Envers
 * Contiene información detallada sobre cambios en el sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaDTO {
    private Long revisionId;
    private LocalDateTime fechaCambio;
    private String usuario;
    private String tipoEntidad;          // Usuario, Laboratorio, Equipo, Novedad, RegistroUso
    private Long idEntidad;
    private String nombreEntidad;        // Nombre/descripción de la entidad afectada (ej: nombre del lab, usuario, equipo)
    private String accion;               // INSERT (0), UPDATE (1), DELETE (2)
    private String descripcionAccion;    // Traducción legible: "Creado", "Modificado", "Eliminado"
    private String detalles;             // Descripción general del cambio
    
    // Campos para mostrar cambios específicos
    private String camposModificados;    // JSON o texto con los campos que cambiaron
    private String valorAnterior;        // Valor anterior en formato legible
    private String valorNuevo;           // Valor nuevo en formato legible
    private String ipAddress;            // IP de origen del cambio
    private Long duracionOperacion;      // Duración en ms (si aplica)
}
