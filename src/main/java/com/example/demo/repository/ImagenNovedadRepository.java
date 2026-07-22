package com.example.demo.repository;

import com.example.demo.entity.ImagenNovedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ImagenNovedadRepository extends JpaRepository<ImagenNovedad, Long> {

    List<ImagenNovedad> findByNovedadId(Long novedadId);

    void deleteByNovedadId(Long novedadId);

    long countByNovedadId(Long novedadId);

    @Query("SELECT i.novedad.id, COUNT(i) FROM ImagenNovedad i WHERE i.novedad.id IN :ids GROUP BY i.novedad.id")
    List<Object[]> countGroupByNovedadIdIn(@Param("ids") Collection<Long> ids);
}
