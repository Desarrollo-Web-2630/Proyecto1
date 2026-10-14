package com.proyecto1.thymeleaf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto1.thymeleaf.model.RolProceso;

public interface RolProcesoRepository extends JpaRepository<RolProceso, Long> {

// Aislamiento por empresa: todas las consultas filtran por empresa en el propio JPQL.
@Query("SELECT r FROM RolProceso r WHERE r.empresa.id = :empresaId AND r.status = 0 ORDER BY r.nombre")
List<RolProceso> findByEmpresaId(@Param("empresaId") Long empresaId);

@Query("SELECT r FROM RolProceso r WHERE r.id = :id AND r.empresa.id = :empresaId AND r.status = 0")
Optional<RolProceso> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

@Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RolProceso r " +
        "WHERE LOWER(r.nombre) = LOWER(:nombre) AND r.empresa.id = :empresaId AND r.status = 0")
boolean existsByNombreAndEmpresaId(@Param("nombre") String nombre, @Param("empresaId") Long empresaId);

@Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RolProceso r " +
        "WHERE LOWER(r.nombre) = LOWER(:nombre) AND r.empresa.id = :empresaId " +
        "AND r.id <> :idActual AND r.status = 0")
boolean existsByNombreAndEmpresaIdAndIdNot(@Param("nombre") String nombre,
                                            @Param("empresaId") Long empresaId,
                                            @Param("idActual") Long idActual);

// HU-20: busqueda por nombre y paginacion; el filtro solo se aplica si viene informado
@Query("SELECT r FROM RolProceso r WHERE r.empresa.id = :empresaId AND r.status = 0 " +
        "AND (:nombre IS NULL OR LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))")
Page<RolProceso> buscar(@Param("empresaId") Long empresaId,
                        @Param("nombre") String nombre,
                        Pageable pageable);
}
