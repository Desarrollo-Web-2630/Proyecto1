package com.proyecto1.thymeleaf.services;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.ProcesoCompartidoRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;

@Service
public class AccesoProcesoService {

    private final ProcesoRepository procesoRepository;
    private final ProcesoCompartidoRepository compartidoRepository;

    public AccesoProcesoService(ProcesoRepository procesoRepository,
                                ProcesoCompartidoRepository compartidoRepository) {
        this.procesoRepository = procesoRepository;
        this.compartidoRepository = compartidoRepository;
    }

    @Transactional(readOnly = true)
    public Proceso verificarLectura(Long procesoId, Long empresaId) {
        Proceso propio = procesoRepository.findByIdAndEmpresaId(procesoId, empresaId).orElse(null);
        if (propio != null) {
            return propio;
        }
        if (compartidoRepository.existsByProcesoIdAndEmpresaInvitadaIdAndActivoTrue(procesoId, empresaId)) {
            return procesoRepository.findById(procesoId)
                    .orElseThrow(() -> new AccessDeniedException("El proceso compartido ya no está disponible"));
        }
        throw new AccessDeniedException("El proceso no existe o no pertenece a su empresa");
    }

    @Transactional(readOnly = true)
    public Proceso verificarEscritura(Long procesoId, Long empresaId) {
        Proceso propio = procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new AccessDeniedException("El proceso no existe o no pertenece a su empresa"));
        if (propio.getEstado() == Proceso.EstadoProceso.INACTIVO) {
            throw new AccessDeniedException("Un proceso inactivo no se puede modificar");
        }
        return propio;
    }

    @Transactional(readOnly = true)
    public boolean esSoloLectura(Long procesoId, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(procesoId, empresaId).isEmpty()
                && compartidoRepository.existsByProcesoIdAndEmpresaInvitadaIdAndActivoTrue(procesoId, empresaId);
    }

    @Transactional(readOnly = true)
    public List<Long> idsAccesibles(Long empresaId) {
        Set<Long> ids = new LinkedHashSet<>(procesoRepository.findIdsByEmpresaId(empresaId));
        ids.addAll(compartidoRepository.findIdsCompartidosCon(empresaId));
        return new ArrayList<>(ids);
    }
}
