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
    private final AuditoriaService auditoriaService;
    private final AccesoProcesoService accesoProcesoService;
    private final PermisoPoolService permisoPoolService;

    // @Lazy rompe el ciclo PoolService -> LaneService -> PoolService
    public PoolService(PoolRepository poolRepository, @Lazy LaneService laneService, ProcesoService procesoService,
                       AuditoriaService auditoriaService, AccesoProcesoService accesoProcesoService,
                       @Lazy PermisoPoolService permisoPoolService) {
        this.poolRepository = poolRepository;
        this.laneService = laneService;
        this.procesoService = procesoService;
        this.auditoriaService = auditoriaService;
        this.accesoProcesoService = accesoProcesoService;
        this.permisoPoolService = permisoPoolService;
    }

    @Transactional(readOnly = true)
    public List<Pool> listarPorProcesoYEmpresa(Long procesoId, Long empresaId) {
        accesoProcesoService.verificarLectura(procesoId, empresaId);
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

        Pool guardado = poolRepository.save(pool);
        auditoriaService.registrar("Pool", guardado.getId(), "CREAR", "Creado: " + guardado.getNombre());
        return guardado;
    }

    public Pool actualizarPool(Long id, PoolDTO datos, Long procesoId, Long empresaId) {
        Pool pool = obtenerPorIdYEmpresa(id, procesoId, empresaId);
        permisoPoolService.verificar(pool, "EDITAR");

        pool.setNombre(datos.getNombre().trim());
        pool.setTipo(datos.getTipo());

        Pool guardado = poolRepository.save(pool);
        auditoriaService.registrar("Pool", guardado.getId(), "EDITAR", "Editado: " + guardado.getNombre());
        return guardado;
    }

    public void eliminarPool(Long id, Long procesoId, Long empresaId) {
        Pool pool = obtenerPorIdYEmpresa(id, procesoId, empresaId);
        permisoPoolService.verificar(pool, "ELIMINAR");
        if (laneService.tieneLanes(id)) {
            throw new IllegalArgumentException("No se puede eliminar el pool porque tiene lanes: eliminelas primero");
        }
        auditoriaService.registrar("Pool", id, "ELIMINAR", "Pool eliminado");
        poolRepository.delete(pool);
    }

    private Proceso validarAccesoProceso(Long procesoId, Long empresaId) {
        return procesoService.obtenerPorIdYEmpresa(procesoId, empresaId);
    }
}
