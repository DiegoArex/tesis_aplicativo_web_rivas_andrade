package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para RegistroUso
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsoResponseDTO {

    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private Long laboratorioId;
    private String laboratorioNombre;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaSalida;
    private String proposito;
    private String observaciones;
    private Long duracionMinutos; // Calculado si hay salida
    private Boolean activo; // true si fechaSalida es null
    private String tipoRegistro; // NORMAL o EXAMEN
    private LocalDateTime createdAt;

    public String getDuracionFormateada() {
        if (duracionMinutos == null) {
            return "-";
        }

        long horas = duracionMinutos / 60;
        long minutosRestantes = duracionMinutos % 60;
        return horas + " h " + minutosRestantes + " min";
    }
}
