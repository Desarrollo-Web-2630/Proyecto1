package com.proyecto1.thymeleaf.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActividadRequestDTO {

    @NotBlank(message = "El nombre de la actividad es obligatorio")
    private String nombre;

    @NotBlank(message = "El tipo de actividad es obligatorio")
    private String tipoActividad;

    @NotNull(message = "La posicion X es obligatoria")
    @Min(value = 0, message = "La posicion X no puede ser negativa")
    private Integer posicionX;

    @NotNull(message = "La posicion Y es obligatoria")
    @Min(value = 0, message = "La posicion Y no puede ser negativa")
    private Integer posicionY;

    @NotNull(message = "La lane es obligatoria")
    private Long laneId;
}
