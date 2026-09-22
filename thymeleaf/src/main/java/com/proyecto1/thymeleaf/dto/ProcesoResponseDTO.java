package com.proyecto1.thymeleaf.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcesoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String estado;
}
