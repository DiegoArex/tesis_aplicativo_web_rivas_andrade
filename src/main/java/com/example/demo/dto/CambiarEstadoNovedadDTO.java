package com.example.demo.dto;

import com.example.demo.enums.EstadoNovedad;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cambiar el estado de una novedad (solo ADMIN)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoNovedadDTO {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoNovedad nuevoEstado;

    private String observacionesResolucion; // Opcional, para cuando se marca como SOLUCIONADA
}
