package com.proyecto1.thymeleaf.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto1.thymeleaf.dto.ProcesoUsoDTO;

@ExtendWith(MockitoExtension.class)
class UsoRolProcesoConLanesTest {

    @Mock private LaneService laneService;

    @InjectMocks
    private UsoRolProcesoConLanes uso;

    @Test
    void procesosDondeSeUsa_mapeaLasFilasADto() {
        List<Object[]> filas = new ArrayList<>();
        filas.add(new Object[] {1L, "Despacho"});
        filas.add(new Object[] {2L, "Compras"});
        when(laneService.buscarProcesosDondeSeUsaRol(5L)).thenReturn(filas);

        List<ProcesoUsoDTO> resultado = uso.procesosDondeSeUsa(5L, 1L);

        assertEquals(2, resultado.size());
        assertEquals(new ProcesoUsoDTO(1L, "Despacho"), resultado.get(0));
        assertEquals("Compras", resultado.get(1).nombre());
    }

    @Test
    void procesosDondeSeUsa_sinLanes_devuelveListaVacia() {
        when(laneService.buscarProcesosDondeSeUsaRol(5L)).thenReturn(List.of());

        assertTrue(uso.procesosDondeSeUsa(5L, 1L).isEmpty());
    }
}
