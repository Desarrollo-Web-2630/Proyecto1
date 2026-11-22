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
    private final AuditoriaService auditoriaService;
    private final AccesoProcesoService accesoProcesoService;

    public MensajeService(MensajeRepository mensajeRepository, PoolService poolService,
                          ElementoConectableService elementoService, ProcesoService procesoService,
                          AuditoriaService auditoriaService, AccesoProcesoService accesoProcesoService) {
        this.mensajeRepository = mensajeRepository;
        this.poolService = poolService;
        this.elementoService = elementoService;
        this.procesoService = procesoService;
        this.auditoriaService = auditoriaService;
        this.accesoProcesoService = accesoProcesoService;
    }

    @Transactional(readOnly = true)
    public List<Mensaje> listarPorProcesoYEmpresa(Long procesoId, Long empresaId) {
        accesoProcesoService.verificarLectura(procesoId, empresaId);
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
        aplicarDetalles(datos, mensaje);

        Mensaje guardado = mensajeRepository.save(mensaje);
        auditoriaService.registrar("Mensaje", guardado.getId(), "CREAR", "Creado: " + guardado.getNombre());
        return guardado;
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
        aplicarDetalles(datos, mensaje);

        Mensaje guardado = mensajeRepository.save(mensaje);
        auditoriaService.registrar("Mensaje", guardado.getId(), "EDITAR", "Editado: " + guardado.getNombre());
        return guardado;
    }

    public void eliminarMensaje(Long id, Long procesoId, Long empresaId) {
        Mensaje mensaje = obtenerPorIdYEmpresa(id, procesoId, empresaId);
        auditoriaService.registrar("Mensaje", id, "ELIMINAR", "Mensaje eliminado");
        mensajeRepository.delete(mensaje);
    }

    private void aplicarDetalles(MensajeDTO datos, Mensaje mensaje) {
        Mensaje.TipoMensaje tipo = datos.getTipoMensaje() == null ? Mensaje.TipoMensaje.THROW : datos.getTipoMensaje();
        Mensaje.VarianteMensaje variante = datos.getVariante() == null
                ? Mensaje.VarianteMensaje.INICIO : datos.getVariante();
        String destinoExterno = datos.getDestinoExterno() == null || datos.getDestinoExterno().isBlank()
                ? null : datos.getDestinoExterno().trim();
        String payload = datos.getPayload() == null || datos.getPayload().isBlank()
                ? null : datos.getPayload().trim();

        if (tipo == Mensaje.TipoMensaje.CATCH && destinoExterno != null) {
            throw new IllegalArgumentException("Un mensaje CATCH no puede tener destino externo");
        }
        if (tipo == Mensaje.TipoMensaje.THROW && destinoExterno != null && datos.getPoliticaFallo() == null) {
            throw new IllegalArgumentException("Un mensaje a sistema externo requiere política de fallo");
        }
        if (payload != null && payload.length() > 4000) {
            throw new IllegalArgumentException("El payload no puede superar los 4000 caracteres");
        }

        mensaje.setTipoMensaje(tipo);
        mensaje.setVariante(variante);
        mensaje.setDestinoExterno(destinoExterno);
        mensaje.setPayload(payload);
        mensaje.setPoliticaFallo(datos.getPoliticaFallo());
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
