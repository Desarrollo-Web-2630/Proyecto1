package com.proyecto1.thymeleaf.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void guardarPasswordYValidarPassword_debenCoincidir() {
        String hash = PasswordUtil.guardarPassword("Clave1234");
        assertTrue(PasswordUtil.validarPassword("Clave1234", hash));
    }

    @Test
    void validarPassword_conClaveIncorrecta_devuelveFalse() {
        String hash = PasswordUtil.guardarPassword("Clave1234");
        assertFalse(PasswordUtil.validarPassword("OtraClave1", hash));
    }

    @Test
    void guardarPassword_conNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.guardarPassword(null));
    }

    @Test
    void guardarPassword_conBlanco_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.guardarPassword("   "));
    }

    @Test
    void validarPassword_conGuardadaNula_devuelveFalse() {
        assertFalse(PasswordUtil.validarPassword("Clave1234", null));
    }

    @Test
    void validarPassword_conIngresadaNula_devuelveFalse() {
        String hash = PasswordUtil.guardarPassword("Clave1234");
        assertFalse(PasswordUtil.validarPassword(null, hash));
    }

    @Test
    void validarPassword_conFormatoMalFormado_devuelveFalse() {
        assertFalse(PasswordUtil.validarPassword("Clave1234", "hashSinDosPuntos"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Abcdefg1", "Zz999999"})
    void cumpleReglasBasicas_conPasswordValida_devuelveTrue(String password) {
        assertTrue(PasswordUtil.cumpleReglasBasicas(password));
    }

    @Test
    void cumpleReglasBasicas_conNulo_devuelveFalse() {
        assertFalse(PasswordUtil.cumpleReglasBasicas(null));
    }

    @Test
    void cumpleReglasBasicas_muyCorta_devuelveFalse() {
        assertFalse(PasswordUtil.cumpleReglasBasicas("Ab1"));
    }

    @Test
    void cumpleReglasBasicas_sinMayuscula_devuelveFalse() {
        assertFalse(PasswordUtil.cumpleReglasBasicas("abcdefg1"));
    }

    @Test
    void cumpleReglasBasicas_sinMinuscula_devuelveFalse() {
        assertFalse(PasswordUtil.cumpleReglasBasicas("ABCDEFG1"));
    }

    @Test
    void cumpleReglasBasicas_sinNumero_devuelveFalse() {
        assertFalse(PasswordUtil.cumpleReglasBasicas("Abcdefgh"));
    }
}