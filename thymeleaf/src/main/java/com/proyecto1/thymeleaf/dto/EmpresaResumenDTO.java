package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Empresa;

public record EmpresaResumenDTO(Long id, String nombre) {

    public static EmpresaResumenDTO desde(Empresa empresa) {
        return new EmpresaResumenDTO(empresa.getId(), empresa.getNombre());
    }
}
