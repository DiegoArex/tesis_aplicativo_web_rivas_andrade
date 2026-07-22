package com.example.demo.repository;

import com.example.demo.dto.RegistroUsoListDTO;
import com.example.demo.entity.RegistroUso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroUsoRepository extends JpaRepository<RegistroUso, Long> {

	// ===== PARTE 2: MÉTODOS OPTIMIZADOS CON PROYECCIONES =====
	
	/**
	 * PARTE 2: Query optimizada para listas DataTables
	 * Retorna RegistroUsoListDTO con SOLO campos necesarios
	 * Reduce payload ~60% y optimiza paginación
	 * 
	 * Nota: duracionMinutos calculado en memoria (null aquí)
	 * El DTO lo calcula en getDuracionFormateada()
	 */
	@Query("SELECT NEW com.example.demo.dto.RegistroUsoListDTO(" +
	       "r.id, u.nombreCompleto, l.nombre, r.fechaEntrada, r.fechaSalida, " +
	       "0L, " +
	       "CASE WHEN r.fechaSalida IS NULL THEN true ELSE false END, r.tipoRegistro) " +
	       "FROM RegistroUso r " +
	       "JOIN r.usuario u " +
	       "JOIN r.laboratorio l " +
	       "ORDER BY r.fechaEntrada DESC")
	Page<RegistroUsoListDTO> findAllListDTO(Pageable pageable);
	
	/**
	 * PARTE 2: Query para registros activos sin FETCH
	 * Nota: duracionMinutos calculado en memoria (null aquí)
	 */
	@Query("SELECT NEW com.example.demo.dto.RegistroUsoListDTO(" +
	       "r.id, u.nombreCompleto, l.nombre, r.fechaEntrada, r.fechaSalida, " +
	       "0L, true, r.tipoRegistro) " +
	       "FROM RegistroUso r " +
	       "JOIN r.usuario u " +
	       "JOIN r.laboratorio l " +
	       "WHERE r.fechaSalida IS NULL " +
	       "ORDER BY r.fechaEntrada DESC")
	List<RegistroUsoListDTO> findActiveListDTO();
	
	// ===== MÉTODOS ORIGINALES (COMPATIBILIDAD) =====

        @Query("SELECT DISTINCT r FROM RegistroUso r " +
               "JOIN FETCH r.usuario u " +
               "JOIN FETCH r.laboratorio l " +
               "LEFT JOIN FETCH r.laboratorioSecundario")
        List<RegistroUso> findAllWithRelations();

        @Query("SELECT DISTINCT r FROM RegistroUso r " +
               "JOIN FETCH r.usuario u " +
               "JOIN FETCH r.laboratorio l " +
               "LEFT JOIN FETCH r.laboratorioSecundario " +
               "ORDER BY r.fechaEntrada DESC")
        Page<RegistroUso> findAllWithRelations(Pageable pageable);

        @Query("SELECT DISTINCT r FROM RegistroUso r " +
               "JOIN FETCH r.usuario u " +
               "JOIN FETCH r.laboratorio l " +
               "LEFT JOIN FETCH r.laboratorioSecundario " +
               "WHERE u.id = :usuarioId")
        List<RegistroUso> findByUsuarioIdWithRelations(@Param("usuarioId") Long usuarioId);

        @Query("SELECT DISTINCT r FROM RegistroUso r " +
               "JOIN FETCH r.usuario u " +
               "JOIN FETCH r.laboratorio l " +
               "LEFT JOIN FETCH r.laboratorioSecundario " +
               "WHERE l.id = :laboratorioId")
        List<RegistroUso> findByLaboratorioIdWithRelations(@Param("laboratorioId") Long laboratorioId);

        @Query("SELECT DISTINCT r FROM RegistroUso r " +
               "JOIN FETCH r.usuario u " +
               "JOIN FETCH r.laboratorio l " +
               "LEFT JOIN FETCH r.laboratorioSecundario " +
               "WHERE r.fechaSalida IS NULL")
        List<RegistroUso> findActiveWithRelations();

        // Registros en un rango de fechas con relaciones cargadas
        @Query("SELECT DISTINCT r FROM RegistroUso r " +
               "JOIN FETCH r.usuario u " +
               "JOIN FETCH r.laboratorio l " +
               "LEFT JOIN FETCH r.laboratorioSecundario " +
               "WHERE r.fechaEntrada BETWEEN :inicio AND :fin " +
               "ORDER BY r.fechaEntrada DESC")
        List<RegistroUso> findByFechaEntradaBetweenWithRelations(
                @Param("inicio") LocalDateTime inicio,
                @Param("fin") LocalDateTime fin);

        // Registros de un usuario en un rango de fechas
        @Query("SELECT DISTINCT r FROM RegistroUso r " +
               "JOIN FETCH r.usuario u " +
               "JOIN FETCH r.laboratorio l " +
               "LEFT JOIN FETCH r.laboratorioSecundario " +
               "WHERE u.id = :usuarioId AND r.fechaEntrada BETWEEN :inicio AND :fin " +
               "ORDER BY r.fechaEntrada DESC")
        List<RegistroUso> findByUsuarioIdAndFechaEntradaBetweenWithRelations(
                @Param("usuarioId") Long usuarioId,
                @Param("inicio") LocalDateTime inicio,
                @Param("fin") LocalDateTime fin);

        // Registros de un laboratorio en un rango de fechas
        @Query("SELECT DISTINCT r FROM RegistroUso r " +
               "JOIN FETCH r.usuario u " +
               "JOIN FETCH r.laboratorio l " +
               "LEFT JOIN FETCH r.laboratorioSecundario " +
               "WHERE l.id = :laboratorioId AND r.fechaEntrada BETWEEN :inicio AND :fin " +
               "ORDER BY r.fechaEntrada DESC")
        List<RegistroUso> findByLaboratorioIdAndFechaEntradaBetweenWithRelations(
                @Param("laboratorioId") Long laboratorioId,
                @Param("inicio") LocalDateTime inicio,
                @Param("fin") LocalDateTime fin);

        // Métodos legacy sin optimización (mantener compatibilidad)
        List<RegistroUso> findByUsuarioId(Long usuarioId);
        List<RegistroUso> findByLaboratorioId(Long laboratorioId);
        Optional<RegistroUso> findByUsuarioIdAndFechaSalidaIsNull(Long usuarioId);
        List<RegistroUso> findByFechaSalidaIsNull();
        List<RegistroUso> findByFechaEntradaBetween(LocalDateTime inicio, LocalDateTime fin);

        @Query("SELECT r FROM RegistroUso r WHERE r.usuario.id = :usuarioId AND r.fechaEntrada BETWEEN :inicio AND :fin")
        List<RegistroUso> findByUsuarioIdAndFechaEntradaBetween(
                        @Param("usuarioId") Long usuarioId,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

        @Query("SELECT r FROM RegistroUso r WHERE r.laboratorio.id = :laboratorioId AND r.fechaEntrada BETWEEN :inicio AND :fin")
        List<RegistroUso> findByLaboratorioIdAndFechaEntradaBetween(
                        @Param("laboratorioId") Long laboratorioId,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);
}
