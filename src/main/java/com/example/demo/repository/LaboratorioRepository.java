package com.example.demo.repository;

import com.example.demo.entity.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LaboratorioRepository extends JpaRepository<Laboratorio, Long> {

    Optional<Laboratorio> findByNombre(String nombre);

    List<Laboratorio> findByActivoTrue();

    List<Laboratorio> findByUbicacionContainingIgnoreCase(String ubicacion);

    // Encuentra todos los laboratorios NO eliminados
    @Query("SELECT l FROM Laboratorio l WHERE l.deletedAt IS NULL")
    List<Laboratorio> findAllActive();

    // Encuentra todos los laboratorios eliminados (papelera)
    @Query("SELECT l FROM Laboratorio l WHERE l.deletedAt IS NOT NULL")
    List<Laboratorio> findAllDeleted();

    // Busca un laboratorio por ID solo si NO está eliminado
    @Query("SELECT l FROM Laboratorio l WHERE l.id = :id AND l.deletedAt IS NULL")
    Optional<Laboratorio> findByIdAndNotDeleted(@Param("id") Long id);

    // Busca un laboratorio por nombre solo si NO está eliminado
    @Query("SELECT l FROM Laboratorio l WHERE l.nombre = :nombre AND l.deletedAt IS NULL")
    Optional<Laboratorio> findByNombreAndNotDeleted(@Param("nombre") String nombre);

    // Busca laboratorios activos (activo=true) y NO eliminados
    @Query("SELECT l FROM Laboratorio l WHERE l.activo = true AND l.deletedAt IS NULL")
    List<Laboratorio> findByActivoTrueAndNotDeleted();

    // Busca laboratorios por ubicación solo si NO están eliminados
    @Query("SELECT l FROM Laboratorio l WHERE LOWER(l.ubicacion) LIKE LOWER(CONCAT('%', :ubicacion, '%')) AND l.deletedAt IS NULL")
    List<Laboratorio> findByUbicacionContainingIgnoreCaseAndNotDeleted(@Param("ubicacion") String ubicacion);
}
