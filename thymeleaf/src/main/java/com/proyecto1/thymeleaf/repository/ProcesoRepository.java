package com.proyecto1.thymeleaf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto1.thymeleaf.model.Proceso;

public interface ProcesoRepository extends JpaRepository<Proceso, Long> {

@Query("SELECT p FROM Proceso p WHERE p.empresa.id = :empresaId AND p.status = 0")
List<Proceso> findByEmpresaId(@Param("empresaId") Long empresaId);

@Query("SELECT p FROM Proceso p WHERE p.empresa.id = :empresaId " +
        "AND p.estado <> :estado AND p.status = 0")
List<Proceso> findByEmpresaIdAndEstadoNot(@Param("empresaId") Long empresaId,
                                            @Param("estado") Proceso.EstadoProceso estado);

@Query("SELECT p FROM Proceso p WHERE p.id = :id AND p.empresa.id = :empresaId AND p.status = 0")
Optional<Proceso> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

@Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Proceso p " +
        "WHERE LOWER(p.nombre) = LOWER(:nombre) AND p.empresa.id = :empresaId AND p.status = 0")
boolean existsByNombreAndEmpresaId(@Param("nombre") String nombre, @Param("empresaId") Long empresaId);

@Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Proceso p " +
        "WHERE LOWER(p.nombre) = LOWER(:nombre) AND p.empresa.id = :empresaId " +
        "AND p.id <> :idActual AND p.status = 0")
boolean existsByNombreAndEmpresaIdAndIdNot(@Param("nombre") String nombre,
                                            @Param("empresaId") Long empresaId,
                                            @Param("idActual") Long idActual);

@Query("SELECT p FROM Proceso p WHERE p.empresa.id = :empresaId AND p.status = 0 " +
        "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
        "AND (:estado IS NULL OR p.estado = :estado) " +
        "AND (:incluirInactivos = true OR p.estado <> :inactivo) " +
        "AND (:categoria IS NULL OR LOWER(p.categoria) = LOWER(:categoria))")
Page<Proceso> buscar(@Param("empresaId") Long empresaId,
                    @Param("nombre") String nombre,
                    @Param("estado") Proceso.EstadoProceso estado,
                    @Param("incluirInactivos") boolean incluirInactivos,
                    @Param("inactivo") Proceso.EstadoProceso inactivo,
                    @Param("categoria") String categoria,
                    Pageable pageable);

}