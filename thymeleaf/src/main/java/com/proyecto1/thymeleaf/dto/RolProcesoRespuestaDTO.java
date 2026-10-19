package com.proyecto1.thymeleaf.dto;

import java.util.List;

import com.proyecto1.thymeleaf.model.RolProceso;

public record RolProcesoRespuestaDTO(Long id, String nombre, String descripcion,
                                     List<ProcesoUsoDTO> procesosEnUso, boolean eliminable) {

    public static RolProcesoRespuestaDTO desde(RolProceso rol, List<ProcesoUsoDTO> procesosEnUso) {
        return new RolProcesoRespuestaDTO(rol.getId(), rol.getNombre(), rol.getDescripcion(),
                procesosEnUso, procesosEnUso.isEmpty());
    }
}
