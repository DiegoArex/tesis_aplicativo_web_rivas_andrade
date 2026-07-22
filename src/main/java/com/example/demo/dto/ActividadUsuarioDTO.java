package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para mostrar actividades de usuarios en auditoría
 * Utilizado para generar reportes de qué hizo cada usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActividadUsuarioDTO {
    
    // Información del usuario
    private String usuario;
    private String email;
    private String nombreCompleto;
    
    // Información de la actividad
    private String tipoActividad;        // NOVEDAD_REPORTADA, LABORATORIO_RESERVADO, CAMBIO_EQUIPO, etc
    private String descripcionActividad; // Descripción clara en español
    private String tipoEntidad;          // Usuario, Laboratorio, Equipo, Novedad
    private String nombreEntidad;        // Nombre específico de la entidad
    private Long idEntidad;
    
    // Detalles de la actividad
    private String detalles;             // Información adicional
    private String estado;               // Estado actual del registro (activo/eliminado/etc)
    
    // Información técnica
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fecha;
    private String ipAddress;
    private Long revisionId;
    
    // Resumen de actividades del usuario
    private Integer totalNovedades;
    private Integer totalReservas;
    private Integer totalCambios;
    private Integer totalElimaciones;
    private Integer totalAcciones;
    
    // Para estadísticas
    private Long tiempoPromedio;         // Tiempo promedio de operación en ms
}
