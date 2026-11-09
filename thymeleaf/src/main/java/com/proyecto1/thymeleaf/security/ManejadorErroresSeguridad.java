package com.proyecto1.thymeleaf.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

// Respuestas 401 y 403 con el mismo formato JSON que GlobalExceptionHandler
// ({timestamp, status, mensaje}), para que el front las muestre igual que cualquier error.
public class ManejadorErroresSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    static final String SIN_TOKEN = "Debe iniciar sesion para acceder a este recurso";
    static final String TOKEN_INVALIDO = "El token es invalido o expiro. Inicie sesion de nuevo";
    static final String SIN_PERMISO = "Su rol no tiene permiso para realizar esta accion";

    private final ObjectMapper objectMapper;

    public ManejadorErroresSeguridad(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 401: no hay token, o el token no paso la validacion (firma, expiracion, formato)
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        boolean trajoToken = request.getHeader(HttpHeaders.AUTHORIZATION) != null;
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        escribir(response, HttpStatus.UNAUTHORIZED, trajoToken ? TOKEN_INVALIDO : SIN_TOKEN);
    }

    // 403: el token es valido pero el rol no alcanza para la operacion
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escribir(response, HttpStatus.FORBIDDEN, SIN_PERMISO);
    }

    private void escribir(HttpServletResponse response, HttpStatus status, String mensaje) throws IOException {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", Instant.now().toString());
        cuerpo.put("status", status.value());
        cuerpo.put("mensaje", mensaje);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), cuerpo);
    }
}
