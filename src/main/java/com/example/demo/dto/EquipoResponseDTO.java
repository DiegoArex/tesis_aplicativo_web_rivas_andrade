package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Equipo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoResponseDTO {

    private Long id;
    private String codigo;
    private String tipo;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String estado;
    private Long laboratorioId;
    private String laboratorioNombre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
