package com.proyecto1.thymeleaf.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos del formulario de arco (HU-11, HU-12).
 *
 * Origen y destino viajan como id porque el formulario los elige de una lista
 * desplegable. La HU-12 exige poder cambiarlos al editar, no solo al crear.
 */
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
