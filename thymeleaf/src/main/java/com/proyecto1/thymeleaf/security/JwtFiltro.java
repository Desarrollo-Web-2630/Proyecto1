package com.proyecto1.thymeleaf.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

// Lee el encabezado Authorization: Bearer <token> y autentica la peticion
public class JwtFiltro extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtServicio jwtServicio;
    private final UserDetailsService userDetailsService;

    public JwtFiltro(JwtServicio jwtServicio, UserDetailsService userDetailsService) {
        this.jwtServicio = jwtServicio;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (encabezado != null && encabezado.startsWith(PREFIJO)) {
            jwtServicio.correoDelToken(encabezado.substring(PREFIJO.length()))
                    .flatMap(this::cargarUsuario)
                    // Un usuario desactivado pierde el acceso aunque su token no haya vencido
                    .filter(UserDetails::isEnabled)
                    .ifPresent(usuario -> autenticar(usuario, request));
        }
        chain.doFilter(request, response);
    }

    private Optional<UserDetails> cargarUsuario(String correo) {
        try {
            return Optional.of(userDetailsService.loadUserByUsername(correo));
        } catch (UsernameNotFoundException e) {
            return Optional.empty();
        }
    }

    private void autenticar(UserDetails usuario, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken autenticacion =
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto);
    }
}
