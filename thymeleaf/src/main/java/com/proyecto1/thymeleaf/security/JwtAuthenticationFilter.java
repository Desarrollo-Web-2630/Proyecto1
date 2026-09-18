package com.proyecto1.thymeleaf.security;

import com.proyecto1.thymeleaf.model.Usuario;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Lee "Authorization: Bearer <token>", valida el JWT y, si es valido, deja un
 * UsuarioPrincipal en el SecurityContext para el resto de la peticion.
 *
 * Si el token falta, esta mal formado o expiro, simplemente no autentica: no
 * corta la cadena de filtros ni escribe una respuesta de error aqui. Es
 * authorizeHttpRequests() en SecurityConfig quien decide, segun la ruta, si
 * una peticion sin autenticar se rechaza (401) o esta permitida
 * (registro, login, verificacion de correo).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith(PREFIJO)) {
            String token = encabezado.substring(PREFIJO.length());
            Optional<Claims> claims = jwtService.validarYExtraerClaims(token);

            claims.ifPresent(c -> {
                Long usuarioId = Long.valueOf(c.getSubject());
                Long empresaId = c.get("empresaId", Number.class).longValue();
                Usuario.RolAcceso rol = Usuario.RolAcceso.valueOf(c.get("rolAcceso", String.class));
                String correo = c.get("correo", String.class);

                UsuarioPrincipal principal = new UsuarioPrincipal(usuarioId, empresaId, rol, correo);

                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol.name())));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }
}
