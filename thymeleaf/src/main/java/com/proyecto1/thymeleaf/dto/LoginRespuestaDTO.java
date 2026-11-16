package com.proyecto1.thymeleaf.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRespuestaDTO {

    private String token;
    private String tipo;
    private long expiraEn;
    private UsuarioVistaDTO usuario;
}
