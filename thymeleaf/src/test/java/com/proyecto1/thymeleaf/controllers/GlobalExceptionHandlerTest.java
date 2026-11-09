package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.exceptions.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void manejarNoEncontrado_devuelve404ConElMensaje() {
        ResponseEntity<Map<String, Object>> respuesta =
                handler.manejarNoEncontrado(new RecursoNoEncontradoException("El proceso no existe o no pertenece a su empresa"));

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("El proceso no existe o no pertenece a su empresa", respuesta.getBody().get("mensaje"));
    }

    @Test
    void manejarAutenticacion_devuelve401() {
        ResponseEntity<Map<String, Object>> respuesta =
                handler.manejarAutenticacion(new BadCredentialsException("Correo o contrasena incorrectos"));

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertEquals("Correo o contrasena incorrectos", respuesta.getBody().get("mensaje"));
    }

    @Test
    void manejarAccesoDenegado_devuelve403SinDetallesInternos() {
        ResponseEntity<Map<String, Object>> respuesta =
                handler.manejarAccesoDenegado(new AccessDeniedException("detalle interno"));

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
        assertFalse(respuesta.getBody().get("mensaje").toString().contains("detalle interno"));
    }

    @Test
    void manejarIllegalArgument_devuelve400ConElMensaje() {
        ResponseEntity<Map<String, Object>> respuesta =
                handler.manejarIllegalArgument(new IllegalArgumentException("dato invalido"));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("dato invalido", respuesta.getBody().get("mensaje"));
    }

    @Test
    void manejarValidacion_juntaLosMensajesDeCadaCampo() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("obj", "nombre", "es obligatorio");
        FieldError error2 = new FieldError("obj", "correo", "formato invalido");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> respuesta = handler.manejarValidacion(ex);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        String mensaje = (String) respuesta.getBody().get("mensaje");
        assertTrue(mensaje.contains("nombre"));
        assertTrue(mensaje.contains("correo"));
    }

    @Test
    void manejarValidacion_sinErroresDeCampo_usaMensajePorDefecto() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> respuesta = handler.manejarValidacion(ex);

        assertEquals("Datos invalidos", respuesta.getBody().get("mensaje"));
    }

    @Test
    void manejarConstraint_devuelve400() {
        ConstraintViolationException ex = new ConstraintViolationException("violacion", null);
        ResponseEntity<Map<String, Object>> respuesta = handler.manejarConstraint(ex);
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void manejarCuerpoIlegible_devuelve400ConMensajeGenerico() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        ResponseEntity<Map<String, Object>> respuesta = handler.manejarCuerpoIlegible(ex);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertTrue(((String) respuesta.getBody().get("mensaje")).contains("JSON"));
    }

    @Test
    void manejarTipoInvalido_incluyeElNombreDelParametro() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("posicionX");

        ResponseEntity<Map<String, Object>> respuesta = handler.manejarTipoInvalido(ex);

        assertTrue(((String) respuesta.getBody().get("mensaje")).contains("posicionX"));
    }

    @Test
    void manejarParametroFaltante_incluyeElNombreDelParametro() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("x", "Integer");

        ResponseEntity<Map<String, Object>> respuesta = handler.manejarParametroFaltante(ex);

        assertTrue(((String) respuesta.getBody().get("mensaje")).contains("x"));
    }

    @Test
    void manejarMetodoNoSoportado_devuelve405() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<Map<String, Object>> respuesta = handler.manejarMetodoNoSoportado(ex);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, respuesta.getStatusCode());
    }

    @Test
    void manejarTipoDeContenido_devuelve415() {
        HttpMediaTypeNotSupportedException ex = mock(HttpMediaTypeNotSupportedException.class);

        ResponseEntity<Map<String, Object>> respuesta = handler.manejarTipoDeContenido(ex);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, respuesta.getStatusCode());
    }

    @Test
    void manejarRutaInexistente_devuelve404() {
        NoResourceFoundException ex = mock(NoResourceFoundException.class);

        ResponseEntity<Map<String, Object>> respuesta = handler.manejarRutaInexistente(ex);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void manejarError_devuelve500ConMensajeGenericoSinFiltrarElReal() {
        ResponseEntity<Map<String, Object>> respuesta =
                handler.manejarError(new RuntimeException("detalle interno sensible"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        String mensaje = (String) respuesta.getBody().get("mensaje");
        assertFalse(mensaje.contains("detalle interno sensible"));
    }
}