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
import com.proyecto1.thymeleaf.dto.MensajeDTO;
import com.proyecto1.thymeleaf.model.Mensaje;
import com.proyecto1.thymeleaf.model.Pool;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.services.MensajeService;

@WebMvcTest(MensajeController.class)
class MensajeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private MensajeService mensajeService;

    private Mensaje construirMensaje(Long id) {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        Pool origen = new Pool();
        origen.setId(1L);
        Pool destino = new Pool();
        destino.setId(2L);
        Mensaje mensaje = new Mensaje();
        mensaje.setId(id);
        mensaje.setNombre("Notificar");
        mensaje.setClaveCorrelacion("numero_guia");
        mensaje.setProceso(proceso);
        mensaje.setPoolOrigen(origen);
        mensaje.setPoolDestino(destino);
        return mensaje;
    }

    private MensajeDTO datosValidos() {
        MensajeDTO datos = new MensajeDTO();
        datos.setNombre("Notificar");
        datos.setClaveCorrelacion("numero_guia");
        datos.setPoolOrigenId(1L);
        datos.setPoolDestinoId(2L);
        return datos;
    }

    @Test
    void listar_devuelveOkConLosMensajes() throws Exception {
        when(mensajeService.listarPorProcesoYEmpresa(eq(1L), anyLong())).thenReturn(List.of(construirMensaje(1L)));

        mockMvc.perform(get("/api/v1/procesos/1/mensajes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].claveCorrelacion").value("numero_guia"))
                .andExpect(jsonPath("$[0].poolDestinoId").value(2));
    }

    @Test
    void obtener_existente_devuelveOk() throws Exception {
        when(mensajeService.obtenerPorIdYEmpresa(eq(1L), eq(1L), anyLong())).thenReturn(construirMensaje(1L));

        mockMvc.perform(get("/api/v1/procesos/1/mensajes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Notificar"));
    }

    @Test
    void obtener_inexistente_devuelve400() throws Exception {
        when(mensajeService.obtenerPorIdYEmpresa(eq(9L), eq(1L), anyLong()))
                .thenThrow(new IllegalArgumentException("El mensaje no existe o no pertenece a su empresa"));

        mockMvc.perform(get("/api/v1/procesos/1/mensajes/9"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_conDatosValidos_devuelve201() throws Exception {
        when(mensajeService.crearMensaje(any(MensajeDTO.class), eq(1L), anyLong())).thenReturn(construirMensaje(5L));

        mockMvc.perform(post("/api/v1/procesos/1/mensajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosValidos())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void crear_sinClaveDeCorrelacion_devuelve400() throws Exception {
        MensajeDTO datos = datosValidos();
        datos.setClaveCorrelacion(" ");

        mockMvc.perform(post("/api/v1/procesos/1/mensajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_conDatosValidos_devuelveOk() throws Exception {
        when(mensajeService.actualizarMensaje(eq(1L), any(MensajeDTO.class), eq(1L), anyLong()))
                .thenReturn(construirMensaje(1L));

        mockMvc.perform(put("/api/v1/procesos/1/mensajes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosValidos())))
                .andExpect(status().isOk());
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/v1/procesos/1/mensajes/1"))
                .andExpect(status().isNoContent());

        verify(mensajeService).eliminarMensaje(eq(1L), eq(1L), anyLong());
    }
}
