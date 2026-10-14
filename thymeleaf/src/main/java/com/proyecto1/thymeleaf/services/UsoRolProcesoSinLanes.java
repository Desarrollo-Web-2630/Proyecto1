package com.proyecto1.thymeleaf.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.proyecto1.thymeleaf.dto.ProcesoUsoDTO;

/**
 * Implementacion por defecto de UsoRolProceso mientras no exista la entidad
 * Lane (HU-22): sin lanes, ningun rol esta asignado a nada, asi que siempre
 * responde que el rol no esta en uso.
 *
 * TODO (HU-22): cuando exista Lane con su rol, reemplazar el cuerpo por una
 * consulta que devuelva los procesos cuyas lanes usan el rol.
 */
@Component
public class UsoRolProcesoSinLanes implements UsoRolProceso {

    @Override
    public List<ProcesoUsoDTO> procesosDondeSeUsa(Long rolId, Long empresaId) {
        return List.of();
    }
}
