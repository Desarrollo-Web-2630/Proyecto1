package com.proyecto1.thymeleaf.services;

import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto1.thymeleaf.dto.PoolDTO;
import com.proyecto1.thymeleaf.model.Pool;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.PoolRepository;

// HU-21: logica de negocio de los pools.
@Service
@Transactional
public class PoolService {

    private final PoolRepository poolRepository;
    private final LaneService laneService;
    private final ProcesoService procesoService;

    // @Lazy rompe el ciclo PoolService -> LaneService -> PoolService
    public PoolService(PoolRepository poolRepository, @Lazy LaneService laneService, ProcesoService procesoService) {
        this.poolRepository = poolRepository;
        this.laneService = laneService;
        this.procesoService = procesoService;
    }

    @Transactional(readOnly = true)
    public List<Pool> listarPorProcesoYEmpresa(Long procesoId, Long empresaId) {
        validarAccesoProceso(procesoId, empresaId);
        return poolRepository.findByProcesoId(procesoId);
    }

    @Transactional(readOnly = true)
    public Pool obtenerPorIdYEmpresa(Long id, Long procesoId, Long empresaId) {
        return poolRepository.findByIdAndProcesoIdAndEmpresaId(id, procesoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El pool no existe o no pertenece a su empresa"));
    }

    public Pool crearPool(PoolDTO datos, Long procesoId, Long empresaId) {
        Proceso proceso = validarAccesoProceso(procesoId, empresaId);

        Pool pool = new Pool();
        pool.setNombre(datos.getNombre().trim());
        pool.setTipo(datos.getTipo());
        pool.setProceso(proceso);

        return poolRepository.save(pool);
    }

    public Pool actualizarPool(Long id, PoolDTO datos, Long procesoId, Long empresaId) {
        Pool pool = obtenerPorIdYEmpresa(id, procesoId, empresaId);

        pool.setNombre(datos.getNombre().trim());
        pool.setTipo(datos.getTipo());

        return poolRepository.save(pool);
    }

    public void eliminarPool(Long id, Long procesoId, Long empresaId) {
        Pool pool = obtenerPorIdYEmpresa(id, procesoId, empresaId);
        if (laneService.tieneLanes(id)) {
            throw new IllegalArgumentException("No se puede eliminar el pool porque tiene lanes: eliminelas primero");
        }
        poolRepository.delete(pool);
    }

    private Proceso validarAccesoProceso(Long procesoId, Long empresaId) {
        return procesoService.obtenerPorIdYEmpresa(procesoId, empresaId);
    }
}
