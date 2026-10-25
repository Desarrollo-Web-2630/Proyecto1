package com.proyecto1.thymeleaf.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto1.thymeleaf.dto.MensajeDTO;
import com.proyecto1.thymeleaf.model.ElementoConectable;
import com.proyecto1.thymeleaf.model.Mensaje;
import com.proyecto1.thymeleaf.model.Pool;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.MensajeRepository;

// HU-25 a HU-28: logica de negocio de los mensajes. El sistema solo documenta
// el envio, no lo ejecuta.
@Service
@Transactional
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final PoolService poolService;
    private final ElementoConectableService elementoService;
    private final ProcesoService procesoService;

    public MensajeService(MensajeRepository mensajeRepository, PoolService poolService,
                          ElementoConectableService elementoService, ProcesoService procesoService) {
        this.mensajeRepository = mensajeRepository;
        this.poolService = poolService;
        this.elementoService = elementoService;
        this.procesoService = procesoService;
    }

    @Transactional(readOnly = true)
    public List<Mensaje> listarPorProcesoYEmpresa(Long procesoId, Long empresaId) {
        validarAccesoProceso(procesoId, empresaId);
        return mensajeRepository.findByProcesoId(procesoId);
    }

    @Transactional(readOnly = true)
    public Mensaje obtenerPorIdYEmpresa(Long id, Long procesoId, Long empresaId) {
        return mensajeRepository.findByIdAndProcesoIdAndEmpresaId(id, procesoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El mensaje no existe o no pertenece a su empresa"));
    }

    public Mensaje crearMensaje(MensajeDTO datos, Long procesoId, Long empresaId) {
        Proceso proceso = validarAccesoProceso(procesoId, empresaId);
        Pool origen = poolService.obtenerPorIdYEmpresa(datos.getPoolOrigenId(), procesoId, empresaId);
        Pool destino = poolService.obtenerPorIdYEmpresa(datos.getPoolDestinoId(), procesoId, empresaId);
        validarExtremos(origen, destino);

        Mensaje mensaje = new Mensaje();
        mensaje.setNombre(datos.getNombre().trim());
        mensaje.setClaveCorrelacion(datos.getClaveCorrelacion().trim());
        mensaje.setProceso(proceso);
        mensaje.setPoolOrigen(origen);
        mensaje.setPoolDestino(destino);
        mensaje.setElementoOrigen(buscarElementoOpcional(datos.getElementoOrigenId(), procesoId, empresaId));

        return mensajeRepository.save(mensaje);
    }

    public Mensaje actualizarMensaje(Long id, MensajeDTO datos, Long procesoId, Long empresaId) {
        Mensaje mensaje = obtenerPorIdYEmpresa(id, procesoId, empresaId);
        Pool origen = poolService.obtenerPorIdYEmpresa(datos.getPoolOrigenId(), procesoId, empresaId);
        Pool destino = poolService.obtenerPorIdYEmpresa(datos.getPoolDestinoId(), procesoId, empresaId);
        validarExtremos(origen, destino);

        mensaje.setNombre(datos.getNombre().trim());
        mensaje.setClaveCorrelacion(datos.getClaveCorrelacion().trim());
        mensaje.setPoolOrigen(origen);
        mensaje.setPoolDestino(destino);
        mensaje.setElementoOrigen(buscarElementoOpcional(datos.getElementoOrigenId(), procesoId, empresaId));

        return mensajeRepository.save(mensaje);
    }

    public void eliminarMensaje(Long id, Long procesoId, Long empresaId) {
        Mensaje mensaje = obtenerPorIdYEmpresa(id, procesoId, empresaId);
        mensajeRepository.delete(mensaje);
    }

    private void validarExtremos(Pool origen, Pool destino) {
        if (origen.getId().equals(destino.getId())) {
            throw new IllegalArgumentException("Un mensaje debe ir entre dos pools distintos");
        }
    }

    private ElementoConectable buscarElementoOpcional(Long elementoId, Long procesoId, Long empresaId) {
        if (elementoId == null) {
            return null;
        }
        ElementoConectable elemento = elementoService.obtenerPorIdYEmpresa(elementoId, empresaId);
        if (!elemento.getProceso().getId().equals(procesoId)) {
            throw new IllegalArgumentException("El elemento de origen debe pertenecer al mismo proceso del mensaje");
        }
        return elemento;
    }

    private Proceso validarAccesoProceso(Long procesoId, Long empresaId) {
        return procesoService.obtenerPorIdYEmpresa(procesoId, empresaId);
    }
}
