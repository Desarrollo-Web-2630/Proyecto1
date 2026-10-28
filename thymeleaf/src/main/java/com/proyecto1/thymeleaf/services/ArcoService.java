package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.ArcoDTO;
import com.proyecto1.thymeleaf.model.Actividad;
import com.proyecto1.thymeleaf.model.Arco;
import com.proyecto1.thymeleaf.model.ElementoConectable;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.ArcoRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ArcoService {

    private final ArcoRepository arcoRepository;
    private final ElementoConectableService elementoService;
    private final ProcesoService procesoService;
    private final LaneService laneService;

    public ArcoService(ArcoRepository arcoRepository,
                       ElementoConectableService elementoService,
                       ProcesoService procesoService,
                       @Lazy LaneService laneService) {
        this.arcoRepository = arcoRepository;
        this.elementoService = elementoService;
        this.procesoService = procesoService;
        this.laneService = laneService;
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
    public Arco crearArco(ArcoDTO datos, Long procesoId, Long empresaId) {
        Proceso proceso = validarAccesoProceso(procesoId, empresaId);

        ElementoConectable origen = validarExtremos(datos, procesoId, empresaId, null);
        ElementoConectable destino = obtenerElemento(datos.getDestinoId(), empresaId);

        Arco arco = new Arco();
        arco.setNombre(datos.getNombre());
        arco.setCondicion(datos.getCondicion());
        arco.setOrigen(origen);
        arco.setDestino(destino);
        arco.setProceso(proceso);

        return arcoRepository.save(arco);
    }

    // 4. Editar el arco: la HU-12 permite cambiar tambien origen y destino,
    //    aplicando las mismas validaciones que al crear.
    public Arco actualizarArco(Long id, ArcoDTO datos, Long empresaId) {
        Arco arcoExistente = obtenerPorIdYEmpresa(id, empresaId);
        Long procesoId = arcoExistente.getProceso().getId();

        ElementoConectable origen = validarExtremos(datos, procesoId, empresaId, id);
        ElementoConectable destino = obtenerElemento(datos.getDestinoId(), empresaId);

        arcoExistente.setNombre(datos.getNombre());
        arcoExistente.setCondicion(datos.getCondicion());
        arcoExistente.setOrigen(origen);
        arcoExistente.setDestino(destino);

        return arcoRepository.save(arcoExistente);
    }

    // 5. Eliminar (borrado logico via @SQLDelete)
    public void eliminarArco(Long id, Long empresaId) {
        Arco arco = obtenerPorIdYEmpresa(id, empresaId);
        arcoRepository.delete(arco);
    }

    @Transactional(readOnly = true)
    public String advertenciaAlEliminar(Long id, Long empresaId) {
        Arco arco = obtenerPorIdYEmpresa(id, empresaId);

        List<String> sueltos = new java.util.ArrayList<>();
        if (quedariaSinEntradas(arco.getDestino().getId(), id)) {
            sueltos.add("'" + arco.getDestino().getNombre() + "' se queda sin ningún camino de entrada");
        }
        if (quedariaSinSalidas(arco.getOrigen().getId(), id)) {
            sueltos.add("'" + arco.getOrigen().getNombre() + "' se queda sin ningún camino de salida");
        }

        return sueltos.isEmpty() ? null : "Al eliminar este arco, " + String.join(" y ", sueltos) + ".";
    }

    public int eliminarArcosDeElemento(Long elementoId) {
        List<Arco> conectados = new java.util.ArrayList<>(arcoRepository.findByOrigenId(elementoId));
        conectados.addAll(arcoRepository.findByDestinoId(elementoId));

        conectados.forEach(arcoRepository::delete);
        return conectados.size();
    }

    // Validaciones compartidas por crear y editar (HU-11 y HU-12)
    private ElementoConectable validarExtremos(ArcoDTO datos, Long procesoId, Long empresaId, Long arcoIdActual) {
        if (datos.getOrigenId() == null || datos.getDestinoId() == null) {
            throw new IllegalArgumentException("El arco debe indicar un elemento de origen y uno de destino");
        }
        if (datos.getOrigenId().equals(datos.getDestinoId())) {
            throw new IllegalArgumentException("Un arco no puede conectar un elemento consigo mismo");
        }

        ElementoConectable origen = obtenerElemento(datos.getOrigenId(), empresaId);
        ElementoConectable destino = obtenerElemento(datos.getDestinoId(), empresaId);

        // Un flujo de secuencia nunca cruza de un proceso a otro
        if (!origen.getProceso().getId().equals(procesoId) || !destino.getProceso().getId().equals(procesoId)) {
            throw new IllegalArgumentException("El origen y el destino deben pertenecer al mismo proceso del arco");
        }

        Long poolOrigen = poolDe(origen, procesoId);
        Long poolDestino = poolDe(destino, procesoId);
        if (poolOrigen != null && poolDestino != null && !poolOrigen.equals(poolDestino)) {
            throw new IllegalArgumentException("Un arco no puede cruzar de un pool a otro; use un mensaje");
        }

        boolean duplicado = arcoRepository.findByOrigenId(datos.getOrigenId()).stream()
                .filter(otro -> !otro.getId().equals(arcoIdActual))
                .anyMatch(otro -> otro.getDestino().getId().equals(datos.getDestinoId()));
        if (duplicado) {
            throw new IllegalArgumentException("Ya existe un arco que conecta esos dos elementos");
        }

        return origen;
    }

    private Long poolDe(ElementoConectable elemento, Long procesoId) {
        if (elemento instanceof Actividad actividad && actividad.getLaneId() != null) {
            return laneService.poolIdDeLane(actividad.getLaneId(), procesoId);
        }
        return null;
    }

    private boolean quedariaSinEntradas(Long elementoId, Long arcoQueSeVa) {
        return arcoRepository.findByDestinoId(elementoId).stream()
                .noneMatch(otro -> !otro.getId().equals(arcoQueSeVa));
    }

    private boolean quedariaSinSalidas(Long elementoId, Long arcoQueSeVa) {
        return arcoRepository.findByOrigenId(elementoId).stream()
                .noneMatch(otro -> !otro.getId().equals(arcoQueSeVa));
    }

    // Metodo auxiliar privado para validar que el proceso pertenece a la empresa
    private Proceso validarAccesoProceso(Long procesoId, Long empresaId) {
        return procesoService.obtenerPorIdYEmpresa(procesoId, empresaId);
    }

    private ElementoConectable obtenerElemento(Long elementoId, Long empresaId) {
        return elementoService.obtenerPorIdYEmpresa(elementoId, empresaId);
    }
}
