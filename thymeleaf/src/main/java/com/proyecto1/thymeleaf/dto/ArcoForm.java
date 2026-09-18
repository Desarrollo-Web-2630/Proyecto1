package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Arco;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArcoForm {

    private Long id;

    @Size(max = 100, message = "La etiqueta no puede superar los 100 caracteres")
    private String nombre;

    @NotNull(message = "Debe seleccionar un elemento de origen")
    private Long origenId;

    @NotNull(message = "Debe seleccionar un elemento de destino")
    private Long destinoId;

    @Size(max = 255, message = "La condición no puede superar los 255 caracteres")
    private String condicion;

    public static ArcoForm desdeEntidad(Arco arco) {
        ArcoForm form = new ArcoForm();
        form.setId(arco.getId());
        form.setNombre(arco.getNombre());
        form.setOrigenId(arco.getOrigen().getId());
        form.setDestinoId(arco.getDestino().getId());
        form.setCondicion(arco.getCondicion());
        return form;
    }
}
