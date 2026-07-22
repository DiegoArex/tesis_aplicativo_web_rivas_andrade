package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Laboratorio
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaboratorioResponseDTO {

    private Long id;
    private String nombre;
    private String ubicacion;
    private Integer capacidad;
    private String descripcion;
    private Boolean activo;
    private Integer cantidadEquipos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
