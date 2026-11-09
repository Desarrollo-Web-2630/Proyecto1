package com.proyecto1.thymeleaf.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ManejadorErroresSeguridadTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ManejadorErroresSeguridad manejador = new ManejadorErroresSeguridad(objectMapper);

    @Test
    void commence_sinToken_responde401PidiendoIniciarSesion() throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        manejador.commence(new MockHttpServletRequest(), respuesta, new InsufficientAuthenticationException("x"));

        assertEquals(401, respuesta.getStatus());
        assertEquals("Bearer", respuesta.getHeader("WWW-Authenticate"));
        assertEquals(ManejadorErroresSeguridad.SIN_TOKEN, cuerpo(respuesta).get("mensaje"));
    }

    @Test
    void commence_conTokenInvalido_responde401ConMensajeDeToken() throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest();
        peticion.addHeader("Authorization", "Bearer basura");
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        manejador.commence(peticion, respuesta, new InsufficientAuthenticationException("x"));

        assertEquals(401, respuesta.getStatus());
        assertEquals(ManejadorErroresSeguridad.TOKEN_INVALIDO, cuerpo(respuesta).get("mensaje"));
    }

    @Test
    void handle_responde403EnFormatoJson() throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        manejador.handle(new MockHttpServletRequest(), respuesta, new AccessDeniedException("x"));

        assertEquals(403, respuesta.getStatus());
        assertTrue(respuesta.getContentType().startsWith("application/json"));
        Map<String, Object> cuerpo = cuerpo(respuesta);
        assertEquals(403, cuerpo.get("status"));
        assertEquals(ManejadorErroresSeguridad.SIN_PERMISO, cuerpo.get("mensaje"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> cuerpo(MockHttpServletResponse respuesta) throws Exception {
        return objectMapper.readValue(respuesta.getContentAsByteArray(), Map.class);
    }
}
