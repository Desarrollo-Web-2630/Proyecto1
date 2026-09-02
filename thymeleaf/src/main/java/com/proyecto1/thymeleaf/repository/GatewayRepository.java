package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Gateway;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GatewayRepository extends JpaRepository<Gateway, Long> {

    List<Gateway> findByProcesoId(Long procesoId);

    Optional<Gateway> findByIdAndProcesoEmpresaId(Long id, Long empresaId);
}
