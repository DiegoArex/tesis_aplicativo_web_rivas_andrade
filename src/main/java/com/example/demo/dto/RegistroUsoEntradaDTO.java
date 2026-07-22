package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registrar entrada a un laboratorio (versión mejorada)
 * Soporta registros normales y exámenes con múltiples laboratorios
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsoEntradaDTO {

    @NotNull(message = "El ID del laboratorio es obligatorio")
    private Long laboratorioId;

    // Para exámenes: segundo laboratorio (opcional)
    private Long laboratorioSecundarioId;

    @NotBlank(message = "El propósito es obligatorio")
    private String proposito; // Ej: "Clase", "Investigación", "Práctica Libre", "Examen"

    private String observaciones;

    // Indica si es un registro para examen
    private Boolean esExamen = false;
}
