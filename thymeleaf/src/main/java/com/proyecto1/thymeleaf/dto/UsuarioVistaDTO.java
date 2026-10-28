package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Long empresaId;
}
