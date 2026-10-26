package com.proyecto1.thymeleaf.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.dto.LaneDTO;
import com.proyecto1.thymeleaf.dto.OrdenLanesDTO;
import com.proyecto1.thymeleaf.model.Lane;
import com.proyecto1.thymeleaf.model.Pool;
import com.proyecto1.thymeleaf.model.RolProceso;
import com.proyecto1.thymeleaf.services.LaneService;

@WebMvcTest(LaneController.class)
class LaneControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private LaneService laneService;

    private Lane construirLane(Long id, int orden) {
        Pool pool = new Pool();
        pool.setId(1L);
        RolProceso rol = new RolProceso();
        rol.setId(5L);
        rol.setNombre("Analista");
        Lane lane = new Lane();
        lane.setId(id);
        lane.setNombre("Analisis");
        lane.setOrden(orden);
        lane.setPool(pool);
        lane.setRolProceso(rol);
        return lane;
    }

    private LaneDTO datosValidos() {
        LaneDTO datos = new LaneDTO();
        datos.setNombre("Analisis");
        datos.setRolProcesoId(5L);
        return datos;
    }

    @Test
    void listar_devuelveOkConLasLanes() throws Exception {
        when(laneService.listarPorPoolYEmpresa(eq(1L), eq(1L), anyLong())).thenReturn(List.of(construirLane(1L, 0)));

        mockMvc.perform(get("/api/v1/procesos/1/pools/1/lanes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rolProcesoNombre").value("Analista"));
    }

    @Test
    void obtener_existente_devuelveOk() throws Exception {
        when(laneService.obtenerPorIdYEmpresa(eq(1L), eq(1L), anyLong())).thenReturn(construirLane(1L, 0));

        mockMvc.perform(get("/api/v1/procesos/1/pools/1/lanes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.poolId").value(1));
    }

    @Test
    void obtener_inexistente_devuelve400() throws Exception {
        when(laneService.obtenerPorIdYEmpresa(eq(9L), eq(1L), anyLong()))
                .thenThrow(new IllegalArgumentException("La lane no existe o no pertenece a su empresa"));

        mockMvc.perform(get("/api/v1/procesos/1/pools/1/lanes/9"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_conDatosValidos_devuelve201() throws Exception {
        when(laneService.crearLane(any(LaneDTO.class), eq(1L), eq(1L), anyLong())).thenReturn(construirLane(5L, 0));

        mockMvc.perform(post("/api/v1/procesos/1/pools/1/lanes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosValidos())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void crear_sinRol_devuelve400() throws Exception {
        LaneDTO datos = new LaneDTO();
        datos.setNombre("Analisis");

        mockMvc.perform(post("/api/v1/procesos/1/pools/1/lanes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_conDatosValidos_devuelveOk() throws Exception {
        when(laneService.actualizarLane(eq(1L), any(LaneDTO.class), eq(1L), anyLong())).thenReturn(construirLane(1L, 0));

        mockMvc.perform(put("/api/v1/procesos/1/pools/1/lanes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosValidos())))
                .andExpect(status().isOk());
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/v1/procesos/1/pools/1/lanes/1"))
                .andExpect(status().isNoContent());

        verify(laneService).eliminarLane(eq(1L), eq(1L), anyLong());
    }

    @Test
    void reordenar_conListaValida_devuelveLasLanesEnElNuevoOrden() throws Exception {
        when(laneService.reordenarLanes(eq(1L), eq(List.of(2L, 1L)), eq(1L), anyLong()))
                .thenReturn(List.of(construirLane(2L, 0), construirLane(1L, 1)));

        mockMvc.perform(put("/api/v1/procesos/1/pools/1/lanes/orden")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrdenLanesDTO(List.of(2L, 1L)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].orden").value(1));
    }

    @Test
    void reordenar_conListaVacia_devuelve400() throws Exception {
        mockMvc.perform(put("/api/v1/procesos/1/pools/1/lanes/orden")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrdenLanesDTO(List.of()))))
                .andExpect(status().isBadRequest());
    }
}
