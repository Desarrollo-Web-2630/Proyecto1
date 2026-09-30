package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Actividad;

public record ActividadRespuestaDTO(Long id, String nombre, String tipoActividad, Long laneId,
                                    Integer posicionX, Integer posicionY, Long procesoId) {

    public static ActividadRespuestaDTO desde(Actividad a) {
        return new ActividadRespuestaDTO(a.getId(), a.getNombre(), a.getTipoActividad(), a.getLaneId(),
                a.getPosicionX(), a.getPosicionY(), a.getProceso() == null ? null : a.getProceso().getId());
    }
}
