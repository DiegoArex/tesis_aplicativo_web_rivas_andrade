package com.example.demo.dto;

import com.example.demo.enums.EstadoNovedad;
import com.example.demo.enums.TipoNovedad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PARTE 2: NovedadListDTO - Proyección para tablas DataTables
 * 
 * Campos MÍNIMOS para listas:
 * - id, titulo, tipo, estado, prioridad
 * - usuarioReportaNombre, laboratorioNombre
 * - fechaReporte, cantidadImagenes
 * 
 * Reduce payload JSON ~70% vs respuesta completa
 * Optimizado para filtros y ordenamiento
 * SIN campos pesados: descripcion, observacionesResolucion, imagenes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovedadListDTO {
    
    private Long id;
    private String titulo;
    private TipoNovedad tipo;
    private EstadoNovedad estado;
    private String prioridad;
    private String usuarioReportaNombre;
    private String laboratorioNombre;
    private LocalDateTime fechaReporte;
    private LocalDateTime fechaResolucion;
    private Integer cantidadImagenes;
    
    public boolean esPendiente() {
        return EstadoNovedad.PENDIENTE == estado;
    }
    
    public boolean esCritica() {
        return "CRITICA".equals(prioridad);
    }
}
