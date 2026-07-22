package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para adjuntar imagen a una novedad
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenNovedadRequestDTO {

    @NotBlank(message = "El nombre del archivo es obligatorio")
    private String nombreArchivo;

    @NotBlank(message = "El tipo MIME es obligatorio")
    private String tipoMime; // Ej: "image/jpeg", "image/png"

    @NotBlank(message = "La imagen en Base64 es obligatoria")
    private String imagenBase64;
}
