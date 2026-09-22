package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.ActividadRequestDTO;
import com.proyecto1.thymeleaf.dto.ActividadResponseDTO;
import com.proyecto1.thymeleaf.model.Actividad;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.ActividadRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio de las actividades (HU-08 crear, HU-09 editar, HU-10
 * eliminar).
 *
 * Una actividad siempre vive dentro de un proceso y de una lane; la lane es la
 * que define el rol responsable, porque las actividades se asignan a funciones
 * y no a personas (HU-22).
 */
@Service
@Transactional
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final ProcesoRepository procesoRepository;

    public ActividadService(ActividadRepository actividadRepository, ProcesoRepository procesoRepository) {
        this.actividadRepository = actividadRepository;
        this.procesoRepository = procesoRepository;
    }

    // 1. Listar las actividades de un proceso verificando la empresa
    @Transactional(readOnly = true)
    public List<ActividadResponseDTO> listarPorProcesoYEmpresa(Long procesoId, Long empresaId) {
        validarAccesoProceso(procesoId, empresaId);
        return actividadRepository.findByProcesoId(procesoId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // 2. Listar las actividades de una lane concreta del proceso
    @Transactional(readOnly = true)
    public List<ActividadResponseDTO> listarPorLane(Long procesoId, Long laneId, Long empresaId) {
        validarAccesoProceso(procesoId, empresaId);
        return actividadRepository.findByProcesoIdAndLaneId(procesoId, laneId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // 3. Obtener una actividad verificando que pertenezca a la empresa
    @Transactional(readOnly = true)
    public ActividadResponseDTO obtenerPorIdYEmpresa(Long id, Long empresaId) {
        Actividad actividad = actividadRepository.findByIdAndProcesoEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La actividad no existe o no pertenece a su empresa"));
        return toResponseDTO(actividad);
    }

    // 4. Crear una actividad dentro de un proceso autorizado
    public ActividadResponseDTO crearActividad(ActividadRequestDTO request, Long procesoId, Long empresaId) {
        validarDatosObligatorios(request);
        Proceso proceso = validarAccesoProceso(procesoId, empresaId);

        String nombre = request.getNombre().trim();
        if (actividadRepository.existsByNombreAndProcesoId(nombre, procesoId)) {
            throw new IllegalArgumentException("Ya existe una actividad llamada '" + nombre + "' en el proceso");
        }

        Actividad actividad = new Actividad();
        actividad.setNombre(nombre);
        actividad.setTipoActividad(request.getTipoActividad().trim());
        actividad.setPosicionX(request.getPosicionX());
        actividad.setPosicionY(request.getPosicionY());
        actividad.setLaneId(request.getLaneId());
        actividad.setProceso(proceso);

        Actividad guardada = actividadRepository.save(actividad);
        return toResponseDTO(guardada);
    }

    // 5. Actualizar nombre, tipo y lane de una actividad
    public ActividadResponseDTO actualizarActividad(Long id, ActividadRequestDTO request, Long empresaId) {
        Actividad actividadExistente = actividadRepository.findByIdAndProcesoEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La actividad no existe o no pertenece a su empresa"));

        Long procesoId = actividadExistente.getProceso().getId();
        String nombre = request.getNombre().trim();
        if (actividadRepository.existsByNombreAndProcesoIdAndIdNot(nombre, procesoId, id)) {
            throw new IllegalArgumentException("Ya existe una actividad llamada '" + nombre + "' en el proceso");
        }

        actividadExistente.setNombre(nombre);
        actividadExistente.setTipoActividad(request.getTipoActividad().trim());
        actividadExistente.setLaneId(request.getLaneId());
        actividadExistente.setPosicionX(request.getPosicionX());
        actividadExistente.setPosicionY(request.getPosicionY());

        Actividad guardada = actividadRepository.save(actividadExistente);
        return toResponseDTO(guardada);
    }

    // 6. Mover la actividad en el diagrama sin tocar el resto de sus datos
    public ActividadResponseDTO moverActividad(Long id, Integer posicionX, Integer posicionY, Long empresaId) {
        Actividad actividad = actividadRepository.findByIdAndProcesoEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La actividad no existe o no pertenece a su empresa"));

        if (posicionX == null || posicionY == null) {
            throw new IllegalArgumentException("La posicion de la actividad en el diagrama es obligatoria");
        }
        if (posicionX < 0 || posicionY < 0) {
            throw new IllegalArgumentException("La posicion de la actividad no puede ser negativa");
        }

        actividad.setPosicionX(posicionX);
        actividad.setPosicionY(posicionY);

        Actividad guardada = actividadRepository.save(actividad);
        return toResponseDTO(guardada);
    }

    // 7. Eliminar (borrado logico via @SQLDelete)
    public void eliminarActividad(Long id, Long empresaId) {
        Actividad actividad = actividadRepository.findByIdAndProcesoEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La actividad no existe o no pertenece a su empresa"));
        actividadRepository.delete(actividad);
    }

    // Metodo auxiliar privado para validar que el proceso pertenece a la empresa
    private Proceso validarAccesoProceso(Long procesoId, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Proceso no encontrado o no pertenece a su empresa"));
    }

    private void validarDatosObligatorios(ActividadRequestDTO request) {
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la actividad es obligatorio");
        }
        if (request.getTipoActividad() == null || request.getTipoActividad().isBlank()) {
            throw new IllegalArgumentException("El tipo de actividad es obligatorio");
        }
        if (request.getLaneId() == null) {
            throw new IllegalArgumentException("La actividad debe estar asociada a una lane");
        }
        if (request.getPosicionX() == null || request.getPosicionY() == null) {
            throw new IllegalArgumentException("La posicion de la actividad en el diagrama es obligatoria");
        }
        if (request.getPosicionX() < 0 || request.getPosicionY() < 0) {
            throw new IllegalArgumentException("La posicion de la actividad no puede ser negativa");
        }
    }

    private ActividadResponseDTO toResponseDTO(Actividad actividad) {
        return new ActividadResponseDTO(
                actividad.getId(),
                actividad.getNombre(),
                actividad.getTipoActividad(),
                actividad.getPosicionX(),
                actividad.getPosicionY(),
                actividad.getLaneId()
        );
    }
}
