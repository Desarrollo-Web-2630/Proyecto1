package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.Mensaje;
import jakarta.validation.constraints.NotBlank;
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
public class MensajeDTO {

    private Long id;

    @NotBlank(message = "El nombre del mensaje es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String nombre;

    @NotBlank(message = "La clave de correlacion es obligatoria")
    @Size(max = 255, message = "La clave de correlacion no puede superar los 255 caracteres")
    private String claveCorrelacion;

    @NotNull(message = "El pool de origen es obligatorio")
    private Long poolOrigenId;

    @NotNull(message = "El pool de destino es obligatorio")
    private Long poolDestinoId;

    private Long elementoOrigenId;

    private Mensaje.TipoMensaje tipoMensaje = Mensaje.TipoMensaje.THROW;

    private Mensaje.VarianteMensaje variante = Mensaje.VarianteMensaje.INICIO;

    @Size(max = 255, message = "El destino externo no puede superar los 255 caracteres")
    private String destinoExterno;

    @Size(max = 4000, message = "El payload no puede superar los 4000 caracteres")
    private String payload;

    private Mensaje.PoliticaFallo politicaFallo;
}
