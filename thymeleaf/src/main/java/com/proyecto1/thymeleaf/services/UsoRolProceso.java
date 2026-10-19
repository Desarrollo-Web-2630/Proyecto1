package com.proyecto1.thymeleaf.services;

import java.util.List;

import com.proyecto1.thymeleaf.dto.ProcesoUsoDTO;

public interface UsoRolProceso {

    List<ProcesoUsoDTO> procesosDondeSeUsa(Long rolId, Long empresaId);
}
