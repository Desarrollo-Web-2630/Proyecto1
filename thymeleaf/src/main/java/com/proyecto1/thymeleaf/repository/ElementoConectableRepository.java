package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.ElementoConectable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ElementoConectableRepository extends JpaRepository<ElementoConectable, Long> {

@Query("SELECT e FROM ElementoConectable e WHERE e.proceso.id = :procesoId AND e.status = 0")
List<ElementoConectable> findByProcesoId(@Param("procesoId") Long procesoId);

@Query("SELECT e FROM ElementoConectable e WHERE e.id = :id " +
        "AND e.proceso.empresa.id = :empresaId AND e.status = 0")
Optional<ElementoConectable> findByIdAndProcesoEmpresaId(@Param("id") Long id,
                                                            @Param("empresaId") Long empresaId);
}






