package com.proyecto1.thymeleaf.services;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UsoRolProcesoSinLanesTest {

    @Test
    void sinLanes_ningunRolEstaEnUso() {
        assertTrue(new UsoRolProcesoSinLanes().procesosDondeSeUsa(1L, 1L).isEmpty());
    }
}
