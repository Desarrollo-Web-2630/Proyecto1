package com.proyecto1.thymeleaf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto1.thymeleaf.model.ProcesoCompartido;

public interface ProcesoCompartidoRepository extends JpaRepository<ProcesoCompartido, Long> {

    List<ProcesoCompartido> findByProcesoId(Long procesoId);

    boolean existsByProcesoIdAndEmpresaInvitadaIdAndActivoTrue(Long procesoId, Long empresaInvitadaId);

    @Query("SELECT c.proceso.id FROM ProcesoCompartido c " +
           "WHERE c.empresaInvitadaId = :empresaId AND c.activo = true")
    List<Long> findIdsCompartidosCon(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM ProcesoCompartido c WHERE c.proceso.id = :procesoId AND c.activo = true")
    List<ProcesoCompartido> findActivosByProcesoId(@Param("procesoId") Long procesoId);
}
