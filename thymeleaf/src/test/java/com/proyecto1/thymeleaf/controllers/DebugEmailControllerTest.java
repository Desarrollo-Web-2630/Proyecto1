package com.proyecto1.thymeleaf.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.services.CorreoService;
import com.proyecto1.thymeleaf.services.VerificacionCorreoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @Profile({"h2","local","postman"}) en el controller: sin activar uno de
// estos perfiles en el test, Spring no registra el bean y todo da 404.
@WebMvcTest(DebugEmailController.class)
@ActiveProfiles("h2")
class DebugEmailControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CorreoService correoService;
    @MockBean private VerificacionCorreoService verificacionCorreoService;

    @Test
    void enviarPrueba_sinTo_devuelve400() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("name", "Ana");

        mockMvc.perform(post("/api/v1/debug/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void enviarPrueba_conLinkIgnoreYUsuarioExistente_reenviaYDevuelve202() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("to", "ana@demo.com");
        body.put("link", "ignore");

        doReturn(null).when(verificacionCorreoService).reenviarVerificacion("ana@demo.com");

        mockMvc.perform(post("/api/v1/debug/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    void enviarPrueba_conUsuarioNoEncontrado_generaTokenDebugYDevuelve201() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("to", "noexiste@demo.com");

        doThrow(new IllegalArgumentException("no existe"))
                .when(verificacionCorreoService).reenviarVerificacion("noexiste@demo.com");
        when(verificacionCorreoService.generarTokenDebug("noexiste@demo.com")).thenReturn("token-debug-123");
        when(verificacionCorreoService.getBackendBaseUrl()).thenReturn("http://localhost:8080");

        mockMvc.perform(post("/api/v1/debug/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.debugToken").value("token-debug-123"));

        verify(correoService).enviarCorreoVerificacion(eq("noexiste@demo.com"), anyString(), contains("token-debug-123"));
    }

    @Test
    void enviarPrueba_conLinkExplicito_enviaDirectoYDevuelve202() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("to", "ana@demo.com");
        body.put("link", "http://localhost:8080/verificar?token=xyz");

        mockMvc.perform(post("/api/v1/debug/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(correoService).enviarCorreoVerificacion(eq("ana@demo.com"), anyString(), eq("http://localhost:8080/verificar?token=xyz"));
    }

    @Test
    void enviarPrueba_conErrorInesperado_devuelve500() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("to", "ana@demo.com");
        body.put("link", "http://localhost:8080/verificar?token=xyz");

        doThrow(new RuntimeException("fallo inesperado"))
                .when(correoService).enviarCorreoVerificacion(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/v1/debug/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isInternalServerError());
    }
}