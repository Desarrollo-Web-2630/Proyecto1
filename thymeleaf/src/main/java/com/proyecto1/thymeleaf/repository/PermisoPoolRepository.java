package com.proyecto1.thymeleaf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto1.thymeleaf.model.PermisoPool;

public interface PermisoPoolRepository extends JpaRepository<PermisoPool, Long> {

    List<PermisoPool> findByPoolId(Long poolId);

    Optional<PermisoPool> findByPoolIdAndRolAcceso(Long poolId, String rolAcceso);
}
