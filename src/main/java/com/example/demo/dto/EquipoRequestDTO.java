package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear/actualizar Equipo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoRequestDTO {

    @NotBlank(message = "El código es obligatorio")
    private String codigo;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    private String marca;

    private String modelo;

    private String numeroSerie;

    @NotBlank(message = "El estado es obligatorio")
    private String estado; // "OPERATIVO", "EN_MANTENIMIENTO", "DAÑADO", "FUERA_DE_SERVICIO"

    @NotNull(message = "El ID del laboratorio es obligatorio")
    private Long laboratorioId;
}
