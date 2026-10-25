package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

@Query("SELECT m FROM Mensaje m WHERE m.proceso.id = :procesoId AND m.status = 0")
List<Mensaje> findByProcesoId(@Param("procesoId") Long procesoId);

@Query("SELECT m FROM Mensaje m WHERE m.id = :id AND m.proceso.id = :procesoId " +
        "AND m.proceso.empresa.id = :empresaId AND m.status = 0")
Optional<Mensaje> findByIdAndProcesoIdAndEmpresaId(@Param("id") Long id,
                                                   @Param("procesoId") Long procesoId,
                                                   @Param("empresaId") Long empresaId);
}
