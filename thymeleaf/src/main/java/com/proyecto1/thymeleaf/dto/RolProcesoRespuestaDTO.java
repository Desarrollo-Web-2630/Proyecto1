package com.proyecto1.thymeleaf.dto;

import java.util.List;

import com.proyecto1.thymeleaf.model.RolProceso;

/**
 * Lo que la API devuelve de un rol de proceso (HU-20): nombre y descripcion,
 * los procesos donde esta usado y si se puede eliminar. La entidad no sale
 * nunca: arrastra la empresa y sus usuarios.
 */
public record RolProcesoRespuestaDTO(Long id, String nombre, String descripcion,
                                     List<ProcesoUsoDTO> procesosEnUso, boolean eliminable) {

    public static RolProcesoRespuestaDTO desde(RolProceso rol, List<ProcesoUsoDTO> procesosEnUso) {
        return new RolProcesoRespuestaDTO(rol.getId(), rol.getNombre(), rol.getDescripcion(),
                procesosEnUso, procesosEnUso.isEmpty());
    }
}
