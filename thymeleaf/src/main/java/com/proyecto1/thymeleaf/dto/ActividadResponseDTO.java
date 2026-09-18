package com.proyecto1.thymeleaf.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActividadResponseDTO {

    private Long id;
    private String nombre;
    private String tipoActividad;
    private Integer posicionX;
    private Integer posicionY;
    private Long laneId;
}
