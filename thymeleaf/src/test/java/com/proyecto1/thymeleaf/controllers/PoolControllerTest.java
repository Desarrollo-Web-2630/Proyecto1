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
import com.proyecto1.thymeleaf.dto.PoolDTO;
import com.proyecto1.thymeleaf.model.Pool;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.services.PoolService;

@WebMvcTest(PoolController.class)
class PoolControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private PoolService poolService;

    private Pool construirPool(Long id) {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        Pool pool = new Pool();
        pool.setId(id);
        pool.setNombre("Empresa");
        pool.setTipo(Pool.TipoPool.PROPIETARIO);
        pool.setProceso(proceso);
        return pool;
    }

    private PoolDTO datosValidos() {
        PoolDTO datos = new PoolDTO();
        datos.setNombre("Empresa");
        datos.setTipo(Pool.TipoPool.PROPIETARIO);
        return datos;
    }

    @Test
    void listar_devuelveOkConLosPools() throws Exception {
        when(poolService.listarPorProcesoYEmpresa(eq(1L), anyLong())).thenReturn(List.of(construirPool(1L)));

        mockMvc.perform(get("/api/v1/procesos/1/pools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Empresa"))
                .andExpect(jsonPath("$[0].procesoId").value(1));
    }

    @Test
    void obtener_existente_devuelveOk() throws Exception {
        when(poolService.obtenerPorIdYEmpresa(eq(1L), eq(1L), anyLong())).thenReturn(construirPool(1L));

        mockMvc.perform(get("/api/v1/procesos/1/pools/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("PROPIETARIO"));
    }

    @Test
    void obtener_inexistente_devuelve400() throws Exception {
        when(poolService.obtenerPorIdYEmpresa(eq(9L), eq(1L), anyLong()))
                .thenThrow(new IllegalArgumentException("El pool no existe o no pertenece a su empresa"));

        mockMvc.perform(get("/api/v1/procesos/1/pools/9"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_conDatosValidos_devuelve201() throws Exception {
        when(poolService.crearPool(any(PoolDTO.class), eq(1L), anyLong())).thenReturn(construirPool(5L));

        mockMvc.perform(post("/api/v1/procesos/1/pools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosValidos())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void crear_sinTipo_devuelve400() throws Exception {
        PoolDTO datos = new PoolDTO();
        datos.setNombre("Empresa");

        mockMvc.perform(post("/api/v1/procesos/1/pools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_conDatosValidos_devuelveOk() throws Exception {
        when(poolService.actualizarPool(eq(1L), any(PoolDTO.class), eq(1L), anyLong())).thenReturn(construirPool(1L));

        mockMvc.perform(put("/api/v1/procesos/1/pools/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosValidos())))
                .andExpect(status().isOk());
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/v1/procesos/1/pools/1"))
                .andExpect(status().isNoContent());

        verify(poolService).eliminarPool(eq(1L), eq(1L), anyLong());
    }
}
