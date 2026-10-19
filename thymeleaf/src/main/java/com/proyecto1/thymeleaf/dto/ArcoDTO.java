package com.proyecto1.thymeleaf.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArcoDTO {

    private Long id;

    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String nombre;

    @Size(max = 255, message = "La condición no puede superar los 255 caracteres")
    private String condicion;

    @NotNull(message = "El elemento de origen es obligatorio")
    private Long origenId;

    @NotNull(message = "El elemento de destino es obligatorio")
    private Long destinoId;
}
