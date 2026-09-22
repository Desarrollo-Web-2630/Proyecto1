package com.proyecto1.thymeleaf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto1.thymeleaf.model.Actividad;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    List<Actividad> findByProcesoId(Long procesoId);

    List<Actividad> findByProcesoIdAndLaneId(Long procesoId, Long laneId);

    boolean existsByNombreAndProcesoId(String nombre, Long procesoId);

    boolean existsByNombreAndProcesoIdAndIdNot(String nombre, Long procesoId, Long id);

    Optional<Actividad> findByIdAndProcesoEmpresaId(Long id, Long empresaId);
}
