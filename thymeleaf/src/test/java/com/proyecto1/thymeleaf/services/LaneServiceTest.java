package com.proyecto1.thymeleaf.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto1.thymeleaf.dto.LaneDTO;
import com.proyecto1.thymeleaf.model.Lane;
import com.proyecto1.thymeleaf.model.Pool;
import com.proyecto1.thymeleaf.model.RolProceso;
import com.proyecto1.thymeleaf.repository.LaneRepository;

@ExtendWith(MockitoExtension.class)
class LaneServiceTest {

    @Mock private LaneRepository laneRepository;
    @Mock private PoolService poolService;
    @Mock private RolProcesoService rolProcesoService;

    @InjectMocks
    private LaneService laneService;

    private Pool pool;
    private RolProceso rol;
    private LaneDTO datos;

    @BeforeEach
    void setUp() {
        pool = new Pool();
        pool.setId(1L);

        rol = new RolProceso();
        rol.setId(5L);
        rol.setNombre("Analista");

        datos = new LaneDTO();
        datos.setNombre("  Analisis  ");
        datos.setRolProcesoId(5L);
    }

    private Lane construirLane(Long id, int orden) {
        Lane lane = new Lane();
        lane.setId(id);
        lane.setNombre("Lane " + id);
        lane.setOrden(orden);
        lane.setPool(pool);
        lane.setRolProceso(rol);
        return lane;
    }

    @Test
    void listarPorPoolYEmpresa_devuelveLasLanes() {
        when(poolService.obtenerPorIdYEmpresa(1L, 1L, 1L)).thenReturn(pool);
        when(laneRepository.findByPoolId(1L)).thenReturn(List.of(construirLane(1L, 0)));

        assertEquals(1, laneService.listarPorPoolYEmpresa(1L, 1L, 1L).size());
    }

    @Test
    void obtenerPorIdYEmpresa_existente_devuelveLaLane() {
        when(laneRepository.findByIdAndPoolIdAndEmpresaId(1L, 1L, 1L)).thenReturn(Optional.of(construirLane(1L, 0)));

        assertEquals(1L, laneService.obtenerPorIdYEmpresa(1L, 1L, 1L).getId());
    }

