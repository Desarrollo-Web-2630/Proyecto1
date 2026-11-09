package com.proyecto1.thymeleaf.exceptions;

// El recurso no existe o pertenece a otra empresa. Se responde 404 en ambos casos para no
// confirmar a un usuario que existe un recurso de otra organizacion.
// Extiende IllegalArgumentException para no romper el codigo que ya la capturaba.
public class RecursoNoEncontradoException extends IllegalArgumentException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
