package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.GatewayDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Gateway;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.GatewayRepository;
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
class GatewayServiceTest {

    @Mock private GatewayRepository gatewayRepository;
    @Mock private ProcesoRepository procesoRepository;

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
        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));
        when(gatewayRepository.save(any(Gateway.class))).thenAnswer(inv -> inv.getArgument(0));

        Gateway resultado = gatewayService.crearGateway(datos, 1L, 1L);

        assertEquals("Decision de aprobacion", resultado.getNombre());
        assertEquals(Gateway.TipoGateway.EXCLUSIVO, resultado.getTipo());
    }

    @Test
    void crearGateway_conProcesoDeOtraEmpresa_lanzaExcepcion() {
        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> gatewayService.crearGateway(datos, 1L, 1L));
    }

    @Test
    void crearGateway_sinNombre_lanzaExcepcion() {
        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));
        datos.setNombre(" ");
        assertThrows(IllegalArgumentException.class, () -> gatewayService.crearGateway(datos, 1L, 1L));
    }

    @Test
    void crearGateway_sinTipo_lanzaExcepcion() {
        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));
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
}