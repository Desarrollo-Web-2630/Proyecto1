package com.proyecto1.thymeleaf.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos del formulario de empresa (HU-01).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaDTO {

    private Long id;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚáéíóú])[A-Za-zÁÉÍÓÚáéíóú0-9 .,&'-]+$",
            message = "El nombre solo puede contener letras, números y caracteres básicos de identificación")
    private String nombre;

    @NotBlank(message = "El NIT de la empresa es obligatorio")
    @Pattern(regexp = "^\\d{9,12}(?:-\\d)?$",
            message = "El NIT debe tener formato numérico válido, por ejemplo 900123456-7")
    private String nit;

    @NotBlank(message = "El correo de la empresa es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 255, message = "El correo no puede superar los 255 caracteres")
    private String correo;
}
