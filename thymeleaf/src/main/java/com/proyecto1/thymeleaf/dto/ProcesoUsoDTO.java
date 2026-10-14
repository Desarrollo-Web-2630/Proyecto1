package com.proyecto1.thymeleaf.dto;

/**
 * Un proceso en el que se usa un rol de proceso (HU-19 y HU-20): el sistema
 * indica en que procesos esta usado un rol cuando no se puede eliminar.
 */
public record ProcesoUsoDTO(Long id, String nombre) {
}
