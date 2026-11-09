package com.proyecto1.thymeleaf.security;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

class JwtConfigTest {

    @Test
    void crearLlave_conSecretoValido_usaHmacSha256() {
        SecretKey llave = JwtConfig.crearLlave("una-clave-de-pruebas-con-mas-de-32-caracteres");

        assertEquals("HmacSHA256", llave.getAlgorithm());
    }

    @Test
    void crearLlave_conSecretoCorto_fallaAlArrancar() {
        assertThrows(IllegalStateException.class, () -> JwtConfig.crearLlave("corta"));
    }

    @Test
    void crearLlave_sinSecreto_generaUnaAleatoriaDistintaCadaVez() {
        SecretKey primera = JwtConfig.crearLlave("");
        SecretKey segunda = JwtConfig.crearLlave(null);

        assertEquals(JwtConfig.LONGITUD_MINIMA, primera.getEncoded().length);
        assertNotEquals(new String(primera.getEncoded()), new String(segunda.getEncoded()));
    }
}
