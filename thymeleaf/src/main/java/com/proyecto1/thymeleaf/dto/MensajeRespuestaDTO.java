package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Mensaje;

public record MensajeRespuestaDTO(Long id, String nombre, String claveCorrelacion, Long procesoId,
                                  Long poolOrigenId, Long poolDestinoId, Long elementoOrigenId,
                                  Mensaje.TipoMensaje tipoMensaje, Mensaje.VarianteMensaje variante,
                                  String destinoExterno, String payload, Mensaje.PoliticaFallo politicaFallo) {

    public static MensajeRespuestaDTO desde(Mensaje m) {
        return new MensajeRespuestaDTO(m.getId(), m.getNombre(), m.getClaveCorrelacion(),
                m.getProceso() == null ? null : m.getProceso().getId(),
                m.getPoolOrigen() == null ? null : m.getPoolOrigen().getId(),
                m.getPoolDestino() == null ? null : m.getPoolDestino().getId(),
                m.getElementoOrigen() == null ? null : m.getElementoOrigen().getId(),
                m.getTipoMensaje(), m.getVariante(), m.getDestinoExterno(), m.getPayload(), m.getPoliticaFallo());
    }
}
