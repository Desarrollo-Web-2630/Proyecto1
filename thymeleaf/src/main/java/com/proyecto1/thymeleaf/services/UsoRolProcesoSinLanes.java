package com.proyecto1.thymeleaf.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.proyecto1.thymeleaf.dto.ProcesoUsoDTO;

@Component
public class UsoRolProcesoSinLanes implements UsoRolProceso {

    @Override
    public List<ProcesoUsoDTO> procesosDondeSeUsa(Long rolId, Long empresaId) {
        return List.of();
    }
}
