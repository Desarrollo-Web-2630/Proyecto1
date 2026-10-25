package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PoolRepository extends JpaRepository<Pool, Long> {

@Query("SELECT p FROM Pool p WHERE p.proceso.id = :procesoId AND p.status = 0")
List<Pool> findByProcesoId(@Param("procesoId") Long procesoId);

@Query("SELECT p FROM Pool p WHERE p.id = :id AND p.proceso.id = :procesoId " +
        "AND p.proceso.empresa.id = :empresaId AND p.status = 0")
Optional<Pool> findByIdAndProcesoIdAndEmpresaId(@Param("id") Long id,
                                                @Param("procesoId") Long procesoId,
                                                @Param("empresaId") Long empresaId);
}
