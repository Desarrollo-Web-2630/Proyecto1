package com.proyecto1.thymeleaf.services;

import java.util.List;

import com.proyecto1.thymeleaf.dto.ProcesoUsoDTO;

/**
 * Responde en que procesos esta usado un rol de proceso.
 *
 * HU-19 pide no eliminar un rol que este asignado a una lane o a una actividad
 * y decir en que procesos esta usado; HU-20 pide mostrar ese uso en el
 * listado. La relacion rol -> lane la crea HU-22 (Pool y Lane), que todavia no
 * existe en el backend, por eso la consulta vive detras de esta interfaz: el
 * servicio de roles ya depende de ella y cuando exista Lane solo hay que
 * reemplazar la implementacion por defecto (UsoRolProcesoSinLanes).
 */
public interface UsoRolProceso {

    /**
     * @return los procesos de la empresa donde el rol esta en uso (sin
     *         repetidos); vacio si el rol se puede eliminar.
     */
    List<ProcesoUsoDTO> procesosDondeSeUsa(Long rolId, Long empresaId);
}
