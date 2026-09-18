package com.proyecto1.thymeleaf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Activa una cuenta pendiente de verificacion y fija su contrasena real en un
 * solo paso. Es el unico camino para activar al administrador creado por
 * EmpresaService.registrarEmpresa, ya que a ese usuario no se le asigna una
 * contrasena utilizable al crearlo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivarCuentaRequestDTO {

    @NotBlank(message = "El token de verificacion es obligatorio")
    private String token;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, max = 128, message = "La contrasena debe tener entre 8 y 128 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "La contrasena debe incluir mayuscula, minuscula y al menos un numero")
    private String nuevaPassword;
}
