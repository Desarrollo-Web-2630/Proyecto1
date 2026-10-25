package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Pool;
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
public class PoolDTO {

    private Long id;

    @NotBlank(message = "El nombre del pool es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String nombre;

    @NotNull(message = "El tipo de pool es obligatorio")
    private Pool.TipoPool tipo;
}
