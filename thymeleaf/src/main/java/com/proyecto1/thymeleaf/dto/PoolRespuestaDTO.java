package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Pool;

public record PoolRespuestaDTO(Long id, String nombre, Pool.TipoPool tipo, Long procesoId) {

    public static PoolRespuestaDTO desde(Pool p) {
        return new PoolRespuestaDTO(p.getId(), p.getNombre(), p.getTipo(),
                p.getProceso() == null ? null : p.getProceso().getId());
    }
}
