package com.proyecto1.thymeleaf.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto1.thymeleaf.dto.ComparticionRequestDTO;
import com.proyecto1.thymeleaf.dto.ComparticionResponseDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.model.ProcesoCompartido;
import com.proyecto1.thymeleaf.repository.ProcesoCompartidoRepository;

@ExtendWith(MockitoExtension.class)
class ComparticionServiceTest {

    @Mock private ProcesoCompartidoRepository compartidoRepository;
    @Mock private ProcesoService procesoService;
    @Mock private EmpresaService empresaService;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks
    private ComparticionService comparticionService;

    private Proceso proceso;

    @BeforeEach
    void setUp() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Empresa propia");
        proceso = new Proceso();
        proceso.setId(7L);
        proceso.setEmpresa(empresa);
    }

    @Test
    void listar_devuelveLasEmpresasInvitadasActivas() {
        when(procesoService.obtenerPorIdYEmpresa(7L, 1L)).thenReturn(proceso);
        when(compartidoRepository.findActivosByProcesoId(7L))
                .thenReturn(List.of(new ProcesoCompartido(1L, proceso, 2L, true)));
        Empresa invitada = new Empresa();
        invitada.setId(2L);
        invitada.setNombre("Invitada");
        when(empresaService.obtenerPorId(2L)).thenReturn(invitada);

        ComparticionResponseDTO resultado = comparticionService.listar(7L, 1L);

        assertTrue(resultado.compartido());
        assertEquals(7L, resultado.procesoId());
        assertEquals(2L, resultado.empresasInvitadas().get(0).id());
        assertEquals("Invitada", resultado.empresasInvitadas().get(0).nombre());
    }

    @Test
    void actualizar_creaNuevasYDesactivaLasQueSalen() {
        ComparticionRequestDTO datos = new ComparticionRequestDTO(List.of(3L));
        ProcesoCompartido anterior = new ProcesoCompartido(1L, proceso, 2L, true);
        when(procesoService.obtenerPorIdYEmpresa(7L, 1L)).thenReturn(proceso);
        when(compartidoRepository.findByProcesoId(7L)).thenReturn(List.of(anterior));
        when(compartidoRepository.findActivosByProcesoId(7L)).thenReturn(List.of());

        comparticionService.actualizar(7L, datos, 1L);

        assertFalse(anterior.isActivo());
        ArgumentCaptor<ProcesoCompartido> captor = ArgumentCaptor.forClass(ProcesoCompartido.class);
        verify(compartidoRepository, times(2)).save(captor.capture());
        assertEquals(3L, captor.getAllValues().get(1).getEmpresaInvitadaId());
        assertTrue(captor.getAllValues().get(1).isActivo());
        verify(auditoriaService).registrar(org.mockito.ArgumentMatchers.eq("Proceso"),
                org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.eq("COMPARTIR"), any());
    }

    @Test
    void actualizar_empresaPropietariaComoInvitada_lanzaExcepcion() {
        ComparticionRequestDTO datos = new ComparticionRequestDTO(List.of(1L));
        when(procesoService.obtenerPorIdYEmpresa(7L, 1L)).thenReturn(proceso);

        assertThrows(IllegalArgumentException.class,
                () -> comparticionService.actualizar(7L, datos, 1L));
        verify(compartidoRepository, never()).save(any());
    }
}
