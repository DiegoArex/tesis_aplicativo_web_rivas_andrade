package com.example.demo.dto;

import com.example.demo.enums.TipoRegistro;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * PARTE 2: RegistroUsoListDTO - Proyección para tablas DataTables
 * 
 * Campos MÍNIMOS para listas:
 * - id, usuarioNombre, laboratorioNombre
 * - fechaEntrada, fechaSalida, duracionMinutos
 * - activo, tipoRegistro
 * 
 * Reduce payload JSON ~60% vs respuesta completa
 * Optimizado para server-side paginación
 * SIN campos pesados: observaciones, createdAt, updatedAt
 * 
 * NOTA: duracionMinutos se calcula en memoria (no en BD)
 * por compatibilidad con todos los dialectos SQL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsoListDTO {
    
    private Long id;
    private String usuarioNombre;
    private String laboratorioNombre;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaSalida;
    private Long duracionMinutos;  // No usado en query, calculado en getter
    private Boolean activo;
    private TipoRegistro tipoRegistro;
    
    /**
     * Calcula la duración en minutos basada en fechas
     * Si no hay salida, calcula desde entrada hasta ahora
     */
    public Long getDuracionMinutos() {
        if (fechaEntrada == null) {
            return null;
        }
        
        LocalDateTime fin = (fechaSalida != null) ? fechaSalida : LocalDateTime.now();
        long minutos = ChronoUnit.MINUTES.between(fechaEntrada, fin);
        return minutos;
    }
    
    public String getDuracionFormateada() {
        Long minutos = getDuracionMinutos();
        if (minutos == null || !activo) {
            return "-";
        }
        long horas = minutos / 60;
        long minutosRestantes = minutos % 60;
        return horas + "h " + minutosRestantes + "min";
    }
}
