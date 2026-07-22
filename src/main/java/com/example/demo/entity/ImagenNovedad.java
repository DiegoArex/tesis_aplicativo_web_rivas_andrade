package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.LocalDateTime;

@Entity
@Table(name = "imagenes_novedad")
@Audited
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenNovedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novedad_id", nullable = false)
    private Novedad novedad;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String nombreArchivo;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String tipoMime; // Ej: "image/jpeg", "image/png"

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String imagenBase64; // Imagen codificada en Base64

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
