package com.example.demo.dto;

import com.example.demo.enums.EstadoNovedad;
import com.example.demo.enums.TipoNovedad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear/reportar Novedad
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovedadRequestDTO {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "El tipo es obligatorio")
    private TipoNovedad tipo; // EQUIPO o GENERAL

    @NotBlank(message = "La prioridad es obligatoria")
    private String prioridad; // "BAJA", "MEDIA", "ALTA", "CRITICA"

    private EstadoNovedad estado; // PENDIENTE, EN_PROCESO, SOLUCIONADA (default: PENDIENTE)

    @NotNull(message = "El ID del laboratorio es obligatorio")
    private Long laboratorioId;

    private Long equipoId; // Obligatorio si tipo=EQUIPO, null si tipo=GENERAL
}
