package com.proyecto1.thymeleaf.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.ProcesoCompartidoRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;

@ExtendWith(MockitoExtension.class)
class AccesoProcesoServiceTest {

    @Mock private ProcesoRepository procesoRepository;
    @Mock private ProcesoCompartidoRepository compartidoRepository;

    @InjectMocks
    private AccesoProcesoService accesoProcesoService;

    private Proceso proceso;

    @BeforeEach
    void setUp() {
        proceso = new Proceso();
        proceso.setId(7L);
        proceso.setEstado(Proceso.EstadoProceso.PUBLICADO);
    }

    @Test
    void verificarLectura_procesoPropio_devuelveElProceso() {
        when(procesoRepository.findByIdAndEmpresaId(7L, 1L)).thenReturn(Optional.of(proceso));

        assertEquals(proceso, accesoProcesoService.verificarLectura(7L, 1L));
    }

    @Test
    void verificarLectura_procesoCompartido_devuelveElProceso() {
        when(procesoRepository.findByIdAndEmpresaId(7L, 2L)).thenReturn(Optional.empty());
        when(compartidoRepository.existsByProcesoIdAndEmpresaInvitadaIdAndActivoTrue(7L, 2L)).thenReturn(true);
        when(procesoRepository.findById(7L)).thenReturn(Optional.of(proceso));

        assertEquals(proceso, accesoProcesoService.verificarLectura(7L, 2L));
    }

    @Test
    void verificarLectura_sinAcceso_lanzaAccessDenied() {
        when(procesoRepository.findByIdAndEmpresaId(7L, 3L)).thenReturn(Optional.empty());
        when(compartidoRepository.existsByProcesoIdAndEmpresaInvitadaIdAndActivoTrue(7L, 3L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> accesoProcesoService.verificarLectura(7L, 3L));
    }

    @Test
    void verificarEscritura_procesoInactivo_lanzaAccessDenied() {
        proceso.setEstado(Proceso.EstadoProceso.INACTIVO);
        when(procesoRepository.findByIdAndEmpresaId(7L, 1L)).thenReturn(Optional.of(proceso));

        assertThrows(AccessDeniedException.class, () -> accesoProcesoService.verificarEscritura(7L, 1L));
    }

    @Test
    void verificarEscritura_compartidoDeOtraEmpresa_lanzaAccessDenied() {
        when(procesoRepository.findByIdAndEmpresaId(7L, 2L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> accesoProcesoService.verificarEscritura(7L, 2L));
    }

    @Test
    void esSoloLectura_distinguePropioDeCompartido() {
        when(procesoRepository.findByIdAndEmpresaId(7L, 1L)).thenReturn(Optional.of(proceso));
        when(procesoRepository.findByIdAndEmpresaId(7L, 2L)).thenReturn(Optional.empty());
        when(compartidoRepository.existsByProcesoIdAndEmpresaInvitadaIdAndActivoTrue(7L, 2L)).thenReturn(true);

        assertFalse(accesoProcesoService.esSoloLectura(7L, 1L));
        assertTrue(accesoProcesoService.esSoloLectura(7L, 2L));
    }

    @Test
    void idsAccesibles_unionSinDuplicados() {
        when(procesoRepository.findIdsByEmpresaId(1L)).thenReturn(List.of(1L, 2L));
        when(compartidoRepository.findIdsCompartidosCon(1L)).thenReturn(List.of(2L, 9L));

        assertEquals(List.of(1L, 2L, 9L), accesoProcesoService.idsAccesibles(1L));
    }
}
