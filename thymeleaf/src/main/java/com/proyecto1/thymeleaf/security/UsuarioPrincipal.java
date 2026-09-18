package com.proyecto1.thymeleaf.security;

import com.proyecto1.thymeleaf.model.Usuario;

/**
 * Lo que queda en el SecurityContext despues de validar un JWT.
 *
 * No es un UserDetails de Spring Security a proposito: la autenticacion no
 * pasa por un AuthenticationManager con UserDetailsService (no hay login por
 * formulario ni sesion), el JWT ya trae todo lo necesario y JwtAuthenticationFilter
 * lo arma directamente. Guardar solo estos cuatro campos (y no la entidad
 * Usuario completa) evita otra vuelta a la base de datos en cada peticion.
 */
public record UsuarioPrincipal(Long usuarioId, Long empresaId, Usuario.RolAcceso rolAcceso, String correo) {
}
