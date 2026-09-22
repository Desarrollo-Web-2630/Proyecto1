package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.model.Actividad;
import com.proyecto1.thymeleaf.dto.ActividadRequestDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.ActividadRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActividadServiceTest {

    @Mock private ActividadRepository actividadRepository;
    @Mock private ProcesoRepository procesoRepository;

    @InjectMocks
    private ActividadService actividadService;

    private ActividadRequestDTO datosValidos;
    private Proceso proceso;

    @BeforeEach
    void setUp() {
        datosValidos = new ActividadRequestDTO();
        datosValidos.setNombre("Radicar solicitud");
        datosValidos.setTipoActividad("Tarea");
        datosValidos.setPosicionX(10);
        datosValidos.setPosicionY(20);
        datosValidos.setLaneId(1L);

        Empresa empresa = new Empresa();
        empresa.setId(1L);
        proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);
    }

    @Test
    void crearActividad_conDatosValidos_seGuarda() {
        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));
        when(actividadRepository.existsByNombreAndProcesoId("Radicar solicitud", 1L)).thenReturn(false);
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = actividadService.crearActividad(datosValidos, 1L, 1L);

        assertEquals("Radicar solicitud", resultado.getNombre());
    }

    @Test
    void crearActividad_conNombreDuplicadoEnElProceso_lanzaExcepcion() {
        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));
        when(actividadRepository.existsByNombreAndProcesoId("Radicar solicitud", 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> actividadService.crearActividad(datosValidos, 1L, 1L));
    }

    @Test
    void crearActividad_conProcesoDeOtraEmpresa_lanzaExcepcion() {
        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> actividadService.crearActividad(datosValidos, 1L, 1L));
    }

    @Test
    void crearActividad_sinLane_lanzaExcepcion() {
        datosValidos.setLaneId(null);
        assertThrows(IllegalArgumentException.class, () -> actividadService.crearActividad(datosValidos, 1L, 1L));
    }

    @Test
    void crearActividad_conPosicionNegativa_lanzaExcepcion() {
        datosValidos.setPosicionX(-1);
        assertThrows(IllegalArgumentException.class, () -> actividadService.crearActividad(datosValidos, 1L, 1L));
    }

    @Test
    void moverActividad_conPosicionesValidas_actualiza() {
        Actividad actividad = new Actividad();
        actividad.setId(5L);
        actividad.setProceso(proceso);

        when(actividadRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.of(actividad));
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = actividadService.moverActividad(5L, 30, 40, 1L);

        assertEquals(30, resultado.getPosicionX());
        assertEquals(40, resultado.getPosicionY());
    }

    @Test
    void moverActividad_conPosicionNegativa_lanzaExcepcion() {
        Actividad actividad = new Actividad();
        actividad.setId(5L);
        actividad.setProceso(proceso);

        when(actividadRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.of(actividad));

        assertThrows(IllegalArgumentException.class, () -> actividadService.moverActividad(5L, -1, 40, 1L));
    }

    @Test
    void eliminarActividad_existente_invocaDelete() {
        Actividad actividad = new Actividad();
        actividad.setId(5L);
        actividad.setProceso(proceso);

        when(actividadRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.of(actividad));

        actividadService.eliminarActividad(5L, 1L);

        verify(actividadRepository).delete(actividad);
    }

    @Test
    void listarPorLane_delegaAlRepositorioConLaneId() {
        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));
        when(actividadRepository.findByProcesoIdAndLaneId(1L, 2L)).thenReturn(java.util.List.of(new Actividad()));

        assertEquals(1, actividadService.listarPorLane(1L, 2L, 1L).size());
    }
}