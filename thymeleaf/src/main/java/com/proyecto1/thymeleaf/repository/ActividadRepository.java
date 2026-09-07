package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    List<Actividad> findByProcesoId(Long procesoId);

    List<Actividad> findByProcesoIdAndLaneId(Long procesoId, Long laneId);

    boolean existsByNombreAndProcesoId(String nombre, Long procesoId);

    // Igual que el anterior pero ignorando la propia actividad, para validar el
    // nombre unico cuando se esta editando y no creando.
    boolean existsByNombreAndProcesoIdAndIdNot(String nombre, Long procesoId, Long id);

    Optional<Actividad> findByIdAndProcesoEmpresaId(Long id, Long empresaId);
}
