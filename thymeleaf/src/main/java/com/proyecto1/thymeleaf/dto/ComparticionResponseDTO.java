package com.proyecto1.thymeleaf.dto;

import java.util.List;

public record ComparticionResponseDTO(Long procesoId, boolean compartido,
                                      List<EmpresaResumenDTO> empresasInvitadas) {
}
