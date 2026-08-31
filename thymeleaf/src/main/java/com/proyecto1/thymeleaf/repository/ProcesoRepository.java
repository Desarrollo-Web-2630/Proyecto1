package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Proceso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcesoRepository extends JpaRepository<Proceso, Long> {

    List<Proceso> findByEmpresaId(Long empresaId);

    boolean existsByNombreAndEmpresaId(String nombre, Long empresaId);
}
