package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.ArcoDTO;
import com.proyecto1.thymeleaf.model.Arco;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.ElementoConectable;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.ArcoRepository;
import com.proyecto1.thymeleaf.repository.ElementoConectableRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArcoServiceTest {

    @Mock private ArcoRepository arcoRepository;
    @Mock private ElementoConectableRepository elementoRepository;
    @Mock private ProcesoRepository procesoRepository;

    @InjectMocks
    private ArcoService arcoService;

    private Proceso proceso;
    private ElementoConectable origen;
    private ElementoConectable destino;
    private ArcoDTO datos;

    // Subclase minima solo para poder instanciar el abstracto ElementoConectable en el test
    static class ElementoDePrueba extends ElementoConectable {
    }

    @BeforeEach
    void setUp() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);

        origen = new ElementoDePrueba();
        origen.setId(10L);
        origen.setNombre("Radicar solicitud");
        origen.setProceso(proceso);

        destino = new ElementoDePrueba();
        destino.setId(20L);
        destino.setNombre("Revisar solicitud");
        destino.setProceso(proceso);

        datos = new ArcoDTO();
        datos.setNombre("flujo-1");
        datos.setOrigenId(10L);
        datos.setDestinoId(20L);
    }

    @Test
    void crearArco_conDatosValidos_seGuarda() {
        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));
        when(elementoRepository.findByIdAndProcesoEmpresaId(10L, 1L)).thenReturn(Optional.of(origen));
        when(elementoRepository.findByIdAndProcesoEmpresaId(20L, 1L)).thenReturn(Optional.of(destino));
        when(arcoRepository.findByOrigenId(10L)).thenReturn(List.of());
        when(arcoRepository.save(any(Arco.class))).thenAnswer(inv -> inv.getArgument(0));

        Arco resultado = arcoService.crearArco(datos, 1L, 1L);

        assertEquals(origen, resultado.getOrigen());
        assertEquals(destino, resultado.getDestino());
    }

    @Test
    void crearArco_conOrigenIgualADestino_lanzaExcepcion() {
        datos.setOrigenId(10L);
        datos.setDestinoId(10L);

        assertThrows(IllegalArgumentException.class, () -> arcoService.crearArco(datos, 1L, 1L));
    }

    @Test
    void crearArco_sinOrigenODestino_lanzaExcepcion() {
        datos.setOrigenId(null);
        assertThrows(IllegalArgumentException.class, () -> arcoService.crearArco(datos, 1L, 1L));
    }

    @Test
    void crearArco_conElementosDeOtroProceso_lanzaExcepcion() {
        Proceso otroProceso = new Proceso();
        otroProceso.setId(2L);
        destino.setProceso(otroProceso);

        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));
        when(elementoRepository.findByIdAndProcesoEmpresaId(10L, 1L)).thenReturn(Optional.of(origen));
        when(elementoRepository.findByIdAndProcesoEmpresaId(20L, 1L)).thenReturn(Optional.of(destino));

        assertThrows(IllegalArgumentException.class, () -> arcoService.crearArco(datos, 1L, 1L));
    }

    @Test
    void crearArco_duplicado_lanzaExcepcion() {
        Arco existente = new Arco();
        existente.setId(99L);
        existente.setDestino(destino);

        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));
        when(elementoRepository.findByIdAndProcesoEmpresaId(10L, 1L)).thenReturn(Optional.of(origen));
        when(elementoRepository.findByIdAndProcesoEmpresaId(20L, 1L)).thenReturn(Optional.of(destino));
        when(arcoRepository.findByOrigenId(10L)).thenReturn(List.of(existente));

        assertThrows(IllegalArgumentException.class, () -> arcoService.crearArco(datos, 1L, 1L));
    }

    @Test
    void obtenerPorIdYEmpresa_inexistente_lanzaExcepcion() {
        when(arcoRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> arcoService.obtenerPorIdYEmpresa(5L, 1L));
    }

    @Test
    void eliminarArco_existente_invocaDelete() {
        Arco arco = new Arco();
        arco.setId(5L);
        when(arcoRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.of(arco));

        arcoService.eliminarArco(5L, 1L);

        verify(arcoRepository).delete(arco);
    }

    @Test
    void advertenciaAlEliminar_dejaElementosSueltos_devuelveMensaje() {
        Arco arco = new Arco();
        arco.setId(5L);
        arco.setOrigen(origen);
        arco.setDestino(destino);

        when(arcoRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.of(arco));
        when(arcoRepository.findByDestinoId(20L)).thenReturn(List.of(arco));
        when(arcoRepository.findByOrigenId(10L)).thenReturn(List.of(arco));

        String advertencia = arcoService.advertenciaAlEliminar(5L, 1L);

        assertNotNull(advertencia);
        assertTrue(advertencia.contains(destino.getNombre()));
        assertTrue(advertencia.contains(origen.getNombre()));
    }

    @Test
    void advertenciaAlEliminar_sinDejarElementosSueltos_devuelveNulo() {
        Arco arco = new Arco();
        arco.setId(5L);
        arco.setOrigen(origen);
        arco.setDestino(destino);

        Arco otroQueLlegaAlDestino = new Arco();
        otroQueLlegaAlDestino.setId(6L);
        Arco otroQueSaleDelOrigen = new Arco();
        otroQueSaleDelOrigen.setId(7L);

        when(arcoRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.of(arco));
        when(arcoRepository.findByDestinoId(20L)).thenReturn(List.of(arco, otroQueLlegaAlDestino));
        when(arcoRepository.findByOrigenId(10L)).thenReturn(List.of(arco, otroQueSaleDelOrigen));

        assertNull(arcoService.advertenciaAlEliminar(5L, 1L));
    }

    @Test
    void eliminarArcosDeElemento_borraLosConectadosYDevuelveCantidad() {
        Arco comoOrigen = new Arco();
        comoOrigen.setId(1L);
        Arco comoDestino = new Arco();
        comoDestino.setId(2L);

        when(arcoRepository.findByOrigenId(10L)).thenReturn(List.of(comoOrigen));
        when(arcoRepository.findByDestinoId(10L)).thenReturn(List.of(comoDestino));

        int eliminados = arcoService.eliminarArcosDeElemento(10L);

        assertEquals(2, eliminados);
        verify(arcoRepository).delete(comoOrigen);
        verify(arcoRepository).delete(comoDestino);
    }
}