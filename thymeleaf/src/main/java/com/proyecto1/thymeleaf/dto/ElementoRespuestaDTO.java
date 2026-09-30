package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.ElementoConectable;

public record ElementoRespuestaDTO(Long id, String nombre, Integer posicionX, Integer posicionY, Long procesoId) {

    public static ElementoRespuestaDTO desde(ElementoConectable e) {
        return new ElementoRespuestaDTO(e.getId(), e.getNombre(), e.getPosicionX(), e.getPosicionY(),
                e.getProceso() == null ? null : e.getProceso().getId());
    }
}
