package com.proyecto1.thymeleaf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto1.thymeleaf.model.HistorialCambio;

public interface HistorialCambioRepository extends JpaRepository<HistorialCambio, Long> {

    @Query("SELECT h FROM HistorialCambio h WHERE h.empresaId = :empresaId " +
           "AND h.entidad = :entidad AND h.entidadId = :entidadId ORDER BY h.fecha DESC, h.id DESC")
    List<HistorialCambio> findByEmpresaYEntidad(@Param("empresaId") Long empresaId,
                                                @Param("entidad") String entidad,
                                                @Param("entidadId") Long entidadId);

    @Query("SELECT h FROM HistorialCambio h WHERE h.empresaId = :empresaId ORDER BY h.fecha DESC, h.id DESC")
    List<HistorialCambio> findByEmpresa(@Param("empresaId") Long empresaId);
}
