package com.proyecto1.thymeleaf.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProcesoRequestDTO {

    @NotBlank(message = "El nombre del proceso es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripcion del proceso es obligatoria")
    private String descripcion;

    @NotBlank(message = "La categoria del proceso es obligatoria")
    private String categoria;
}
