package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.GatewayDTO;
import com.proyecto1.thymeleaf.model.Arco;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Gateway;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.ArcoRepository;
import com.proyecto1.thymeleaf.repository.GatewayRepository;
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
class GatewayServiceTest {

    @Mock private GatewayRepository gatewayRepository;
    @Mock private ProcesoService procesoService;
    @Mock private ArcoRepository arcoRepository;
    @Mock private ArcoService arcoService;

    @Mock private AuditoriaService auditoriaService;
    @Mock private AccesoProcesoService accesoProcesoService;

    @InjectMocks
    private GatewayService gatewayService;

    private Proceso proceso;
    private GatewayDTO datos;

    @BeforeEach
    void setUp() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);

        datos = new GatewayDTO();
        datos.setNombre("Decision de aprobacion");
        datos.setTipo(Gateway.TipoGateway.EXCLUSIVO);
    }

    @Test
    void crearGateway_conDatosValidos_seGuarda() {
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenReturn(proceso);
        when(gatewayRepository.save(any(Gateway.class))).thenAnswer(inv -> inv.getArgument(0));

        Gateway resultado = gatewayService.crearGateway(datos, 1L, 1L);

        assertEquals("Decision de aprobacion", resultado.getNombre());
        assertEquals(Gateway.TipoGateway.EXCLUSIVO, resultado.getTipo());
    }

    @Test
    void crearGateway_conProcesoDeOtraEmpresa_lanzaExcepcion() {
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenThrow(new IllegalArgumentException("El proceso no existe o no pertenece a su empresa"));
        assertThrows(IllegalArgumentException.class, () -> gatewayService.crearGateway(datos, 1L, 1L));
    }

    @Test
    void crearGateway_sinNombre_lanzaExcepcion() {
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenReturn(proceso);
        datos.setNombre(" ");
        assertThrows(IllegalArgumentException.class, () -> gatewayService.crearGateway(datos, 1L, 1L));
    }

    @Test
    void crearGateway_sinTipo_lanzaExcepcion() {
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenReturn(proceso);
        datos.setTipo(null);
        assertThrows(IllegalArgumentException.class, () -> gatewayService.crearGateway(datos, 1L, 1L));
    }

    @Test
    void obtenerPorIdYEmpresa_inexistente_lanzaExcepcion() {
        when(gatewayRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> gatewayService.obtenerPorIdYEmpresa(5L, 1L));
    }

    @Test
    void actualizarGateway_conDatosValidos_actualizaNombreYTipo() {
        Gateway existente = new Gateway();
        existente.setId(5L);
        existente.setProceso(proceso);

        when(gatewayRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.of(existente));
        when(gatewayRepository.save(any(Gateway.class))).thenAnswer(inv -> inv.getArgument(0));

        GatewayDTO nuevosDatos = new GatewayDTO();
        nuevosDatos.setNombre("Nuevo nombre");
        nuevosDatos.setTipo(Gateway.TipoGateway.PARALELO);

        Gateway resultado = gatewayService.actualizarGateway(5L, nuevosDatos, 1L);

        assertEquals("Nuevo nombre", resultado.getNombre());
        assertEquals(Gateway.TipoGateway.PARALELO, resultado.getTipo());
    }

    @Test
    void eliminarGateway_existente_invocaDelete() {
        Gateway gateway = new Gateway();
        gateway.setId(5L);
        gateway.setProceso(proceso);

        when(gatewayRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.of(gateway));

        gatewayService.eliminarGateway(5L, 1L);

        verify(gatewayRepository).delete(gateway);
    }

    @Test
    void crearGateway_conPosicion_guardaLasCoordenadas() {
        datos.setPosicionX(120);
        datos.setPosicionY(40);
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenReturn(proceso);
        when(gatewayRepository.save(any(Gateway.class))).thenAnswer(inv -> inv.getArgument(0));

        Gateway resultado = gatewayService.crearGateway(datos, 1L, 1L);

        assertEquals(120, resultado.getPosicionX());
        assertEquals(40, resultado.getPosicionY());
    }

    @Test
    void crearGateway_sinPosicion_usaCero() {
        when(procesoService.obtenerPorIdYEmpresa(1L, 1L)).thenReturn(proceso);
        when(gatewayRepository.save(any(Gateway.class))).thenAnswer(inv -> inv.getArgument(0));

        Gateway resultado = gatewayService.crearGateway(datos, 1L, 1L);

        assertEquals(0, resultado.getPosicionX());
        assertEquals(0, resultado.getPosicionY());
    }

    private Arco arcoConCondicion(String condicion) {
        Arco arco = new Arco();
        arco.setCondicion(condicion);
        return arco;
    }

    @Test
    void validarSalidasProceso_menosDeDosSalidas_lanzaExcepcion() {
        Gateway gateway = new Gateway();
        gateway.setId(5L);
        gateway.setNombre("Decision");
        gateway.setTipo(Gateway.TipoGateway.EXCLUSIVO);
        when(gatewayRepository.findByProcesoId(1L)).thenReturn(List.of(gateway));
        when(arcoRepository.findByOrigenId(5L)).thenReturn(List.of(arcoConCondicion("si")));

        assertThrows(IllegalArgumentException.class, () -> gatewayService.validarSalidasProceso(1L));
    }

    @Test
    void validarSalidasProceso_salidaSinCondicion_lanzaExcepcion() {
        Gateway gateway = new Gateway();
        gateway.setId(5L);
        gateway.setNombre("Decision");
        gateway.setTipo(Gateway.TipoGateway.INCLUSIVO);
        when(gatewayRepository.findByProcesoId(1L)).thenReturn(List.of(gateway));
        when(arcoRepository.findByOrigenId(5L))
                .thenReturn(List.of(arcoConCondicion("si"), arcoConCondicion("  ")));

        assertThrows(IllegalArgumentException.class, () -> gatewayService.validarSalidasProceso(1L));
    }

    @Test
    void validarSalidasProceso_paraleloSinCondicion_noLanza() {
        Gateway gateway = new Gateway();
        gateway.setId(5L);
        gateway.setTipo(Gateway.TipoGateway.PARALELO);
        when(gatewayRepository.findByProcesoId(1L)).thenReturn(List.of(gateway));
        when(arcoRepository.findByOrigenId(5L))
                .thenReturn(List.of(arcoConCondicion(null), arcoConCondicion(null)));

        gatewayService.validarSalidasProceso(1L);
    }

    @Test
    void actualizarGateway_cambioAParalelo_limpiaCondiciones() {
        Gateway existente = new Gateway();
        existente.setId(5L);
        existente.setProceso(proceso);
        existente.setTipo(Gateway.TipoGateway.EXCLUSIVO);
        Arco salida = arcoConCondicion("si");
        when(gatewayRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.of(existente));
        when(gatewayRepository.save(any(Gateway.class))).thenAnswer(inv -> inv.getArgument(0));
        when(arcoRepository.findByOrigenId(5L)).thenReturn(List.of(salida));
        GatewayDTO paralelo = new GatewayDTO();
        paralelo.setNombre("Paralelo");
        paralelo.setTipo(Gateway.TipoGateway.PARALELO);

        gatewayService.actualizarGateway(5L, paralelo, 1L);

        assertNull(salida.getCondicion());
        verify(arcoRepository).save(salida);
    }

    @Test
    void advertenciaAlEliminar_conArcos_devuelveMensaje() {
        Gateway gateway = new Gateway();
        gateway.setId(5L);
        gateway.setProceso(proceso);
        when(gatewayRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.of(gateway));
        when(arcoRepository.findByOrigenId(5L)).thenReturn(List.of(arcoConCondicion("si")));
        when(arcoRepository.findByDestinoId(5L)).thenReturn(List.of(arcoConCondicion(null)));

        String aviso = gatewayService.advertenciaAlEliminar(5L, 1L);

        assertTrue(aviso.contains("2 arco"));
    }

    @Test
    void eliminarGateway_eliminaTambienSusArcos() {
        Gateway gateway = new Gateway();
        gateway.setId(5L);
        gateway.setProceso(proceso);
        when(gatewayRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.of(gateway));

        gatewayService.eliminarGateway(5L, 1L);

        verify(arcoService).eliminarArcosDeElemento(5L);
        verify(gatewayRepository).delete(gateway);
    }
}
