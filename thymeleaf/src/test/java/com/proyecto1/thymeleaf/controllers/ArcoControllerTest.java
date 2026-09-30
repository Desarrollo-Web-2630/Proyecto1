package com.proyecto1.thymeleaf.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.dto.ArcoDTO;
import com.proyecto1.thymeleaf.model.Arco;
import com.proyecto1.thymeleaf.model.ElementoConectable;
import com.proyecto1.thymeleaf.services.ArcoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArcoController.class)
class ArcoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ArcoService arcoService;

    static class ElementoDePrueba extends ElementoConectable {
    }

    private Arco construirArco(Long id) {
        ElementoDePrueba origen = new ElementoDePrueba();
        origen.setId(10L);
        origen.setNombre("Radicar");
        ElementoDePrueba destino = new ElementoDePrueba();
        destino.setId(20L);
        destino.setNombre("Revisar");

        Arco arco = new Arco();
        arco.setId(id);
        arco.setNombre("flujo-1");
        arco.setOrigen(origen);
        arco.setDestino(destino);
        return arco;
    }

    @Test
    void listar_devuelveOkConLaListaMapeada() throws Exception {
        when(arcoService.listarPorProcesoYEmpresa(eq(1L), anyLong())).thenReturn(List.of(construirArco(1L)));

        mockMvc.perform(get("/api/v1/procesos/1/arcos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("flujo-1"));
    }

    @Test
    void obtener_existente_devuelveOk() throws Exception {
        when(arcoService.obtenerPorIdYEmpresa(eq(1L), anyLong())).thenReturn(construirArco(1L));

        mockMvc.perform(get("/api/v1/procesos/1/arcos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origenNombre").value("Radicar"));
    }

    @Test
    void crear_conDatosValidos_devuelve201() throws Exception {
        ArcoDTO datos = new ArcoDTO();
        datos.setNombre("flujo-1");
        datos.setOrigenId(10L);
        datos.setDestinoId(20L);

        when(arcoService.crearArco(any(ArcoDTO.class), eq(1L), anyLong())).thenReturn(construirArco(5L));

        mockMvc.perform(post("/api/v1/procesos/1/arcos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void crear_sinOrigen_devuelve400PorValidacionDeBean() throws Exception {
        ArcoDTO datos = new ArcoDTO();
        datos.setNombre("flujo-1");
        datos.setDestinoId(20L);

        mockMvc.perform(post("/api/v1/procesos/1/arcos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isBadRequest());

        verify(arcoService, never()).crearArco(any(), anyLong(), anyLong());
    }

    @Test
    void actualizar_conDatosValidos_devuelveOk() throws Exception {
        ArcoDTO datos = new ArcoDTO();
        datos.setNombre("flujo-actualizado");
        datos.setOrigenId(10L);
        datos.setDestinoId(20L);

        when(arcoService.actualizarArco(eq(1L), any(ArcoDTO.class), anyLong())).thenReturn(construirArco(1L));

        mockMvc.perform(put("/api/v1/procesos/1/arcos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk());
    }

    @Test
    void eliminar_sinAdvertencia_devuelveOkConSoloMensaje() throws Exception {
        when(arcoService.advertenciaAlEliminar(eq(1L), anyLong())).thenReturn(null);

        mockMvc.perform(delete("/api/v1/procesos/1/arcos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Arco eliminado correctamente."))
                .andExpect(jsonPath("$.advertencia").doesNotExist());

        verify(arcoService).eliminarArco(eq(1L), anyLong());
    }

    @Test
    void eliminar_conAdvertencia_devuelveOkConAmbosCampos() throws Exception {
        when(arcoService.advertenciaAlEliminar(eq(1L), anyLong()))
                .thenReturn("'Revisar' se queda sin ningún camino de entrada.");

        mockMvc.perform(delete("/api/v1/procesos/1/arcos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.advertencia").exists());
    }
}