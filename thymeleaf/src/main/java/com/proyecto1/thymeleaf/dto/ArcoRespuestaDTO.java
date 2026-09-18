package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Arco;

public record ArcoRespuestaDTO(Long id, String nombre, String condicion,
                               Long origenId, String origenNombre,
                               Long destinoId, String destinoNombre,
                               Long procesoId) {

    public static ArcoRespuestaDTO desde(Arco a) {
        return new ArcoRespuestaDTO(a.getId(), a.getNombre(), a.getCondicion(),
                a.getOrigen() == null ? null : a.getOrigen().getId(),
                a.getOrigen() == null ? null : a.getOrigen().getNombre(),
                a.getDestino() == null ? null : a.getDestino().getId(),
                a.getDestino() == null ? null : a.getDestino().getNombre(),
                a.getProceso() == null ? null : a.getProceso().getId());
    }
}
