package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.ElementoConectable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ElementoConectableRepository extends JpaRepository<ElementoConectable, Long> {

    List<ElementoConectable> findByProcesoId(Long procesoId);

    Optional<ElementoConectable> findByIdAndProcesoId(Long id, Long procesoId);
}
