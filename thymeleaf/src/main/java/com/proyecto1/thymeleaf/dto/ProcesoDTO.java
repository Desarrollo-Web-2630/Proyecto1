package com.proyecto1.thymeleaf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos del formulario de proceso (HU-04, HU-05).
 *
 * No lleva estado ni empresa: el estado lo fija el servicio (siempre nace en
 * BORRADOR) y la empresa sale del usuario autenticado, no del formulario.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcesoDTO {

    private Long id;

    @NotBlank(message = "El nombre del proceso es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción del proceso es obligatoria")
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String descripcion;

    @NotBlank(message = "La categoría del proceso es obligatoria")
    @Size(max = 255, message = "La categoría no puede superar los 255 caracteres")
    private String categoria;
}
