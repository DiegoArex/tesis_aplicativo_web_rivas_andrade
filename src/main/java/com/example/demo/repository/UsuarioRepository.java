package com.example.demo.repository;

import com.example.demo.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByKeycloakId(String keycloakId);

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByCarrera(String carrera);

    List<Usuario> findByActivoTrue();

    // Encuentra todos los usuarios NO eliminados (activos en BD)
    @Query("SELECT u FROM Usuario u WHERE u.deletedAt IS NULL")
    List<Usuario> findAllActive();

    // Encuentra todos los usuarios eliminados (papelera)
    @Query("SELECT u FROM Usuario u WHERE u.deletedAt IS NOT NULL")
    List<Usuario> findAllDeleted();

    // Busca un usuario por ID solo si NO está eliminado
    @Query("SELECT u FROM Usuario u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<Usuario> findByIdAndNotDeleted(@Param("id") Long id);

    // Busca un usuario por username solo si NO está eliminado
    @Query("SELECT u FROM Usuario u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<Usuario> findByUsernameAndNotDeleted(@Param("username") String username);

    // Busca un usuario por keycloakId solo si NO está eliminado
    @Query("SELECT u FROM Usuario u WHERE u.keycloakId = :keycloakId AND u.deletedAt IS NULL")
    Optional<Usuario> findByKeycloakIdAndNotDeleted(@Param("keycloakId") String keycloakId);

    // Busca usuarios por carrera solo si NO están eliminados
    @Query("SELECT u FROM Usuario u WHERE u.carrera = :carrera AND u.deletedAt IS NULL")
    List<Usuario> findByCarreraAndNotDeleted(@Param("carrera") String carrera);

    // Busca usuarios activos (activo=true) y NO eliminados
    @Query("SELECT u FROM Usuario u WHERE u.activo = true AND u.deletedAt IS NULL")
    List<Usuario> findByActivoTrueAndNotDeleted();
}
