package com.proyecto1.thymeleaf.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.proyecto1.thymeleaf.dto.ProcesoUsoDTO;

// HU-19: un rol esta en uso si alguna lane activa lo referencia.
@Component
public class UsoRolProcesoConLanes implements UsoRolProceso {

    private final LaneService laneService;

    public UsoRolProcesoConLanes(LaneService laneService) {
        this.laneService = laneService;
    }

    @Override
    public List<ProcesoUsoDTO> procesosDondeSeUsa(Long rolId, Long empresaId) {
        return laneService.buscarProcesosDondeSeUsaRol(rolId).stream()
                .map(fila -> new ProcesoUsoDTO((Long) fila[0], (String) fila[1]))
                .toList();
    }
}
