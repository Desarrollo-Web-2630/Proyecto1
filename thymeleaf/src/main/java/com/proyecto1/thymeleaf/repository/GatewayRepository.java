package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Gateway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GatewayRepository extends JpaRepository<Gateway, Long> {

@Query("SELECT g FROM Gateway g WHERE g.proceso.id = :procesoId AND g.status = 0")
List<Gateway> findByProcesoId(@Param("procesoId") Long procesoId);

@Query("SELECT g FROM Gateway g WHERE g.id = :id " +
        "AND g.proceso.empresa.id = :empresaId AND g.status = 0")
Optional<Gateway> findByIdAndProcesoEmpresaId(@Param("id") Long id,
                                                @Param("empresaId") Long empresaId);
}
