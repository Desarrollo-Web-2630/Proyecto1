package com.proyecto1.thymeleaf.security;

import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private final JwtConfig config = new JwtConfig();
    private final SecretKey llave = JwtConfig.crearLlave("una-clave-de-pruebas-con-mas-de-32-caracteres");
    private final TokenService tokenService = new TokenService(config.jwtEncoder(llave), 60);
    private final JwtDecoder decoder = config.jwtDecoder(llave);

    @Test
    void emitir_generaUnTokenQueElDecoderAceptaConLosClaimsDelUsuario() {
        TokenService.TokenEmitido emitido = tokenService.emitir(usuario());

        Jwt jwt = decoder.decode(emitido.token());
        assertEquals("ana@demo.com", jwt.getSubject());
        assertEquals(5L, ((Number) jwt.getClaims().get(TokenService.CLAIM_USUARIO)).longValue());
        assertEquals(9L, ((Number) jwt.getClaims().get(TokenService.CLAIM_EMPRESA)).longValue());
        assertEquals("EDITOR", jwt.getClaimAsString(TokenService.CLAIM_ROL));
        assertEquals(emitido.expiraEn().getEpochSecond(), jwt.getExpiresAt().getEpochSecond());
    }

    @Test
    void emitir_expiraSegunLaDuracionConfigurada() {
        Instant antes = Instant.now();
        TokenService.TokenEmitido emitido = tokenService.emitir(usuario());

        Duration vigencia = Duration.between(antes, emitido.expiraEn());
        assertTrue(vigencia.compareTo(Duration.ofMinutes(59)) > 0);
        assertTrue(vigencia.compareTo(Duration.ofMinutes(61)) < 0);
    }

    @Test
    void decoder_rechazaUnTokenAlterado() {
        String token = tokenService.emitir(usuario()).token();
        int firma = token.lastIndexOf('.') + 1;
        char cambio = token.charAt(firma) == 'A' ? 'B' : 'A';
        String alterado = token.substring(0, firma) + cambio + token.substring(firma + 1);

        assertThrows(JwtException.class, () -> decoder.decode(alterado));
    }

    @Test
    void decoder_rechazaUnTokenFirmadoConOtraClave() {
        SecretKey otra = JwtConfig.crearLlave("otra-clave-distinta-tambien-de-32-caracteres");
        String ajeno = new TokenService(config.jwtEncoder(otra), 60).emitir(usuario()).token();

        assertThrows(JwtException.class, () -> decoder.decode(ajeno));
    }

    @Test
    void emitir_sinEmpresa_lanzaExcepcion() {
        Usuario incompleto = usuario();
        incompleto.setEmpresa(null);

        assertThrows(IllegalArgumentException.class, () -> tokenService.emitir(incompleto));
    }

    @Test
    void constructor_conDuracionInvalida_falla() {
        assertThrows(IllegalStateException.class, () -> new TokenService(config.jwtEncoder(llave), 0));
    }

    private static Usuario usuario() {
        Empresa empresa = new Empresa();
        empresa.setId(9L);
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setCorreo("ana@demo.com");
        usuario.setRolAcceso(Usuario.RolAcceso.EDITOR);
        usuario.setEmpresa(empresa);
        return usuario;
    }
}
