package com.example.demo.repository;

import com.example.demo.dto.NovedadListDTO;
import com.example.demo.entity.Novedad;
import com.example.demo.enums.EstadoNovedad;
import com.example.demo.enums.TipoNovedad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NovedadRepository extends JpaRepository<Novedad, Long> {

    // ===== PARTE 2: MÉTODOS OPTIMIZADOS CON PROYECCIONES =====
    
    /**
     * PARTE 2: Query optimizada para listas DataTables
     * Retorna NovedadListDTO con SOLO campos necesarios
     * Reduce payload ~70% y optimiza paginación
     */
    @Query("SELECT NEW com.example.demo.dto.NovedadListDTO(" +
           "n.id, n.titulo, n.tipo, n.estado, n.prioridad, " +
           "u.nombreCompleto, l.nombre, n.fechaReporte, n.fechaResolucion, " +
           "CAST(COALESCE(SIZE(n.imagenes), 0) AS int)) " +
           "FROM Novedad n " +
           "JOIN n.usuarioReporta u " +
           "JOIN n.laboratorio l " +
           "LEFT JOIN n.imagenes " +
           "ORDER BY n.fechaReporte DESC")
    Page<NovedadListDTO> findAllListDTO(Pageable pageable);
    
    /**
     * PARTE 2: Query para novedades por estado
     */
    @Query("SELECT NEW com.example.demo.dto.NovedadListDTO(" +
           "n.id, n.titulo, n.tipo, n.estado, n.prioridad, " +
           "u.nombreCompleto, l.nombre, n.fechaReporte, n.fechaResolucion, " +
           "CAST(COALESCE(SIZE(n.imagenes), 0) AS int)) " +
           "FROM Novedad n " +
           "JOIN n.usuarioReporta u " +
           "JOIN n.laboratorio l " +
           "LEFT JOIN n.imagenes " +
           "WHERE n.estado = :estado " +
           "ORDER BY n.fechaReporte DESC")
    Page<NovedadListDTO> findByEstadoListDTO(@Param("estado") EstadoNovedad estado, Pageable pageable);
    
    /**
     * PARTE 2: Query para novedades pendientes ordenadas por prioridad
     */
    @Query("SELECT NEW com.example.demo.dto.NovedadListDTO(" +
           "n.id, n.titulo, n.tipo, n.estado, n.prioridad, " +
           "u.nombreCompleto, l.nombre, n.fechaReporte, n.fechaResolucion, " +
           "CAST(COALESCE(SIZE(n.imagenes), 0) AS int)) " +
           "FROM Novedad n " +
           "JOIN n.usuarioReporta u " +
           "JOIN n.laboratorio l " +
           "LEFT JOIN n.imagenes " +
           "WHERE n.estado = :estado " +
           "ORDER BY CASE n.prioridad WHEN 'CRITICA' THEN 1 WHEN 'ALTA' THEN 2 WHEN 'MEDIA' THEN 3 ELSE 4 END, " +
           "n.fechaReporte DESC")
    List<NovedadListDTO> findByEstadoListDTOOrderByPrioridad(@Param("estado") EstadoNovedad estado);
    
    // ===== MÉTODOS ORIGINALES (COMPATIBILIDAD) =====

    // Métodos optimizados con JOIN FETCH y paginación
    @Query("SELECT DISTINCT n FROM Novedad n " +
           "JOIN FETCH n.usuarioReporta u " +
           "JOIN FETCH n.laboratorio l " +
           "LEFT JOIN FETCH n.equipo " +
           "ORDER BY n.fechaReporte DESC")
    Page<Novedad> findAllWithRelations(Pageable pageable);

    @Query("SELECT DISTINCT n FROM Novedad n " +
           "JOIN FETCH n.usuarioReporta u " +
           "JOIN FETCH n.laboratorio l " +
           "LEFT JOIN FETCH n.equipo " +
           "ORDER BY n.fechaReporte DESC")
    List<Novedad> findAllWithRelationsUnpaged();

    @Query("SELECT DISTINCT n FROM Novedad n " +
           "JOIN FETCH n.usuarioReporta u " +
           "JOIN FETCH n.laboratorio l " +
           "LEFT JOIN FETCH n.equipo " +
           "WHERE n.estado = :estado " +
           "ORDER BY n.fechaReporte DESC")
    Page<Novedad> findByEstadoWithRelations(@Param("estado") EstadoNovedad estado, Pageable pageable);

    @Query("SELECT DISTINCT n FROM Novedad n " +
           "JOIN FETCH n.usuarioReporta u " +
           "JOIN FETCH n.laboratorio l " +
           "LEFT JOIN FETCH n.equipo " +
           "WHERE n.estado = :estado " +
           "ORDER BY n.fechaReporte DESC")
    List<Novedad> findByEstadoWithRelationsUnpaged(@Param("estado") EstadoNovedad estado);

    @Query("SELECT DISTINCT n FROM Novedad n " +
           "JOIN FETCH n.usuarioReporta u " +
           "JOIN FETCH n.laboratorio l " +
           "LEFT JOIN FETCH n.equipo " +
           "WHERE n.usuarioReporta.id = :usuarioId " +
           "ORDER BY n.fechaReporte DESC")
    List<Novedad> findByUsuarioReportaIdWithRelations(@Param("usuarioId") Long usuarioId);

    @Query("SELECT DISTINCT n FROM Novedad n " +
           "JOIN FETCH n.usuarioReporta u " +
           "JOIN FETCH n.laboratorio l " +
           "LEFT JOIN FETCH n.equipo " +
           "WHERE u.carrera = :carrera " +
           "ORDER BY n.fechaReporte DESC")
    List<Novedad> findByCarreraWithRelations(@Param("carrera") String carrera);

    @Query("SELECT DISTINCT n FROM Novedad n " +
           "JOIN FETCH n.usuarioReporta u " +
           "JOIN FETCH n.laboratorio l " +
           "LEFT JOIN FETCH n.equipo " +
           "WHERE n.laboratorio.id = :laboratorioId " +
           "ORDER BY n.fechaReporte DESC")
    List<Novedad> findByLaboratorioIdWithRelations(@Param("laboratorioId") Long laboratorioId);

    @Query("SELECT DISTINCT n FROM Novedad n " +
           "JOIN FETCH n.usuarioReporta u " +
           "JOIN FETCH n.laboratorio l " +
           "LEFT JOIN FETCH n.equipo " +
           "WHERE n.fechaReporte BETWEEN :inicio AND :fin " +
           "ORDER BY n.fechaReporte DESC")
    Page<Novedad> findByFechaReporteBetweenWithRelations(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            Pageable pageable);

    // Métodos legacy sin optimización (mantener compatibilidad)
    List<Novedad> findAllByOrderByFechaReporteDesc();
    List<Novedad> findByEstado(EstadoNovedad estado);
    List<Novedad> findByEstadoOrderByFechaReporteDesc(EstadoNovedad estado);
    List<Novedad> findByUsuarioReportaId(Long usuarioId);
    List<Novedad> findByUsuarioReportaIdOrderByFechaReporteDesc(Long usuarioId);
    List<Novedad> findByLaboratorioId(Long laboratorioId);
    List<Novedad> findByEquipoId(Long equipoId);
    List<Novedad> findByTipo(TipoNovedad tipo);
    List<Novedad> findByPrioridad(String prioridad);
    List<Novedad> findByLaboratorioIdAndEstado(Long laboratorioId, EstadoNovedad estado);
    List<Novedad> findByFechaReporteBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT n FROM Novedad n WHERE n.usuarioReporta.carrera = :carrera")
    List<Novedad> findByCarrera(@Param("carrera") String carrera);

    @Query("SELECT n FROM Novedad n WHERE n.estado = :estado ORDER BY " +
            "CASE n.prioridad WHEN 'CRITICA' THEN 1 WHEN 'ALTA' THEN 2 WHEN 'MEDIA' THEN 3 ELSE 4 END, " +
            "n.fechaReporte DESC")
    List<Novedad> findByEstadoOrderByPrioridadAndFecha(@Param("estado") EstadoNovedad estado);
}
