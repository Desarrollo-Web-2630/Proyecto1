package com.proyecto1.thymeleaf.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto1.thymeleaf.dto.GatewayDTO;
import com.proyecto1.thymeleaf.model.Arco;
import com.proyecto1.thymeleaf.model.Gateway;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.ArcoRepository;
import com.proyecto1.thymeleaf.repository.GatewayRepository;

@Service
@Transactional
public class GatewayService {

    private final GatewayRepository gatewayRepository;
    private final ArcoRepository arcoRepository;
    private final ArcoService arcoService;
    private final ProcesoService procesoService;
    private final AuditoriaService auditoriaService;
    private final AccesoProcesoService accesoProcesoService;

    public GatewayService(GatewayRepository gatewayRepository, ArcoRepository arcoRepository,
                          ArcoService arcoService, ProcesoService procesoService,
                          AuditoriaService auditoriaService, AccesoProcesoService accesoProcesoService) {
        this.gatewayRepository = gatewayRepository;
        this.arcoRepository = arcoRepository;
        this.arcoService = arcoService;
        this.procesoService = procesoService;
        this.auditoriaService = auditoriaService;
        this.accesoProcesoService = accesoProcesoService;
    }

    @Transactional(readOnly = true)
    public List<Gateway> listarPorProcesoYEmpresa(Long procesoId, Long empresaId) {
        accesoProcesoService.verificarLectura(procesoId, empresaId);
        return gatewayRepository.findByProcesoId(procesoId);
    }

    @Transactional(readOnly = true)
    public Gateway obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return gatewayRepository.findByIdAndProcesoEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El gateway no existe o no pertenece a su empresa"));
    }

    public Gateway crearGateway(GatewayDTO datos, Long procesoId, Long empresaId) {
        Proceso proceso = validarAccesoProceso(procesoId, empresaId);
        validarDatosObligatorios(datos);

        Gateway gateway = new Gateway();
        gateway.setNombre(datos.getNombre().trim());
        gateway.setTipo(datos.getTipo());
        gateway.setPosicionX(datos.getPosicionX() == null ? 0 : Math.max(datos.getPosicionX(), 0));
        gateway.setPosicionY(datos.getPosicionY() == null ? 0 : Math.max(datos.getPosicionY(), 0));
        gateway.setProceso(proceso);
        gateway.setStatus(0);

        Gateway guardado = gatewayRepository.save(gateway);
        auditoriaService.registrar("Gateway", guardado.getId(), "CREAR", "Creado: " + guardado.getNombre());
        return guardado;
    }

    public Gateway actualizarGateway(Long id, GatewayDTO datos, Long empresaId) {
        Gateway gatewayExistente = obtenerPorIdYEmpresa(id, empresaId);
        validarDatosObligatorios(datos);

        boolean cambioAParalelo = datos.getTipo() == Gateway.TipoGateway.PARALELO
                && gatewayExistente.getTipo() != Gateway.TipoGateway.PARALELO;
        gatewayExistente.setNombre(datos.getNombre().trim());
        gatewayExistente.setTipo(datos.getTipo());
        if (datos.getPosicionX() != null && datos.getPosicionY() != null) {
            gatewayExistente.setPosicionX(Math.max(datos.getPosicionX(), 0));
            gatewayExistente.setPosicionY(Math.max(datos.getPosicionY(), 0));
        }

        Gateway guardado = gatewayRepository.save(gatewayExistente);
        if (cambioAParalelo) {
            limpiarCondiciones(guardado.getId());
        }
        auditoriaService.registrar("Gateway", guardado.getId(), "EDITAR", "Editado: " + guardado.getNombre());
        return guardado;
    }

    public void eliminarGateway(Long id, Long empresaId) {
        Gateway gateway = obtenerPorIdYEmpresa(id, empresaId);
        arcoService.eliminarArcosDeElemento(id);
        auditoriaService.registrar("Gateway", id, "ELIMINAR", "Gateway eliminado con sus arcos");
        gatewayRepository.delete(gateway);
    }

    @Transactional(readOnly = true)
    public String advertenciaAlEliminar(Long id, Long empresaId) {
        Gateway gateway = obtenerPorIdYEmpresa(id, empresaId);
        List<Arco> salientes = arcoRepository.findByOrigenId(gateway.getId());
        List<Arco> entrantes = arcoRepository.findByDestinoId(gateway.getId());
        if (salientes.isEmpty() && entrantes.isEmpty()) {
            return null;
        }
        return "Al eliminar este gateway también se eliminarán " + (salientes.size() + entrantes.size())
                + " arco(s) conectado(s).";
    }

    @Transactional(readOnly = true)
    public void validarSalidasProceso(Long procesoId) {
        for (Gateway gateway : gatewayRepository.findByProcesoId(procesoId)) {
            List<Arco> salientes = arcoRepository.findByOrigenId(gateway.getId());
            if (salientes.size() < 2) {
                throw new IllegalArgumentException("El gateway '" + gateway.getNombre()
                        + "' debe tener al menos dos salidas antes de publicar el proceso");
            }
            if (gateway.getTipo() != Gateway.TipoGateway.PARALELO
                    && salientes.stream().anyMatch(arco -> arco.getCondicion() == null || arco.getCondicion().isBlank())) {
                throw new IllegalArgumentException("Todas las salidas del gateway '" + gateway.getNombre()
                        + "' deben tener condición");
            }
        }
    }

    private void limpiarCondiciones(Long gatewayId) {
        for (Arco arco : arcoRepository.findByOrigenId(gatewayId)) {
            arco.setCondicion(null);
            arcoRepository.save(arco);
        }
    }

    private Proceso validarAccesoProceso(Long procesoId, Long empresaId) {
        return procesoService.obtenerPorIdYEmpresa(procesoId, empresaId);
    }

    private void validarDatosObligatorios(GatewayDTO datos) {
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del gateway es obligatorio");
        }
        if (datos.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de gateway es obligatorio");
        }
    }
}
