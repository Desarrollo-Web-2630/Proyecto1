package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Lane;

public record LaneRespuestaDTO(Long id, String nombre, Integer orden, Long poolId,
                               Long rolProcesoId, String rolProcesoNombre) {

    public static LaneRespuestaDTO desde(Lane l) {
        return new LaneRespuestaDTO(l.getId(), l.getNombre(), l.getOrden(),
                l.getPool() == null ? null : l.getPool().getId(),
                l.getRolProceso() == null ? null : l.getRolProceso().getId(),
                l.getRolProceso() == null ? null : l.getRolProceso().getNombre());
    }
}
