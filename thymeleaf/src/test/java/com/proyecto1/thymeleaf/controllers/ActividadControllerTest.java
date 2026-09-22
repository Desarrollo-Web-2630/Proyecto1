package com.proyecto1.thymeleaf.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.dto.ActividadRequestDTO;
import com.proyecto1.thymeleaf.dto.ActividadResponseDTO;
import com.proyecto1.thymeleaf.services.ActividadService;
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

@WebMvcTest(ActividadController.class)
class ActividadControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ActividadService actividadService;

    @Test
    void listar_devuelveOkConLaListaDelService() throws Exception {
        ActividadResponseDTO dto = new ActividadResponseDTO(1L, "Radicar solicitud", "Manual", 0, 0, 1L);
        when(actividadService.listarPorProcesoYEmpresa(eq(1L), anyLong())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/procesos/1/actividades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Radicar solicitud"));
    }

    @Test
    void obtener_existente_devuelveOk() throws Exception {
        ActividadResponseDTO dto = new ActividadResponseDTO(1L, "Radicar solicitud", "Manual", 0, 0, 1L);
        when(actividadService.obtenerPorIdYEmpresa(eq(1L), anyLong())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/procesos/1/actividades/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Radicar solicitud"));
    }

    @Test
    void obtener_inexistente_devuelve400() throws Exception {
        when(actividadService.obtenerPorIdYEmpresa(eq(99L), anyLong()))
                .thenThrow(new IllegalArgumentException("La actividad no existe o no pertenece a su empresa"));

        mockMvc.perform(get("/api/v1/procesos/1/actividades/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_conDatosValidos_devuelve201() throws Exception {
        ActividadRequestDTO datos = new ActividadRequestDTO();
        datos.setNombre("Tarea 1");
        datos.setTipoActividad("Manual");
        datos.setLaneId(1L);
        datos.setPosicionX(0);
        datos.setPosicionY(0);

        ActividadResponseDTO creada = new ActividadResponseDTO(5L, "Tarea 1", "Manual", 0, 0, 1L);
        when(actividadService.crearActividad(any(ActividadRequestDTO.class), eq(1L), anyLong())).thenReturn(creada);

        mockMvc.perform(post("/api/v1/procesos/1/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void crear_sinNombre_devuelve400PorValidacionDeBean() throws Exception {
        ActividadRequestDTO datos = new ActividadRequestDTO();
        datos.setTipoActividad("Manual");
        datos.setLaneId(1L);
        datos.setPosicionX(0);
        datos.setPosicionY(0);

        mockMvc.perform(post("/api/v1/procesos/1/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isBadRequest());

        verify(actividadService, never()).crearActividad(any(), anyLong(), anyLong());
    }

    @Test
    void mover_conPosicionesValidas_devuelveOk() throws Exception {
        ActividadResponseDTO movida = new ActividadResponseDTO(1L, "Tarea 1", "Manual", 50, 60, 1L);
        when(actividadService.moverActividad(eq(1L), eq(50), eq(60), anyLong())).thenReturn(movida);

        mockMvc.perform(post("/api/v1/procesos/1/actividades/1/mover")
                        .param("posicionX", "50")
                        .param("posicionY", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posicionX").value(50));
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/v1/procesos/1/actividades/1"))
                .andExpect(status().isNoContent());

        verify(actividadService).eliminarActividad(eq(1L), anyLong());
    }
}