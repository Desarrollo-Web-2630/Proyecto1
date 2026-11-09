package com.proyecto1.thymeleaf.security;

import com.proyecto1.thymeleaf.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

// Emite el JWT del login. Los claims son lo unico que el backend necesita para autorizar:
// quien es (sub, uid), de que empresa (empresaId) y con que rol de acceso (rol).
@Service
public class TokenService {

    public static final String CLAIM_USUARIO = "uid";
    public static final String CLAIM_EMPRESA = "empresaId";
    public static final String CLAIM_ROL = "rol";
    public static final String EMISOR = "procesos-api";

    private final JwtEncoder jwtEncoder;
    private final Duration duracion;

    public TokenService(JwtEncoder jwtEncoder, @Value("${app.jwt.expiracion-minutos:60}") long minutos) {
        if (minutos <= 0) {
            throw new IllegalStateException("app.jwt.expiracion-minutos debe ser mayor que cero");
        }
        this.jwtEncoder = jwtEncoder;
        this.duracion = Duration.ofMinutes(minutos);
    }

    public TokenEmitido emitir(Usuario usuario) {
        if (usuario == null || usuario.getId() == null || usuario.getEmpresa() == null
                || usuario.getEmpresa().getId() == null || usuario.getRolAcceso() == null) {
            throw new IllegalArgumentException("El usuario no tiene los datos necesarios para emitir el token");
        }
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(duracion);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISOR)
                .subject(usuario.getCorreo())
                .issuedAt(ahora)
                .expiresAt(expira)
                .claim(CLAIM_USUARIO, usuario.getId())
                .claim(CLAIM_EMPRESA, usuario.getEmpresa().getId())
                .claim(CLAIM_ROL, usuario.getRolAcceso().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenEmitido(token, expira);
    }

    public record TokenEmitido(String token, Instant expiraEn) {
    }
}
