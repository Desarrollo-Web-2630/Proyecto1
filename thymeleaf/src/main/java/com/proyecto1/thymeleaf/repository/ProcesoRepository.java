package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Proceso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcesoRepository extends JpaRepository<Proceso, Long> {

    List<Proceso> findByEmpresaId(Long empresaId);

    boolean existsByNombreAndEmpresaId(String nombre, Long empresaId);

    // Igual que el anterior pero ignorando el propio proceso, para validar el
    // nombre unico cuando se esta editando y no creando.
    boolean existsByNombreAndEmpresaIdAndIdNot(String nombre, Long empresaId, Long id);

    Optional<Proceso> findByIdAndEmpresaId(Long procesoId, Long empresaId);
}
