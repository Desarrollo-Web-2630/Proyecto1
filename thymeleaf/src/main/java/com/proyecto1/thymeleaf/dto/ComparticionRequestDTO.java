package com.proyecto1.thymeleaf.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComparticionRequestDTO {

    @NotNull(message = "La lista de empresas invitadas es obligatoria")
    private List<Long> empresasInvitadasIds;
}
