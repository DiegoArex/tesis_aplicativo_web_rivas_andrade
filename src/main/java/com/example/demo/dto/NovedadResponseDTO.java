package com.example.demo.dto;

import com.example.demo.enums.EstadoNovedad;
import com.example.demo.enums.TipoNovedad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Novedad
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovedadResponseDTO {

    private Long id;
    private String titulo;
    private String descripcion;
    private TipoNovedad tipo;
    private EstadoNovedad estado;
    private String prioridad;
    private Long usuarioReportaId;
    private String usuarioReportaNombre;
    private Long laboratorioId;
    private String laboratorioNombre;
    private Long equipoId;
    private String equipoCodigo;
    private LocalDateTime fechaReporte;
    private LocalDateTime fechaResolucion;
    private String observacionesResolucion;
    private Integer cantidadImagenes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
