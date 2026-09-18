package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuario tal como se muestra en pantalla.
 *
 * Existe para no llevar la entidad Usuario a la vista: la entidad carga la
 * contrasena y la empresa completa, y ninguna de las dos tiene por que salir
 * del servidor.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioVistaDTO {

    private Long id;
    private String nombre;
    private String correo;
    private Usuario.RolAcceso rolAcceso;
    private Boolean activo;
}
