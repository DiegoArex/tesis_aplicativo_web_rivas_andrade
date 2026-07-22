package com.example.demo.repository;

import com.example.demo.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByLaboratorioId(Long laboratorioId);

    Optional<Equipo> findByCodigo(String codigo);

    List<Equipo> findByEstado(String estado);

    List<Equipo> findByTipo(String tipo);

    List<Equipo> findByLaboratorioIdAndEstado(Long laboratorioId, String estado);

    // Encuentra todos los equipos NO eliminados
    @Query("SELECT e FROM Equipo e WHERE e.deletedAt IS NULL")
    List<Equipo> findAllActive();

    // Encuentra todos los equipos eliminados (papelera)
    @Query("SELECT e FROM Equipo e WHERE e.deletedAt IS NOT NULL")
    List<Equipo> findAllDeleted();

    // Busca un equipo por ID solo si NO está eliminado
    @Query("SELECT e FROM Equipo e WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<Equipo> findByIdAndNotDeleted(@Param("id") Long id);

    // Busca un equipo por código solo si NO está eliminado
    @Query("SELECT e FROM Equipo e WHERE e.codigo = :codigo AND e.deletedAt IS NULL")
    Optional<Equipo> findByCodigoAndNotDeleted(@Param("codigo") String codigo);

    // Busca equipos por laboratorio solo si NO están eliminados
    @Query("SELECT e FROM Equipo e WHERE e.laboratorio.id = :laboratorioId AND e.deletedAt IS NULL")
    List<Equipo> findByLaboratorioIdAndNotDeleted(@Param("laboratorioId") Long laboratorioId);

    // Busca equipos por estado solo si NO están eliminados
    @Query("SELECT e FROM Equipo e WHERE e.estado = :estado AND e.deletedAt IS NULL")
    List<Equipo> findByEstadoAndNotDeleted(@Param("estado") String estado);

    // Busca equipos por tipo solo si NO están eliminados
    @Query("SELECT e FROM Equipo e WHERE e.tipo = :tipo AND e.deletedAt IS NULL")
    List<Equipo> findByTipoAndNotDeleted(@Param("tipo") String tipo);

    // Busca equipos por laboratorio y estado solo si NO están eliminados
    @Query("SELECT e FROM Equipo e WHERE e.laboratorio.id = :laboratorioId AND e.estado = :estado AND e.deletedAt IS NULL")
    List<Equipo> findByLaboratorioIdAndEstadoAndNotDeleted(@Param("laboratorioId") Long laboratorioId,
            @Param("estado") String estado);
}
