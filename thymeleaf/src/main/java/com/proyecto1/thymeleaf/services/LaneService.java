package com.proyecto1.thymeleaf.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto1.thymeleaf.dto.LaneDTO;
import com.proyecto1.thymeleaf.model.Lane;
import com.proyecto1.thymeleaf.model.Pool;
import com.proyecto1.thymeleaf.model.RolProceso;
import com.proyecto1.thymeleaf.repository.LaneRepository;

// HU-22: logica de negocio de las lanes.
@Service
@Transactional
public class LaneService {

    private final LaneRepository laneRepository;
    private final PoolService poolService;
    private final RolProcesoService rolProcesoService;

    public LaneService(LaneRepository laneRepository, PoolService poolService, RolProcesoService rolProcesoService) {
        this.laneRepository = laneRepository;
        this.poolService = poolService;
        this.rolProcesoService = rolProcesoService;
    }

    @Transactional(readOnly = true)
    public List<Lane> listarPorPoolYEmpresa(Long poolId, Long procesoId, Long empresaId) {
        poolService.obtenerPorIdYEmpresa(poolId, procesoId, empresaId);
        return laneRepository.findByPoolId(poolId);
    }

    @Transactional(readOnly = true)
    public Lane obtenerPorIdYEmpresa(Long id, Long poolId, Long empresaId) {
        return laneRepository.findByIdAndPoolIdAndEmpresaId(id, poolId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La lane no existe o no pertenece a su empresa"));
    }

    public Lane crearLane(LaneDTO datos, Long poolId, Long procesoId, Long empresaId) {
        Pool pool = poolService.obtenerPorIdYEmpresa(poolId, procesoId, empresaId);
        RolProceso rol = rolProcesoService.obtenerEntidadPorIdYEmpresa(datos.getRolProcesoId(), empresaId);

        Lane lane = new Lane();
        lane.setNombre(datos.getNombre().trim());
        lane.setRolProceso(rol);
        lane.setPool(pool);
        lane.setOrden(laneRepository.findByPoolId(poolId).size());

        return laneRepository.save(lane);
    }

    public Lane actualizarLane(Long id, LaneDTO datos, Long poolId, Long empresaId) {
        Lane lane = obtenerPorIdYEmpresa(id, poolId, empresaId);
        RolProceso rol = rolProcesoService.obtenerEntidadPorIdYEmpresa(datos.getRolProcesoId(), empresaId);

        lane.setNombre(datos.getNombre().trim());
        lane.setRolProceso(rol);

        return laneRepository.save(lane);
    }

    public void eliminarLane(Long id, Long poolId, Long empresaId) {
        Lane lane = obtenerPorIdYEmpresa(id, poolId, empresaId);
        laneRepository.delete(lane);
    }

    // HU-22: recibe las lanes del pool en el nuevo orden, de arriba hacia abajo
    public List<Lane> reordenarLanes(Long poolId, List<Long> lanesIds, Long procesoId, Long empresaId) {
        poolService.obtenerPorIdYEmpresa(poolId, procesoId, empresaId);
        List<Lane> lanesActuales = laneRepository.findByPoolId(poolId);

        boolean mismaCantidad = lanesIds.size() == lanesActuales.size();
        boolean mismosIds = lanesActuales.stream().map(Lane::getId).collect(Collectors.toSet()).containsAll(lanesIds);
        if (!mismaCantidad || !mismosIds) {
            throw new IllegalArgumentException("La lista de lanes no coincide con las lanes del pool");
        }

        for (int posicion = 0; posicion < lanesIds.size(); posicion++) {
            Long laneId = lanesIds.get(posicion);
            Lane lane = lanesActuales.stream()
                    .filter(l -> l.getId().equals(laneId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("La lane " + laneId + " no existe en el pool"));
            lane.setOrden(posicion);
            laneRepository.save(lane);
        }

        return laneRepository.findByPoolId(poolId);
    }

    // La usa PoolService antes de eliminar un pool.
    @Transactional(readOnly = true)
    public boolean tieneLanes(Long poolId) {
        return !laneRepository.findByPoolId(poolId).isEmpty();
    }

    // La usa ActividadService para validar que una lane exista en el proceso.
    @Transactional(readOnly = true)
    public boolean existeEnProceso(Long laneId, Long procesoId) {
        return laneRepository.findByIdAndProcesoId(laneId, procesoId).isPresent();
    }

    // La usa UsoRolProcesoConLanes para saber si un rol esta en uso.
    @Transactional(readOnly = true)
    public List<Object[]> buscarProcesosDondeSeUsaRol(Long rolId) {
        return laneRepository.buscarProcesosDondeSeUsaRol(rolId);
    }
}
