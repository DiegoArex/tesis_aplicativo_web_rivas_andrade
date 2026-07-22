package com.example.demo.entity;

import com.example.demo.enums.EstadoNovedad;
import com.example.demo.enums.TipoNovedad;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Representa un reporte de incidente o problema

@Entity
@Table(name = "novedades", indexes = {
        @Index(name = "idx_novedad_estado", columnList = "estado"),
        @Index(name = "idx_novedad_tipo", columnList = "tipo"),
        @Index(name = "idx_novedad_prioridad", columnList = "prioridad"),
        @Index(name = "idx_novedad_fecha_reporte", columnList = "fechaReporte"),
        @Index(name = "idx_novedad_usuario", columnList = "usuario_reporta_id"),
        @Index(name = "idx_novedad_laboratorio", columnList = "laboratorio_id"),
        @Index(name = "idx_novedad_equipo", columnList = "equipo_id"),
        // Índice compuesto para búsquedas por estado y prioridad
        @Index(name = "idx_novedad_estado_prioridad", columnList = "estado, prioridad")
})
@Audited
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = { "usuarioReporta", "laboratorio", "equipo", "imagenes" })
@NoArgsConstructor
@AllArgsConstructor
public class Novedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank
    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    private TipoNovedad tipo; // EQUIPO o GENERAL

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    private EstadoNovedad estado = EstadoNovedad.PENDIENTE;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String prioridad; // "BAJA", "MEDIA", "ALTA", "CRITICA"

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_reporta_id", nullable = false)
    private Usuario usuarioReporta;

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratorio_id", nullable = false)
    private Laboratorio laboratorio;

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id")
    private Equipo equipo; // Null si tipo=GENERAL

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaReporte;

    @Column
    private LocalDateTime fechaResolucion;

    @Column(length = 1000)
    private String observacionesResolucion;

    @NotAudited
    @OneToMany(mappedBy = "novedad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagenNovedad> imagenes = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
