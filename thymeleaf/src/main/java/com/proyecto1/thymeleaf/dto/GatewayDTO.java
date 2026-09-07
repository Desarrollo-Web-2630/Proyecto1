package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Gateway;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos del formulario de gateway (HU-14, HU-15).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GatewayDTO {

    private Long id;

    @NotBlank(message = "El nombre del gateway es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String nombre;

    @NotNull(message = "El tipo de gateway es obligatorio")
    private Gateway.TipoGateway tipo;
}
