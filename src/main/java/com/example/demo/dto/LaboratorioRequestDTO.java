package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear/actualizar Laboratorio
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaboratorioRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La ubicación es obligatoria")
    private String ubicacion;

    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    private Integer capacidad;

    private String descripcion;

    private Boolean activo = true;
}
