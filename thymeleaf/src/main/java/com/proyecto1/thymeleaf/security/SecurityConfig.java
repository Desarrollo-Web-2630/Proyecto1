package com.proyecto1.thymeleaf.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

// Autenticacion: JWT en el encabezado Authorization: Bearer <token>, validado en cada peticion.
// Autorizacion por rol de acceso (claim "rol"):
//   LECTURA -> solo GET
//   EDITOR  -> GET + crear y editar elementos de procesos
//   ADMIN   -> todo, incluido eliminar, inactivar y administrar usuarios, roles y empresa
// El aislamiento por empresa no depende de estas reglas: cada servicio filtra por la empresa
// del token (EmpresaActual.id()) y un recurso de otra empresa responde 404.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String EDITOR = "EDITOR";
    private static final String API = "/api/**";

    // Rutas que no exigen token: login, registro de empresa, activacion de cuenta y documentacion
    static final RequestMatcher RUTAS_PUBLICAS = new OrRequestMatcher(
            antMatcher(HttpMethod.OPTIONS, "/**"),
            antMatcher(HttpMethod.POST, "/api/v1/usuarios/login"),
            antMatcher(HttpMethod.POST, "/api/v1/empresas"),
            antMatcher("/api/auth/**"),
            antMatcher("/v3/api-docs/**"),
            antMatcher("/swagger-ui/**"),
            antMatcher("/swagger-ui.html"),
            antMatcher("/error"));

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        ManejadorErroresSeguridad manejador = new ManejadorErroresSeguridad(objectMapper);

        http
                // API stateless con token en encabezado (no en cookie): el navegador no lo envia solo,
                // asi que un sitio externo no puede falsificar peticiones (CSRF) en nombre del usuario.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        // Herramienta de desarrollo (solo existe en perfiles h2/local/postman)
                        .requestMatchers(antMatcher("/api/v1/debug/**")).hasRole(ADMIN)
                        // Consultar: cualquier usuario autenticado
                        .requestMatchers(antMatcher(HttpMethod.GET, API)).authenticated()
                        // Administrar la empresa, sus usuarios y sus roles de proceso: solo ADMIN
                        .requestMatchers(antMatcher("/api/v1/empresas/**"), antMatcher("/api/v1/usuarios/**"),
                                antMatcher("/api/v1/roles-proceso/**")).hasRole(ADMIN)
                        // Eliminar, inactivar y reactivar: solo ADMIN
                        .requestMatchers(antMatcher(HttpMethod.DELETE, API)).hasRole(ADMIN)
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/v1/procesos/*/inactivar"),
                                antMatcher(HttpMethod.POST, "/api/v1/procesos/*/reactivar")).hasRole(ADMIN)
                        // Crear y editar procesos y sus elementos: ADMIN o EDITOR
                        .requestMatchers(antMatcher(API)).hasAnyRole(ADMIN, EDITOR)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .bearerTokenResolver(resolvedorDeToken())
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorDeRoles()))
                        .authenticationEntryPoint(manejador)
                        .accessDeniedHandler(manejador))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(manejador)
                        .accessDeniedHandler(manejador));

        return http.build();
    }

    // En las rutas publicas se ignora el encabezado Authorization: un token viejo que el
    // cliente haya dejado guardado no debe impedir el login ni el registro.
    static BearerTokenResolver resolvedorDeToken() {
        DefaultBearerTokenResolver porDefecto = new DefaultBearerTokenResolver();
        return request -> RUTAS_PUBLICAS.matches(request) ? null : porDefecto.resolve(request);
    }

    // Convierte el claim "rol" (ADMIN | EDITOR | LECTURA) en la autoridad ROLE_<rol>
    static JwtAuthenticationConverter convertidorDeRoles() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(TokenService.CLAIM_ROL);
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }

    // Origenes del front autorizados a llamar la API desde el navegador (app.cors.origenes).
    // Con el proxy de ng serve o de nginx el front y la API comparten origen y esto no se usa.
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origenes:http://localhost:4200}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origenes.split(","))
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(API, config);
        return source;
    }
}
