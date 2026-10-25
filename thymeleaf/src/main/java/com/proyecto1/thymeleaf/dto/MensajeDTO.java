package com.proyecto1.thymeleaf.dto;

import jakarta.validation.constraints.NotBlank;
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
public class MensajeDTO {

    private Long id;

    @NotBlank(message = "El nombre del mensaje es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String nombre;

    @NotBlank(message = "La clave de correlacion es obligatoria")
    @Size(max = 255, message = "La clave de correlacion no puede superar los 255 caracteres")
    private String claveCorrelacion;

    @NotNull(message = "El pool de origen es obligatorio")
    private Long poolOrigenId;

    @NotNull(message = "El pool de destino es obligatorio")
    private Long poolDestinoId;

    private Long elementoOrigenId;
}
