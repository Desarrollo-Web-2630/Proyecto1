package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.ActividadDTO;
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
    public List<Actividad> listarPorProcesoYEmpresa(Long procesoId, Long empresaId) {
        validarAccesoProceso(procesoId, empresaId);
        return actividadRepository.findByProcesoId(procesoId);
    }

    // 2. Listar las actividades de una lane concreta del proceso
    @Transactional(readOnly = true)
    public List<Actividad> listarPorLane(Long procesoId, Long laneId, Long empresaId) {
        validarAccesoProceso(procesoId, empresaId);
        return actividadRepository.findByProcesoIdAndLaneId(procesoId, laneId);
    }

    // 3. Obtener una actividad verificando que pertenezca a la empresa
    @Transactional(readOnly = true)
    public Actividad obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return actividadRepository.findByIdAndProcesoEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La actividad no existe o no pertenece a su empresa"));
    }

    // 4. Crear una actividad dentro de un proceso autorizado
    public Actividad crearActividad(ActividadDTO datos, Long procesoId, Long empresaId) {
        Proceso proceso = validarAccesoProceso(procesoId, empresaId);
        validarDatosObligatorios(datos);

        String nombre = datos.getNombre().trim();
        if (actividadRepository.existsByNombreAndProcesoId(nombre, procesoId)) {
            throw new IllegalArgumentException("Ya existe una actividad llamada '" + nombre + "' en el proceso");
        }

        Actividad actividad = new Actividad();
        actividad.setNombre(nombre);
        actividad.setTipoActividad(datos.getTipoActividad().trim());
        actividad.setLaneId(datos.getLaneId());
        actividad.setPosicionX(datos.getPosicionX());
        actividad.setPosicionY(datos.getPosicionY());
        actividad.setProceso(proceso);

        return actividadRepository.save(actividad);
    }

    // 5. Editar nombre, tipo, lane y posicion (HU-09).
    //    Cambiar la lane cambia el responsable, no es un movimiento estetico.
    public Actividad actualizarActividad(Long id, ActividadDTO datos, Long empresaId) {
        Actividad actividadExistente = obtenerPorIdYEmpresa(id, empresaId);
        validarDatosObligatorios(datos);

        Long procesoId = actividadExistente.getProceso().getId();
        String nombre = datos.getNombre().trim();
        if (actividadRepository.existsByNombreAndProcesoIdAndIdNot(nombre, procesoId, id)) {
            throw new IllegalArgumentException("Ya existe una actividad llamada '" + nombre + "' en el proceso");
        }

        actividadExistente.setNombre(nombre);
        actividadExistente.setTipoActividad(datos.getTipoActividad().trim());
        actividadExistente.setLaneId(datos.getLaneId());
        actividadExistente.setPosicionX(datos.getPosicionX());
        actividadExistente.setPosicionY(datos.getPosicionY());

        return actividadRepository.save(actividadExistente);
    }

    // 6. Mover la actividad en el diagrama sin tocar el resto de sus datos
    public Actividad moverActividad(Long id, Integer posicionX, Integer posicionY, Long empresaId) {
        Actividad actividad = obtenerPorIdYEmpresa(id, empresaId);
        validarPosicion(posicionX, posicionY);

        actividad.setPosicionX(posicionX);
        actividad.setPosicionY(posicionY);

        return actividadRepository.save(actividad);
    }

    // 7. Eliminar (borrado logico via @SQLDelete)
    public void eliminarActividad(Long id, Long empresaId) {
        Actividad actividad = obtenerPorIdYEmpresa(id, empresaId);
        actividadRepository.delete(actividad);
    }

    // Metodo auxiliar privado para validar que el proceso pertenece a la empresa
    private Proceso validarAccesoProceso(Long procesoId, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Proceso no encontrado o no pertenece a su empresa"));
    }

    /**
     * El DTO ya trae las anotaciones de validacion, pero el servicio no puede
     * confiar en que siempre lo llamen desde un formulario validado.
     */
    private void validarDatosObligatorios(ActividadDTO datos) {
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la actividad es obligatorio");
        }
        if (datos.getTipoActividad() == null || datos.getTipoActividad().isBlank()) {
            throw new IllegalArgumentException("El tipo de actividad es obligatorio");
        }
        // La lane es la que define el rol responsable de la actividad
        if (datos.getLaneId() == null) {
            throw new IllegalArgumentException("La actividad debe estar asociada a una lane");
        }
        validarPosicion(datos.getPosicionX(), datos.getPosicionY());
    }

    // La actividad debe quedar donde el usuario la ubico en el diagrama
    private void validarPosicion(Integer posicionX, Integer posicionY) {
        if (posicionX == null || posicionY == null) {
            throw new IllegalArgumentException("La posición de la actividad en el diagrama es obligatoria");
        }
        if (posicionX < 0 || posicionY < 0) {
            throw new IllegalArgumentException("La posición de la actividad no puede ser negativa");
        }
    }
}
