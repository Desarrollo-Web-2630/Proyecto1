package com.proyecto1.thymeleaf.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// HU-22: nuevo orden de las lanes de un pool, de arriba hacia abajo
public record OrdenLanesDTO(@NotEmpty(message = "La lista de lanes es obligatoria") List<Long> lanesIds) {
}
