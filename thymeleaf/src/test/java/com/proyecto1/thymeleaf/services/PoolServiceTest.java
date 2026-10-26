package com.proyecto1.thymeleaf.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto1.thymeleaf.dto.PoolDTO;
import com.proyecto1.thymeleaf.model.Pool;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.PoolRepository;

@ExtendWith(MockitoExtension.class)
class PoolServiceTest {

    @Mock private PoolRepository poolRepository;
    @Mock private LaneService laneService;
    @Mock private ProcesoService procesoService;

    @InjectMocks
    private PoolService poolService;

    private Proceso proceso;
    private PoolDTO datos;

    @BeforeEach
    void setUp() {
        proceso = new Proceso();
        proceso.setId(1L);

        datos = new PoolDTO();
        datos.setNombre("  Empresa  ");
        datos.setTipo(Pool.TipoPool.PROPIETARIO);
    }

    private Pool construirPool(Long id) {
        Pool pool = new Pool();
        pool.setId(id);
        pool.setNombre("Empresa");
        pool.setTipo(Pool.TipoPool.PROPIETARIO);
        pool.setProceso(proceso);
        return pool;
    }

    @Test
    void listarPorProcesoYEmpresa_devuelveLosPoolsDelProceso() {
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenReturn(proceso);
        when(poolRepository.findByProcesoId(1L)).thenReturn(List.of(construirPool(1L)));

        assertEquals(1, poolService.listarPorProcesoYEmpresa(1L, 1L).size());
    }

    @Test
    void listarPorProcesoYEmpresa_conProcesoAjeno_lanzaExcepcion() {
        when(procesoService.obtenerPorIdYEmpresa(1L, 2L)).thenThrow(new IllegalArgumentException("ajeno"));

        assertThrows(IllegalArgumentException.class, () -> poolService.listarPorProcesoYEmpresa(1L, 2L));
    }

    @Test
    void obtenerPorIdYEmpresa_existente_devuelveElPool() {
        when(poolRepository.findByIdAndProcesoIdAndEmpresaId(1L, 1L, 1L)).thenReturn(Optional.of(construirPool(1L)));

        assertEquals(1L, poolService.obtenerPorIdYEmpresa(1L, 1L, 1L).getId());
    }

    @Test
    void obtenerPorIdYEmpresa_inexistente_lanzaExcepcion() {
        when(poolRepository.findByIdAndProcesoIdAndEmpresaId(9L, 1L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> poolService.obtenerPorIdYEmpresa(9L, 1L, 1L));
    }

    @Test
    void crearPool_conDatosValidos_guardaConNombreRecortado() {
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenReturn(proceso);
        when(poolRepository.save(any(Pool.class))).thenAnswer(inv -> inv.getArgument(0));

        Pool resultado = poolService.crearPool(datos, 1L, 1L);

        assertEquals("Empresa", resultado.getNombre());
        assertEquals(Pool.TipoPool.PROPIETARIO, resultado.getTipo());
        assertEquals(proceso, resultado.getProceso());
    }

    @Test
    void crearPool_conProcesoAjeno_noGuarda() {
        when(procesoService.obtenerPorIdYEmpresa(1L, 2L)).thenThrow(new IllegalArgumentException("ajeno"));

        assertThrows(IllegalArgumentException.class, () -> poolService.crearPool(datos, 1L, 2L));
        verify(poolRepository, never()).save(any());
    }

    @Test
    void actualizarPool_cambiaNombreYTipo() {
        when(poolRepository.findByIdAndProcesoIdAndEmpresaId(1L, 1L, 1L)).thenReturn(Optional.of(construirPool(1L)));
        when(poolRepository.save(any(Pool.class))).thenAnswer(inv -> inv.getArgument(0));
        datos.setNombre("Proveedor X");
        datos.setTipo(Pool.TipoPool.PROVEEDOR);

        Pool resultado = poolService.actualizarPool(1L, datos, 1L, 1L);

        assertEquals("Proveedor X", resultado.getNombre());
        assertEquals(Pool.TipoPool.PROVEEDOR, resultado.getTipo());
    }

    @Test
    void actualizarPool_inexistente_lanzaExcepcion() {
        when(poolRepository.findByIdAndProcesoIdAndEmpresaId(9L, 1L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> poolService.actualizarPool(9L, datos, 1L, 1L));
    }

    @Test
    void eliminarPool_sinLanes_loElimina() {
        Pool pool = construirPool(1L);
        when(poolRepository.findByIdAndProcesoIdAndEmpresaId(1L, 1L, 1L)).thenReturn(Optional.of(pool));
        when(laneService.tieneLanes(1L)).thenReturn(false);

        poolService.eliminarPool(1L, 1L, 1L);

        verify(poolRepository).delete(pool);
    }

    @Test
    void eliminarPool_conLanes_lanzaExcepcionYNoElimina() {
        when(poolRepository.findByIdAndProcesoIdAndEmpresaId(1L, 1L, 1L)).thenReturn(Optional.of(construirPool(1L)));
        when(laneService.tieneLanes(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> poolService.eliminarPool(1L, 1L, 1L));
        verify(poolRepository, never()).delete(any());
    }
}
