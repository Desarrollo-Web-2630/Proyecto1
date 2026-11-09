package com.proyecto1.thymeleaf.dto;

import java.time.Instant;

// Respuesta del login: el token que el front envia como "Authorization: Bearer <token>"
// y los datos del usuario autenticado (sin contrasena).
public record LoginResponseDTO(String token, String tipo, Instant expiraEn, UsuarioVistaDTO usuario) {

    public static final String TIPO_BEARER = "Bearer";
}
