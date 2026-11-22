package com.proyecto1.thymeleaf.dto;

import java.time.Instant;

import com.proyecto1.thymeleaf.model.HistorialCambio;

public record HistorialCambioDTO(Long id, String entidad, Long entidadId, String accion,
                                 String descripcion, String usuarioCorreo, Instant fecha) {

    public static HistorialCambioDTO desde(HistorialCambio cambio) {
        return new HistorialCambioDTO(cambio.getId(), cambio.getEntidad(), cambio.getEntidadId(),
                cambio.getAccion(), cambio.getDescripcion(), cambio.getUsuarioCorreo(), cambio.getFecha());
    }
}