    @Test
    void obtenerPorIdYEmpresa_inexistente_lanzaExcepcion() {
        when(laneRepository.findByIdAndPoolIdAndEmpresaId(9L, 1L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> laneService.obtenerPorIdYEmpresa(9L, 1L, 1L));
    }

    @Test
    void crearLane_asignaRolYOrdenAlFinal() {
        when(poolService.obtenerPorIdYEmpresa(1L, 1L, 1L)).thenReturn(pool);
        when(rolProcesoService.obtenerEntidadPorIdYEmpresa(5L, 1L)).thenReturn(rol);
        when(laneRepository.findByPoolId(1L)).thenReturn(List.of(construirLane(1L, 0), construirLane(2L, 1)));
        when(laneRepository.save(any(Lane.class))).thenAnswer(inv -> inv.getArgument(0));

        Lane resultado = laneService.crearLane(datos, 1L, 1L, 1L);

        assertEquals("Analisis", resultado.getNombre());
        assertEquals(2, resultado.getOrden());
        assertEquals(rol, resultado.getRolProceso());
        assertEquals(pool, resultado.getPool());
    }

    @Test
    void crearLane_conRolAjeno_noGuarda() {
        when(poolService.obtenerPorIdYEmpresa(1L, 1L, 1L)).thenReturn(pool);
        when(rolProcesoService.obtenerEntidadPorIdYEmpresa(5L, 1L)).thenThrow(new IllegalArgumentException("ajeno"));

        assertThrows(IllegalArgumentException.class, () -> laneService.crearLane(datos, 1L, 1L, 1L));
        verify(laneRepository, never()).save(any());
    }

    @Test
    void actualizarLane_cambiaNombreYRol() {
        RolProceso otroRol = new RolProceso();
        otroRol.setId(6L);
        datos.setRolProcesoId(6L);
        when(laneRepository.findByIdAndPoolIdAndEmpresaId(1L, 1L, 1L)).thenReturn(Optional.of(construirLane(1L, 0)));
        when(rolProcesoService.obtenerEntidadPorIdYEmpresa(6L, 1L)).thenReturn(otroRol);
        when(laneRepository.save(any(Lane.class))).thenAnswer(inv -> inv.getArgument(0));

        Lane resultado = laneService.actualizarLane(1L, datos, 1L, 1L);

        assertEquals("Analisis", resultado.getNombre());
        assertEquals(otroRol, resultado.getRolProceso());
    }

    @Test
    void eliminarLane_existente_laElimina() {
        Lane lane = construirLane(1L, 0);
        when(laneRepository.findByIdAndPoolIdAndEmpresaId(1L, 1L, 1L)).thenReturn(Optional.of(lane));

        laneService.eliminarLane(1L, 1L, 1L);

        verify(laneRepository).delete(lane);
    }

    @Test
    void reordenarLanes_conListaValida_asignaLasPosiciones() {
        Lane a = construirLane(1L, 0);
        Lane b = construirLane(2L, 1);
        Lane c = construirLane(3L, 2);
        when(poolService.obtenerPorIdYEmpresa(1L, 1L, 1L)).thenReturn(pool);
        when(laneRepository.findByPoolId(1L)).thenReturn(new ArrayList<>(List.of(a, b, c)));

        laneService.reordenarLanes(1L, List.of(3L, 1L, 2L), 1L, 1L);

        assertEquals(0, c.getOrden());
        assertEquals(1, a.getOrden());
        assertEquals(2, b.getOrden());
        verify(laneRepository).save(a);
        verify(laneRepository).save(b);
        verify(laneRepository).save(c);
    }

    @Test
    void reordenarLanes_conCantidadDistinta_lanzaExcepcion() {
        when(poolService.obtenerPorIdYEmpresa(1L, 1L, 1L)).thenReturn(pool);
        when(laneRepository.findByPoolId(1L)).thenReturn(List.of(construirLane(1L, 0), construirLane(2L, 1)));

        assertThrows(IllegalArgumentException.class, () -> laneService.reordenarLanes(1L, List.of(1L), 1L, 1L));
        verify(laneRepository, never()).save(any());
    }

    @Test
    void reordenarLanes_conIdAjenoAlPool_lanzaExcepcion() {
        when(poolService.obtenerPorIdYEmpresa(1L, 1L, 1L)).thenReturn(pool);
        when(laneRepository.findByPoolId(1L)).thenReturn(List.of(construirLane(1L, 0), construirLane(2L, 1)));

        assertThrows(IllegalArgumentException.class, () -> laneService.reordenarLanes(1L, List.of(1L, 99L), 1L, 1L));
    }

    @Test
    void reordenarLanes_conIdsRepetidos_lanzaExcepcion() {
        when(poolService.obtenerPorIdYEmpresa(1L, 1L, 1L)).thenReturn(pool);
        when(laneRepository.findByPoolId(1L)).thenReturn(List.of(construirLane(1L, 0), construirLane(2L, 1)));

        assertThrows(IllegalArgumentException.class, () -> laneService.reordenarLanes(1L, List.of(1L, 1L), 1L, 1L));
    }

    @Test
    void tieneLanes_reflejaElContenidoDelPool() {
        when(laneRepository.findByPoolId(1L)).thenReturn(List.of(construirLane(1L, 0)));
        when(laneRepository.findByPoolId(2L)).thenReturn(List.of());

        assertTrue(laneService.tieneLanes(1L));
        assertFalse(laneService.tieneLanes(2L));
    }

    @Test
    void existeEnProceso_reflejaLaBusqueda() {
        when(laneRepository.findByIdAndProcesoId(1L, 1L)).thenReturn(Optional.of(construirLane(1L, 0)));
        when(laneRepository.findByIdAndProcesoId(2L, 1L)).thenReturn(Optional.empty());

        assertTrue(laneService.existeEnProceso(1L, 1L));
        assertFalse(laneService.existeEnProceso(2L, 1L));
    }

    @Test
    void buscarProcesosDondeSeUsaRol_delegaEnElRepositorio() {
        List<Object[]> filas = new ArrayList<>();
        filas.add(new Object[] {1L, "Proceso"});
        when(laneRepository.buscarProcesosDondeSeUsaRol(5L)).thenReturn(filas);

        assertEquals(1, laneService.buscarProcesosDondeSeUsaRol(5L).size());
    }
}
