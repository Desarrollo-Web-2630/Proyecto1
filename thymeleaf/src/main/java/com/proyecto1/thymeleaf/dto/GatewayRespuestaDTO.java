package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Gateway;

public record GatewayRespuestaDTO(Long id, String nombre, Gateway.TipoGateway tipo, Long procesoId) {

    public static GatewayRespuestaDTO desde(Gateway g) {
        return new GatewayRespuestaDTO(g.getId(), g.getNombre(), g.getTipo(),
                g.getProceso() == null ? null : g.getProceso().getId());
    }
}
