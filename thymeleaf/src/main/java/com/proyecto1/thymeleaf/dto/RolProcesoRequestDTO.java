package com.proyecto1.thymeleaf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RolProcesoRequestDTO {

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 100, message = "El nombre del rol no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "La descripcion del rol es obligatoria")
    @Size(max = 500, message = "La descripcion del rol no puede superar 500 caracteres")
    private String descripcion;
}
