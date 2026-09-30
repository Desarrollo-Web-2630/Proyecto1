package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Empresa;

public record EmpresaRespuestaDTO(Long id, String nombre, String nit, String correo) {

    public static EmpresaRespuestaDTO desde(Empresa e) {
        return new EmpresaRespuestaDTO(e.getId(), e.getNombre(), e.getNit(), e.getCorreo());
    }
}
