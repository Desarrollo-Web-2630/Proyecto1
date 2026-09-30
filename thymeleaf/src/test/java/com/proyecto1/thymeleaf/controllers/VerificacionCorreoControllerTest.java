package com.proyecto1.thymeleaf.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.dto.ActivarCuentaRequestDTO;
import com.proyecto1.thymeleaf.dto.VerificacionCorreoRequestDTO;
import com.proyecto1.thymeleaf.dto.VerificacionCorreoResponseDTO;
import com.proyecto1.thymeleaf.services.VerificacionCorreoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VerificacionCorreoController.class)
class VerificacionCorreoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private VerificacionCorreoService verificacionCorreoService;

    private VerificacionCorreoResponseDTO respuestaOk() {
        return VerificacionCorreoResponseDTO.builder()
                .verificado(true)
                .mensaje("Correo verificado correctamente")
                .correo("ana@demo.com")
                .build();
    }

    @Test
    void verificarCorreo_porQueryParam_devuelveOk() throws Exception {
        when(verificacionCorreoService.verificarCorreo("abc")).thenReturn(respuestaOk());

        mockMvc.perform(get("/api/auth/verificar-correo").param("token", "abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificado").value(true));
    }

    @Test
    void verificarCorreoPorPath_devuelveOk() throws Exception {
        when(verificacionCorreoService.verificarCorreo("abc")).thenReturn(respuestaOk());

        mockMvc.perform(get("/api/auth/verificar-correo/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("ana@demo.com"));
    }

    @Test
    void verificarCorreoPorBody_conTokenEnBody_devuelveOk() throws Exception {
        VerificacionCorreoRequestDTO body = new VerificacionCorreoRequestDTO();
        body.setCorreo("ana@demo.com");
        body.setToken("abc");

        when(verificacionCorreoService.verificarCorreo("abc")).thenReturn(respuestaOk());

        mockMvc.perform(post("/api/auth/verificar-correo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void verificarCorreoPorBody_conTokenPorQueryParam_priorizaQueryParam() throws Exception {
        when(verificacionCorreoService.verificarCorreo("desdeQuery")).thenReturn(respuestaOk());

        mockMvc.perform(post("/api/auth/verificar-correo").param("token", "desdeQuery"))
                .andExpect(status().isOk());

        verify(verificacionCorreoService).verificarCorreo("desdeQuery");
    }

    @Test
    void activarCuenta_conDatosValidos_devuelveOk() throws Exception {
        ActivarCuentaRequestDTO request = new ActivarCuentaRequestDTO();
        request.setToken("abc");
        request.setNuevaPassword("Clave1234");

        when(verificacionCorreoService.activarCuenta("abc", "Clave1234")).thenReturn(respuestaOk());

        mockMvc.perform(post("/api/auth/activar-cuenta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void activarCuenta_conPasswordDebil_devuelve400PorValidacionDeBean() throws Exception {
        ActivarCuentaRequestDTO request = new ActivarCuentaRequestDTO();
        request.setToken("abc");
        request.setNuevaPassword("debil");

        mockMvc.perform(post("/api/auth/activar-cuenta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(verificacionCorreoService, never()).activarCuenta(any(), any());
    }

    @Test
    void reenviarVerificacion_conCorreoPorBody_devuelveOk() throws Exception {
        VerificacionCorreoRequestDTO body = new VerificacionCorreoRequestDTO();
        body.setCorreo("ana@demo.com");
        body.setToken("no-usado-aqui");

        VerificacionCorreoResponseDTO respuestaGenerica = VerificacionCorreoResponseDTO.builder()
                .verificado(false)
                .mensaje("Si el correo esta registrado, recibira un enlace de verificacion")
                .correo("")
                .build();
        when(verificacionCorreoService.reenviarVerificacion("ana@demo.com")).thenReturn(respuestaGenerica);

        mockMvc.perform(post("/api/auth/reenviar-verificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void reenviarVerificacion_sinCorreo_devuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/reenviar-verificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}