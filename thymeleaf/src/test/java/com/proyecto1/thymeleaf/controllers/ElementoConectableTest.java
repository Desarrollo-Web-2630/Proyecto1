package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.model.ElementoConectable;
import com.proyecto1.thymeleaf.services.ElementoConectableService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ElementoConectableController.class)
class ElementoConectableControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ElementoConectableService elementoConectableService;

    static class ElementoDePrueba extends ElementoConectable {
    }

    @Test
    void listarPorProceso_devuelveOkConLaListaMapeada() throws Exception {
        ElementoDePrueba elemento = new ElementoDePrueba();
        elemento.setId(1L);
        elemento.setNombre("Radicar solicitud");

        when(elementoConectableService.listarElementosPorProcesoYEmpresa(eq(1L), anyLong()))
                .thenReturn(List.of(elemento));

        mockMvc.perform(get("/api/v1/procesos/1/elementos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Radicar solicitud"));
    }

    @Test
    void obtenerPorId_inexistente_devuelve400() throws Exception {
        when(elementoConectableService.obtenerPorIdYEmpresa(eq(99L), anyLong()))
                .thenThrow(new IllegalArgumentException("Elemento no encontrado o sin autorización"));

        mockMvc.perform(get("/api/v1/procesos/1/elementos/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarPosicion_conCoordenadasValidas_devuelveOk() throws Exception {
        mockMvc.perform(patch("/api/v1/procesos/1/elementos/10/posicion")
                        .param("x", "150")
                        .param("y", "250"))
                .andExpect(status().isOk());

        verify(elementoConectableService).actualizarPosiciones(eq(10L), eq(150), eq(250), anyLong());
    }

    @Test
    void actualizarPosicion_sinParametroX_devuelve400() throws Exception {
        mockMvc.perform(patch("/api/v1/procesos/1/elementos/10/posicion")
                        .param("y", "250"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/v1/procesos/1/elementos/10"))
                .andExpect(status().isNoContent());

        verify(elementoConectableService).eliminarElemento(eq(10L), anyLong());
    }
}