package com.proyecto1.thymeleaf.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.dto.GatewayDTO;
import com.proyecto1.thymeleaf.model.Gateway;
import com.proyecto1.thymeleaf.services.GatewayService;
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

@WebMvcTest(GatewayController.class)
class GatewayControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private GatewayService gatewayService;

    private Gateway construirGateway(Long id) {
        Gateway gateway = new Gateway();
        gateway.setId(id);
        gateway.setNombre("Decision 1");
        gateway.setTipo(Gateway.TipoGateway.EXCLUSIVO);
        return gateway;
    }

    @Test
    void listar_devuelveOkConLaListaMapeada() throws Exception {
        when(gatewayService.listarPorProcesoYEmpresa(eq(1L), anyLong())).thenReturn(List.of(construirGateway(1L)));

        mockMvc.perform(get("/api/v1/procesos/1/gateways"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Decision 1"));
    }

    @Test
    void obtener_inexistente_devuelve400() throws Exception {
        when(gatewayService.obtenerPorIdYEmpresa(eq(99L), anyLong()))
                .thenThrow(new IllegalArgumentException("El gateway no existe o no pertenece a su empresa"));

        mockMvc.perform(get("/api/v1/procesos/1/gateways/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_conDatosValidos_devuelve201() throws Exception {
        GatewayDTO datos = new GatewayDTO();
        datos.setNombre("Decision 1");
        datos.setTipo(Gateway.TipoGateway.EXCLUSIVO);

        when(gatewayService.crearGateway(any(GatewayDTO.class), eq(1L), anyLong())).thenReturn(construirGateway(5L));

        mockMvc.perform(post("/api/v1/procesos/1/gateways")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void crear_sinTipo_devuelve400PorValidacionDeBean() throws Exception {
        GatewayDTO datos = new GatewayDTO();
        datos.setNombre("Decision 1");

        mockMvc.perform(post("/api/v1/procesos/1/gateways")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_conDatosValidos_devuelveOk() throws Exception {
        GatewayDTO datos = new GatewayDTO();
        datos.setNombre("Decision actualizada");
        datos.setTipo(Gateway.TipoGateway.PARALELO);

        Gateway actualizado = construirGateway(1L);
        actualizado.setNombre("Decision actualizada");
        actualizado.setTipo(Gateway.TipoGateway.PARALELO);
        when(gatewayService.actualizarGateway(eq(1L), any(GatewayDTO.class), anyLong())).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/procesos/1/gateways/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("PARALELO"));
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/v1/procesos/1/gateways/1"))
                .andExpect(status().isNoContent());

        verify(gatewayService).eliminarGateway(eq(1L), anyLong());
    }
}