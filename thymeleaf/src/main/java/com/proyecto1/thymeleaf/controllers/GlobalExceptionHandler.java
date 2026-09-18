package com.proyecto1.thymeleaf.controllers;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo centralizado de errores. Antes de esto, cualquier IllegalArgumentException
 * de un servicio (nombre duplicado, proceso no encontrado, contrasena debil,
 * literalmente el 90% de las validaciones de negocio de este proyecto) caia
 * sin capturar y Spring Boot respondia 500 Internal Server Error con su
 * pagina de error generica, sin importar que la causa fuera un error del
 * cliente (400) y no del servidor.
 *
 * Los mensajes que se devuelven son siempre los que ya escriben los propios
 * servicios (pensados para mostrarse: "Ya existe un proceso llamado...", "La
 * contrasena debe tener..."). Nunca se devuelve el mensaje crudo de una
 * excepcion inesperada ni su stacktrace: eso solo se registra en el log del
 * servidor.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Reglas de negocio: nombre duplicado, entidad no encontrada, datos invalidos, etc.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarIllegalArgument(IllegalArgumentException e) {
        return construir(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // @Valid en un @RequestBody: junta los mensajes de cada campo invalido
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Datos invalidos");
        return construir(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarConstraint(ConstraintViolationException e) {
        return construir(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // Un rol sin permiso para la operacion (ContextoSeguridad.exigirAdmin/exigirEscritura/exigirPropiaEmpresa)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> manejarAccesoDenegado(AccessDeniedException e) {
        return construir(HttpStatus.FORBIDDEN, e.getMessage());
    }

    // ContextoSeguridad.usuarioActual() cuando no hay ningun JWT valido en la peticion
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> manejarSinAutenticar(IllegalStateException e) {
        return construir(HttpStatus.UNAUTHORIZED, "Debe iniciar sesion para realizar esta operacion");
    }

    // Cualquier otra cosa: no se filtra el mensaje real, solo se registra en el log
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarError(Exception e) {
        log.error("Error no controlado", e);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado. Intente nuevamente.");
    }

    private ResponseEntity<Map<String, Object>> construir(HttpStatus status, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", Instant.now().toString());
        cuerpo.put("status", status.value());
        cuerpo.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(cuerpo);
    }
}
