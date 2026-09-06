package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.model.Arco;
import com.proyecto1.thymeleaf.model.ElementoConectable;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.ArcoRepository;
import com.proyecto1.thymeleaf.repository.ElementoConectableRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio de los arcos (HU-11 Crear arco).
 *
 * Un arco es el flujo de secuencia que une dos elementos conectables dentro
 * del mismo proceso y fija el orden de ejecucion.
 */
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

    // 1. Listar los arcos de un proceso verificando la empresa
    @Transactional(readOnly = true)
    public List<Arco> listarPorProcesoYEmpresa(Long procesoId, Long empresaId) {
        validarAccesoProceso(procesoId, empresaId);
        return arcoRepository.findByProcesoId(procesoId);
    }

    // 2. Obtener un arco verificando que pertenezca a la empresa
    @Transactional(readOnly = true)
    public Arco obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return arcoRepository.findByIdAndProcesoEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El arco no existe o no pertenece a su empresa"));
    }

    // 3. Crear un arco entre dos elementos del mismo proceso
    public Arco crearArco(Arco arco, Long procesoId, Long origenId, Long destinoId, Long empresaId) {
        Proceso proceso = validarAccesoProceso(procesoId, empresaId);

        if (origenId == null || destinoId == null) {
            throw new IllegalArgumentException("El arco debe indicar un elemento de origen y uno de destino");
        }
        if (origenId.equals(destinoId)) {
            throw new IllegalArgumentException("Un arco no puede conectar un elemento consigo mismo");
        }

        ElementoConectable origen = obtenerElemento(origenId, empresaId);
        ElementoConectable destino = obtenerElemento(destinoId, empresaId);

        // Los dos extremos tienen que vivir en el proceso donde se dibuja el arco
        if (!origen.getProceso().getId().equals(procesoId) || !destino.getProceso().getId().equals(procesoId)) {
            throw new IllegalArgumentException("El origen y el destino deben pertenecer al mismo proceso del arco");
        }
        if (arcoRepository.existsByOrigenIdAndDestinoId(origenId, destinoId)) {
            throw new IllegalArgumentException("Ya existe un arco que conecta esos dos elementos");
        }

        arco.setOrigen(origen);
        arco.setDestino(destino);
        arco.setProceso(proceso);

        return arcoRepository.save(arco);
    }

    // 4. Actualizar el nombre y la condicion del arco
    public Arco actualizarArco(Long id, Arco arcoDetalles, Long empresaId) {
        Arco arcoExistente = obtenerPorIdYEmpresa(id, empresaId);

        arcoExistente.setNombre(arcoDetalles.getNombre());
        arcoExistente.setCondicion(arcoDetalles.getCondicion());

        return arcoRepository.save(arcoExistente);
    }

    // 5. Eliminar (borrado logico via @SQLDelete)
    public void eliminarArco(Long id, Long empresaId) {
        Arco arco = obtenerPorIdYEmpresa(id, empresaId);
        arcoRepository.delete(arco);
    }

    // Metodo auxiliar privado para validar que el proceso pertenece a la empresa
    private Proceso validarAccesoProceso(Long procesoId, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Proceso no encontrado o no pertenece a su empresa"));
    }

    private ElementoConectable obtenerElemento(Long elementoId, Long empresaId) {
        return elementoRepository.findByIdAndProcesoEmpresaId(elementoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El elemento " + elementoId + " no existe o no pertenece a su empresa"));
    }
}
