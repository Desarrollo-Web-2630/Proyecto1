package com.proyecto1.thymeleaf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos del formulario de actividad (HU-08, HU-09).
 *
 * La lane es obligatoria porque es la que define el rol responsable: las
 * actividades se asignan a funciones y no a personas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActividadDTO {

    private Long id;

    @NotBlank(message = "El nombre de la actividad es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String nombre;

    @NotBlank(message = "El tipo de actividad es obligatorio")
    @Size(max = 255, message = "El tipo no puede superar los 255 caracteres")
    private String tipoActividad;

    @NotNull(message = "La actividad debe estar asociada a una lane")
    private Long laneId;

    @NotNull(message = "La posición X es obligatoria")
    @PositiveOrZero(message = "La posición X no puede ser negativa")
    private Integer posicionX;

    @NotNull(message = "La posición Y es obligatoria")
    @PositiveOrZero(message = "La posición Y no puede ser negativa")
    private Integer posicionY;
}
