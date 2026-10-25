package com.proyecto1.thymeleaf.services;

import java.util.List;

import com.proyecto1.thymeleaf.dto.ProcesoUsoDTO;

// Implementacion historica previa a la existencia de Lane; ya no se registra
// como bean (ver UsoRolProcesoConLanes), se conserva solo para su test.
public class UsoRolProcesoSinLanes implements UsoRolProceso {

    @Override
    public List<ProcesoUsoDTO> procesosDondeSeUsa(Long rolId, Long empresaId) {
        return List.of();
    }
}
