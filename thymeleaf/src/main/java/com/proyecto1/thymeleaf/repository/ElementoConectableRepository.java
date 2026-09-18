package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.ElementoConectable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ElementoConectableRepository extends JpaRepository<ElementoConectable, Long> {

    List<ElementoConectable> findByProcesoId(Long procesoId);
    
    Optional<ElementoConectable> findByIdAndProcesoEmpresaId(Long id, Long empresaId);

    List<ElementoConectable> findByProcesoIdAndStatus(Long procesoId, Integer status);
}