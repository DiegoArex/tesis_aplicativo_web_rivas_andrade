package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registrar salida de un laboratorio
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsoSalidaDTO {

    private String observaciones; // Observaciones adicionales al salir (opcional)
}
