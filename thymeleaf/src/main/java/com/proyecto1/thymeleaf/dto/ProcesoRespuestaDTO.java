package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Proceso;

public record ProcesoRespuestaDTO(Long id, String nombre, String descripcion, String categoria,
                                  Proceso.EstadoProceso estado, Long empresaId) {

    public static ProcesoRespuestaDTO desde(Proceso p) {
        return new ProcesoRespuestaDTO(p.getId(), p.getNombre(), p.getDescripcion(), p.getCategoria(),
                p.getEstado(), p.getEmpresa() == null ? null : p.getEmpresa().getId());
    }
}
