package com.proyecto1.thymeleaf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.proyecto1.thymeleaf.model.Proceso;

public interface ProcesoRepository extends JpaRepository<Proceso, Long>, JpaSpecificationExecutor<Proceso> {

    List<Proceso> findByEmpresaId(Long empresaId);

    // Listado "activo por defecto" (HU-07): todo menos los INACTIVO
    List<Proceso> findByEmpresaIdAndEstadoNot(Long empresaId, Proceso.EstadoProceso estado);

    boolean existsByNombreAndEmpresaId(String nombre, Long empresaId);

    boolean existsByNombreAndEmpresaIdAndIdNot(String nombre, Long empresaId, Long id);

    Optional<Proceso> findByIdAndEmpresaId(Long procesoId, Long empresaId);
}
