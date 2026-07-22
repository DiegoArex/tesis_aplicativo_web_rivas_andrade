package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para mostrar auditoría de uso de laboratorios por usuario
 * Enfocado en: qué sala usó, cuándo entró/salió, qué clase dio, si reportó novedad
 * Soporta tanto uso normal (1 laboratorio) como exámenes (2 laboratorios)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaUsoLaboratorioDTO {
    
    // Usuario
    private Long usuarioId;
    private String usuario;                      // Nombre de usuario
    private String nombreCompletoUsuario;        // Nombre completo
    
    // Laboratorio/Sala (Principal)
    private Long laboratorioId;
    private String nombreLaboratorio;            // Ej: "Lab-001" o "Laboratorio A"
    private String ubicacionLaboratorio;         // Ubicación
    
    // Laboratorio Secundario (Para exámenes)
    private Long laboratorioSecundarioId;        // ID del segundo laboratorio
    private String nombreLaboratorioSecundario;  // Nombre del segundo laboratorio
    private String ubicacionLaboratorioSecundario; // Ubicación del segundo laboratorio
    
    // Información de Uso
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime horaEntrada;           // Cuándo entró
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime horaSalida;            // Cuándo salió
    
    private String proposito;                    // Qué clase dio / actividad
    private String observaciones;                // Detalles adicionales
    
    // Tipo de uso
    private String tipoRegistro;                 // "NORMAL" o "EXAMEN"
    private Boolean esExamen;                    // true si es examen (laboratorios múltiples)
    
    // Novedad (si la hay)
    private Long novedadId;                      // ID de la novedad si existe
    private String nombreNovedad;                // Título de la novedad (si existe)
    private String tipoNovedad;                  // Tipo de novedad
    private String estadoNovedad;                // Estado de la novedad
    
    // Información calculada
    private Long duracionMinutos;                // Duración total de la sesión en minutos
    private String duracionFormatada;            // Duración formateada (ej: "1h 30min")
    
    // Campo para mostrar laboratorios (uno o dos)
    public String getLaboratoriosCompleto() {
        if (esExamen && nombreLaboratorioSecundario != null) {
            return nombreLaboratorio + " y " + nombreLaboratorioSecundario;
        }
        return nombreLaboratorio;
    }
}
