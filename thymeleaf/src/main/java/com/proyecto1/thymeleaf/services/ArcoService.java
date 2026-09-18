package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.ArcoForm;
import com.proyecto1.thymeleaf.model.Arco;
import com.proyecto1.thymeleaf.model.ElementoConectable;
import com.proyecto1.thymeleaf.model.Gateway;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.ArcoRepository;
import com.proyecto1.thymeleaf.repository.ElementoConectableRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ArcoService {

    private final ArcoRepository arcoRepository;
    private final ElementoConectableRepository elementoRepository;
    private final ProcesoRepository procesoRepository;

    public ArcoService(ArcoRepository arcoRepository,
                       ElementoConectableRepository elementoRepository,
                       ProcesoRepository procesoRepository) {
        this.arcoRepository = arcoRepository;
        this.elementoRepository = elementoRepository;
        this.procesoRepository = procesoRepository;
    }

    @Transactional(readOnly = true)
    public List<Arco> listarPorProcesoYEmpresa(Long procesoId, Long empresaId) {
        validarAccesoProceso(procesoId, empresaId);
        return arcoRepository.findByProcesoId(procesoId);
    }

    @Transactional(readOnly = true)
    public List<ElementoConectable> listarElementosConectables(Long procesoId, Long empresaId) {
        validarAccesoProceso(procesoId, empresaId);
        return elementoRepository.findByProcesoId(procesoId);
    }

    @Transactional(readOnly = true)
    public Arco obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return arcoRepository.findByIdAndProcesoEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El arco no existe o no pertenece a su empresa"));
    }

    public Arco crearArco(ArcoForm form, Long procesoId, Long empresaId) {
        Proceso proceso = validarAccesoProceso(procesoId, empresaId);

        ElementoConectable origen = obtenerElemento(form.getOrigenId(), procesoId);
        ElementoConectable destino = obtenerElemento(form.getDestinoId(), procesoId);
        validarConexion(origen, destino, null);

        Arco arco = Arco.builder()
                .nombre(normalizarTexto(form.getNombre()))
                .condicion(resolverCondicion(origen, form.getCondicion()))
                .origen(origen)
                .destino(destino)
                .proceso(proceso)
                .build();

        return arcoRepository.save(arco);
    }

    public Arco actualizarArco(Long id, ArcoForm form, Long empresaId) {
        Arco arco = obtenerPorIdYEmpresa(id, empresaId);
        Long procesoId = arco.getProceso().getId();

        ElementoConectable origen = obtenerElemento(form.getOrigenId(), procesoId);
        ElementoConectable destino = obtenerElemento(form.getDestinoId(), procesoId);
        validarConexion(origen, destino, arco.getId());

        arco.setNombre(normalizarTexto(form.getNombre()));
        arco.setOrigen(origen);
        arco.setDestino(destino);
        arco.setCondicion(resolverCondicion(origen, form.getCondicion()));

        return arcoRepository.save(arco);
    }

    public List<String> eliminarArco(Long id, Long empresaId) {
        Arco arco = obtenerPorIdYEmpresa(id, empresaId);

        ElementoConectable origen = arco.getOrigen();
        ElementoConectable destino = arco.getDestino();
        String nombreOrigen = origen.getNombre();
        String nombreDestino = destino.getNombre();
        Long origenId = origen.getId();
        Long destinoId = destino.getId();

        arcoRepository.delete(arco);
        arcoRepository.flush();

        List<String> advertencias = new ArrayList<>();
        if (arcoRepository.findByOrigenId(origenId).isEmpty()) {
            advertencias.add("«" + nombreOrigen + "» queda sin camino de salida");
        }
        if (arcoRepository.findByDestinoId(destinoId).isEmpty()) {
            advertencias.add("«" + nombreDestino + "» queda sin camino de entrada, es inalcanzable en el diagrama");
        }
        return advertencias;
    }

    private void validarConexion(ElementoConectable origen, ElementoConectable destino, Long arcoIdActual) {
        if (origen.getId().equals(destino.getId())) {
            throw new IllegalArgumentException("Un arco no puede tener el mismo elemento como origen y destino");
        }
        if (!origen.getProceso().getId().equals(destino.getProceso().getId())) {
            throw new IllegalArgumentException("Un arco no puede conectar elementos de procesos distintos");
        }
        boolean duplicado = arcoRepository.findByOrigenId(origen.getId()).stream()
                .filter(a -> arcoIdActual == null || !a.getId().equals(arcoIdActual))
                .anyMatch(a -> a.getDestino().getId().equals(destino.getId()));
        if (duplicado) {
            throw new IllegalArgumentException("Ya existe un arco entre «" + origen.getNombre()
                    + "» y «" + destino.getNombre() + "»");
        }
    }

    private String resolverCondicion(ElementoConectable origen, String condicion) {
        String valor = normalizarTexto(condicion);
        if (origen instanceof Gateway gateway) {
            if (gateway.requiereCondicionEnSalidas()) {
                if (valor == null) {
                    throw new IllegalArgumentException("Un arco que sale de un gateway "
                            + gateway.getTipo() + " requiere una condición definida");
                }
                return valor;
            }
            return null;
        }
        return null;
    }

    private ElementoConectable obtenerElemento(Long elementoId, Long procesoId) {
        return elementoRepository.findByIdAndProcesoId(elementoId, procesoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El elemento seleccionado no existe o no pertenece a este proceso"));
    }

    private Proceso validarAccesoProceso(Long procesoId, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Proceso no encontrado o no pertenece a su empresa"));
    }

    private String normalizarTexto(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
