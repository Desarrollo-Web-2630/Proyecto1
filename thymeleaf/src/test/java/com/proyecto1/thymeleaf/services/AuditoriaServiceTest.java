package com.proyecto1.thymeleaf.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto1.thymeleaf.dto.HistorialCambioDTO;
import com.proyecto1.thymeleaf.model.HistorialCambio;
import com.proyecto1.thymeleaf.repository.HistorialCambioRepository;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock private HistorialCambioRepository historialRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @Test
    void registrar_sinSesion_guardaComoSistema() {
        String descripcion = "x".repeat(1200);

        auditoriaService.registrar("Proceso", 7L, "CREAR", descripcion);

        ArgumentCaptor<HistorialCambio> captor = ArgumentCaptor.forClass(HistorialCambio.class);
        verify(historialRepository).save(captor.capture());
        HistorialCambio cambio = captor.getValue();
        assertEquals(0L, cambio.getEmpresaId());
        assertEquals("sistema", cambio.getUsuarioCorreo());
        assertEquals(1000, cambio.getDescripcion().length());
        assertEquals("Proceso", cambio.getEntidad());
        assertEquals("CREAR", cambio.getAccion());
        assertTrue(cambio.getFecha() != null);
    }

    @Test
    void historialDeEmpresa_mapeaLosCambios() {
        Instant fecha = Instant.parse("2026-01-01T10:00:00Z");
        when(historialRepository.findByEmpresa(1L)).thenReturn(List.of(
                new HistorialCambio(3L, 1L, "Proceso", 7L, "EDITAR", "Editado", "juan@correo.com", fecha)));

        List<HistorialCambioDTO> resultado = auditoriaService.historialDeEmpresa(1L);

        assertEquals(1, resultado.size());
        assertEquals("EDITAR", resultado.get(0).accion());
        assertEquals("juan@correo.com", resultado.get(0).usuarioCorreo());
        assertEquals(fecha, resultado.get(0).fecha());
    }

    @Test
    void historialDeEntidad_filtraPorEmpresaEntidadYId() {
        when(historialRepository.findByEmpresaYEntidad(1L, "Proceso", 7L)).thenReturn(List.of());

        assertTrue(auditoriaService.historialDeEntidad("Proceso", 7L, 1L).isEmpty());
        verify(historialRepository).findByEmpresaYEntidad(1L, "Proceso", 7L);
    }
}
