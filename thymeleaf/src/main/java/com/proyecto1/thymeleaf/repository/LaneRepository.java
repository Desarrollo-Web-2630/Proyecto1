package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Lane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LaneRepository extends JpaRepository<Lane, Long> {

@Query("SELECT l FROM Lane l WHERE l.pool.id = :poolId AND l.status = 0 ORDER BY l.orden")
List<Lane> findByPoolId(@Param("poolId") Long poolId);

@Query("SELECT l FROM Lane l WHERE l.id = :id AND l.pool.id = :poolId " +
        "AND l.pool.proceso.empresa.id = :empresaId AND l.status = 0")
Optional<Lane> findByIdAndPoolIdAndEmpresaId(@Param("id") Long id,
                                             @Param("poolId") Long poolId,
                                             @Param("empresaId") Long empresaId);

@Query("SELECT l FROM Lane l WHERE l.id = :id AND l.pool.proceso.id = :procesoId AND l.status = 0")
Optional<Lane> findByIdAndProcesoId(@Param("id") Long id, @Param("procesoId") Long procesoId);

@Query("SELECT DISTINCT l.pool.proceso.id, l.pool.proceso.nombre FROM Lane l " +
        "WHERE l.rolProceso.id = :rolId AND l.status = 0")
List<Object[]> buscarProcesosDondeSeUsaRol(@Param("rolId") Long rolId);
}
