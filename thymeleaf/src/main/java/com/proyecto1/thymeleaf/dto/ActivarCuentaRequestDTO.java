package com.proyecto1.thymeleaf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
