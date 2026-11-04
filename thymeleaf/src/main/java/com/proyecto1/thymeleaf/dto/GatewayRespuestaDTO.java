package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Gateway;

public record GatewayRespuestaDTO(Long id, String nombre, Gateway.TipoGateway tipo,
                                Integer posicionX, Integer posicionY, Long procesoId) {

    public static GatewayRespuestaDTO desde(Gateway g) {
        return new GatewayRespuestaDTO(g.getId(), g.getNombre(), g.getTipo(),
                g.getPosicionX(), g.getPosicionY(),
                g.getProceso() == null ? null : g.getProceso().getId());
    }
}
