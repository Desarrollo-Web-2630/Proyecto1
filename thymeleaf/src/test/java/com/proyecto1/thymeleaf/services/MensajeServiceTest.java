package com.proyecto1.thymeleaf.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.proyecto1.thymeleaf.dto.MensajeDTO;
import com.proyecto1.thymeleaf.model.Actividad;
import com.proyecto1.thymeleaf.model.Mensaje;
import com.proyecto1.thymeleaf.model.Pool;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.MensajeRepository;

@ExtendWith(MockitoExtension.class)
class MensajeServiceTest {

    @Mock private MensajeRepository mensajeRepository;
    @Mock private PoolService poolService;
    @Mock private ElementoConectableService elementoService;
    @Mock private ProcesoService procesoService;

    @InjectMocks
    private MensajeService mensajeService;

    private Proceso proceso;
    private Pool origen;
    private Pool destino;
    private MensajeDTO datos;

    @BeforeEach
    void setUp() {
        proceso = new Proceso();
        proceso.setId(1L);

        origen = new Pool();
        origen.setId(1L);
        destino = new Pool();
        destino.setId(2L);

        datos = new MensajeDTO();
        datos.setNombre("  Notificar  ");
        datos.setClaveCorrelacion("  numero_guia  ");
        datos.setPoolOrigenId(1L);
        datos.setPoolDestinoId(2L);
    }

    private void prepararPools() {
        when(poolService.obtenerPorIdYEmpresa(1L, 1L, 1L)).thenReturn(origen);
        when(poolService.obtenerPorIdYEmpresa(2L, 1L, 1L)).thenReturn(destino);
    }

    private Mensaje construirMensaje(Long id) {
        Mensaje mensaje = new Mensaje();
        mensaje.setId(id);
        mensaje.setNombre("Notificar");
        mensaje.setClaveCorrelacion("numero_guia");
        mensaje.setProceso(proceso);
        mensaje.setPoolOrigen(origen);
        mensaje.setPoolDestino(destino);
        return mensaje;
    }

    private Actividad construirActividad(Long id, Proceso procesoDeLaActividad) {
        Actividad actividad = new Actividad();
        actividad.setId(id);
        actividad.setProceso(procesoDeLaActividad);
        return actividad;
    }

    @Test
    void listarPorProcesoYEmpresa_devuelveLosMensajes() {
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenReturn(proceso);
        when(mensajeRepository.findByProcesoId(1L)).thenReturn(List.of(construirMensaje(1L)));

        assertEquals(1, mensajeService.listarPorProcesoYEmpresa(1L, 1L).size());
    }

    @Test
    void obtenerPorIdYEmpresa_existente_devuelveElMensaje() {
        when(mensajeRepository.findByIdAndProcesoIdAndEmpresaId(1L, 1L, 1L)).thenReturn(Optional.of(construirMensaje(1L)));

        assertEquals(1L, mensajeService.obtenerPorIdYEmpresa(1L, 1L, 1L).getId());
    }

    @Test
    void obtenerPorIdYEmpresa_inexistente_lanzaExcepcion() {
        when(mensajeRepository.findByIdAndProcesoIdAndEmpresaId(9L, 1L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> mensajeService.obtenerPorIdYEmpresa(9L, 1L, 1L));
    }

    @Test
    void crearMensaje_sinElementoOrigen_guardaConDatosRecortados() {
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenReturn(proceso);
        prepararPools();
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(inv -> inv.getArgument(0));

        Mensaje resultado = mensajeService.crearMensaje(datos, 1L, 1L);

        assertEquals("Notificar", resultado.getNombre());
        assertEquals("numero_guia", resultado.getClaveCorrelacion());
        assertEquals(origen, resultado.getPoolOrigen());
        assertEquals(destino, resultado.getPoolDestino());
        assertNull(resultado.getElementoOrigen());
    }

    @Test
    void crearMensaje_conElementoDelMismoProceso_loAsocia() {
        Actividad actividad = construirActividad(7L, proceso);
        datos.setElementoOrigenId(7L);
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenReturn(proceso);
        prepararPools();
        when(elementoService.obtenerPorIdYEmpresa(7L, 1L)).thenReturn(actividad);
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(inv -> inv.getArgument(0));

        Mensaje resultado = mensajeService.crearMensaje(datos, 1L, 1L);

        assertEquals(actividad, resultado.getElementoOrigen());
    }

    @Test
    void crearMensaje_conElementoDeOtroProceso_lanzaExcepcion() {
        Proceso otroProceso = new Proceso();
        otroProceso.setId(2L);
        datos.setElementoOrigenId(7L);
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenReturn(proceso);
        prepararPools();
        when(elementoService.obtenerPorIdYEmpresa(7L, 1L)).thenReturn(construirActividad(7L, otroProceso));

        assertThrows(IllegalArgumentException.class, () -> mensajeService.crearMensaje(datos, 1L, 1L));
        verify(mensajeRepository, never()).save(any());
    }

    @Test
    void crearMensaje_conMismoPoolEnAmbosExtremos_lanzaExcepcion() {
        datos.setPoolDestinoId(1L);
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenReturn(proceso);
        when(poolService.obtenerPorIdYEmpresa(1L, 1L, 1L)).thenReturn(origen);

        assertThrows(IllegalArgumentException.class, () -> mensajeService.crearMensaje(datos, 1L, 1L));
        verify(mensajeRepository, never()).save(any());
    }

    @Test
    void crearMensaje_conPoolAjeno_lanzaExcepcion() {
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenReturn(proceso);
        when(poolService.obtenerPorIdYEmpresa(1L, 1L, 1L)).thenThrow(new IllegalArgumentException("ajeno"));

        assertThrows(IllegalArgumentException.class, () -> mensajeService.crearMensaje(datos, 1L, 1L));
    }

    @Test
    void actualizarMensaje_cambiaLosDatos() {
        Mensaje existente = construirMensaje(1L);
        when(mensajeRepository.findByIdAndProcesoIdAndEmpresaId(1L, 1L, 1L)).thenReturn(Optional.of(existente));
        prepararPools();
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(inv -> inv.getArgument(0));
        datos.setNombre("Nuevo nombre");
        datos.setClaveCorrelacion("nueva_clave");

        Mensaje resultado = mensajeService.actualizarMensaje(1L, datos, 1L, 1L);

        assertEquals("Nuevo nombre", resultado.getNombre());
        assertEquals("nueva_clave", resultado.getClaveCorrelacion());
    }

    @Test
    void actualizarMensaje_conMismoPoolEnAmbosExtremos_lanzaExcepcion() {
        datos.setPoolDestinoId(1L);
        when(mensajeRepository.findByIdAndProcesoIdAndEmpresaId(1L, 1L, 1L)).thenReturn(Optional.of(construirMensaje(1L)));
        when(poolService.obtenerPorIdYEmpresa(1L, 1L, 1L)).thenReturn(origen);

        assertThrows(IllegalArgumentException.class, () -> mensajeService.actualizarMensaje(1L, datos, 1L, 1L));
    }

    @Test
    void eliminarMensaje_existente_loElimina() {
        Mensaje mensaje = construirMensaje(1L);
        when(mensajeRepository.findByIdAndProcesoIdAndEmpresaId(1L, 1L, 1L)).thenReturn(Optional.of(mensaje));

        mensajeService.eliminarMensaje(1L, 1L, 1L);

        verify(mensajeRepository).delete(mensaje);
    }
}
