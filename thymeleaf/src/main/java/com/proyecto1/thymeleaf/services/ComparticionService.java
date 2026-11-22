package com.proyecto1.thymeleaf.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto1.thymeleaf.dto.ComparticionRequestDTO;
import com.proyecto1.thymeleaf.dto.ComparticionResponseDTO;
import com.proyecto1.thymeleaf.dto.EmpresaResumenDTO;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.model.ProcesoCompartido;
import com.proyecto1.thymeleaf.repository.ProcesoCompartidoRepository;

@Service
@Transactional
public class ComparticionService {

    private final ProcesoCompartidoRepository compartidoRepository;
    private final ProcesoService procesoService;
    private final EmpresaService empresaService;
    private final AuditoriaService auditoriaService;

    public ComparticionService(ProcesoCompartidoRepository compartidoRepository,
                               ProcesoService procesoService,
                               EmpresaService empresaService,
                               AuditoriaService auditoriaService) {
        this.compartidoRepository = compartidoRepository;
        this.procesoService = procesoService;
        this.empresaService = empresaService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public ComparticionResponseDTO listar(Long procesoId, Long empresaId) {
        Proceso proceso = procesoService.obtenerPorIdYEmpresa(procesoId, empresaId);
        List<EmpresaResumenDTO> invitadas = new ArrayList<>();
        for (ProcesoCompartido compartido : compartidoRepository.findActivosByProcesoId(proceso.getId())) {
            invitadas.add(EmpresaResumenDTO.desde(empresaService.obtenerPorId(compartido.getEmpresaInvitadaId())));
        }
        return new ComparticionResponseDTO(proceso.getId(), !invitadas.isEmpty(), invitadas);
    }

    public ComparticionResponseDTO actualizar(Long procesoId, ComparticionRequestDTO datos, Long empresaId) {
        Proceso proceso = procesoService.obtenerPorIdYEmpresa(procesoId, empresaId);
        List<Long> solicitadas = datos.getEmpresasInvitadasIds() == null
                ? List.of() : datos.getEmpresasInvitadasIds().stream().distinct().toList();

        if (solicitadas.contains(empresaId)) {
            throw new IllegalArgumentException("La empresa propietaria no puede invitarse a sí misma");
        }
        for (Long idEmpresa : solicitadas) {
            empresaService.obtenerPorId(idEmpresa);
        }

        Set<Long> destino = new HashSet<>(solicitadas);
        for (ProcesoCompartido compartido : compartidoRepository.findByProcesoId(procesoId)) {
            compartido.setActivo(destino.contains(compartido.getEmpresaInvitadaId()));
            compartidoRepository.save(compartido);
            destino.remove(compartido.getEmpresaInvitadaId());
        }
        for (Long idEmpresa : destino) {
            ProcesoCompartido nuevo = new ProcesoCompartido(null, proceso, idEmpresa, true);
            compartidoRepository.save(nuevo);
        }

        auditoriaService.registrar("Proceso", procesoId, "COMPARTIR",
                "Empresas invitadas: " + solicitadas);
        return listar(procesoId, empresaId);
    }
}
