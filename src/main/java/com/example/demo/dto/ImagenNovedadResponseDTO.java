package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para ImagenNovedad
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenNovedadResponseDTO {

    private Long id;
    private Long novedadId;
    private String nombreArchivo;
    private String tipoMime;
    private String imagenBase64;
    private LocalDateTime createdAt;

    public String getDataUri() {
        if (imagenBase64 == null || imagenBase64.isBlank()) {
            return "";
        }
        String mime = (tipoMime == null || tipoMime.isBlank()) ? "image/png" : tipoMime;
        return "data:" + mime + ";base64," + imagenBase64;
    }
}
