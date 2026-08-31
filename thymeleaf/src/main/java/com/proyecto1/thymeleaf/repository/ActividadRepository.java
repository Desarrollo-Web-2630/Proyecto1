package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    List<Actividad> findByProcesoId(Long procesoId);

    boolean existsByNombreAndProcesoId(String nombre, Long procesoId);
}
