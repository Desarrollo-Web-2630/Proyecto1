package com.proyecto1.thymeleaf.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta de un login exitoso: el JWT que hay que mandar en
 * "Authorization: Bearer <token>" en el resto de peticiones, mas los datos
 * basicos del usuario para que el cliente no tenga que decodificar el token
 * solo para mostrar el nombre.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private UsuarioVistaDTO usuario;
}
