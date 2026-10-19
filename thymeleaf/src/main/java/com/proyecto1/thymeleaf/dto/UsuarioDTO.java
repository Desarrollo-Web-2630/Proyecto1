package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class UsuarioDTO {

        private Long id;

        @NotBlank(message = "El nombre del usuario es obligatorio")
        @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚáéíóú])[A-Za-zÁÉÍÓÚáéíóú .'-]+$",
                message = "El nombre solo puede contener letras y espacios básicos")
        private String nombre;

        @NotBlank(message = "El correo del usuario es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 255, message = "El correo no puede superar los 255 caracteres")
        private String correo;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "La contraseña debe incluir mayúscula, minúscula y al menos un número")
        private String password;

        @NotNull(message = "El rol de acceso es obligatorio")
        private Usuario.RolAcceso rolAcceso;
}
