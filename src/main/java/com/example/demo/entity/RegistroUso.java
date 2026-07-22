package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import com.example.demo.enums.TipoRegistro;

import java.time.LocalDateTime;

// Registra entrada y salida de usuarios a laboratorios
// Soporta registros normales (1 laboratorio) y exámenes (múltiples laboratorios)

@Entity
@Table(name = "registros_uso", indexes = {
        @Index(name = "idx_registro_usuario", columnList = "usuario_id"),
        @Index(name = "idx_registro_laboratorio", columnList = "laboratorio_id"),
        @Index(name = "idx_registro_fecha_entrada", columnList = "fechaEntrada"),
        @Index(name = "idx_registro_fecha_salida", columnList = "fechaSalida"),

        // Índice compuesto para reportes por laboratorio y fecha
        @Index(name = "idx_registro_lab_fecha", columnList = "laboratorio_id, fechaEntrada")
})
@Audited
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratorio_id", nullable = false)
    private Laboratorio laboratorio;

    // Laboratorio secundario para exámenes (opcional)
    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratorio_secundario_id")
    private Laboratorio laboratorioSecundario;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaEntrada;

    @Column
    private LocalDateTime fechaSalida; // Null mientras el usuario está dentro

    @NotBlank
    @Column(nullable = false, length = 100)
    private String proposito; // Ej: clase de materia, exposicion de materia, concurso, examen, etc

    @Column(length = 500)
    private String observaciones;

    // Tipo de registro: NORMAL o EXAMEN
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TipoRegistro tipoRegistro;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

