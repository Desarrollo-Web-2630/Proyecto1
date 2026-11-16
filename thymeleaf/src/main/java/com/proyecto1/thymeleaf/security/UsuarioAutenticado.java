package com.proyecto1.thymeleaf.security;

import com.proyecto1.thymeleaf.dto.UsuarioVistaDTO;
import com.proyecto1.thymeleaf.model.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Usuario que queda en el contexto de seguridad despues de validar el token
@Getter
public class UsuarioAutenticado implements UserDetails {

    private final Long id;
    private final String correo;
    private final String nombre;
    private final String password;
    private final Long empresaId;
    private final Usuario.RolAcceso rolAcceso;
    private final boolean activo;

    public UsuarioAutenticado(Long id, String correo, String nombre, String password,
                              Long empresaId, Usuario.RolAcceso rolAcceso, boolean activo) {
        this.id = id;
        this.correo = correo;
        this.nombre = nombre;
        this.password = password;
        this.empresaId = empresaId;
        this.rolAcceso = rolAcceso;
        this.activo = activo;
    }

    public static UsuarioAutenticado desde(Usuario usuario) {
        return new UsuarioAutenticado(usuario.getId(), usuario.getCorreo(), usuario.getNombre(),
                usuario.getPassword(), usuario.getEmpresa().getId(), usuario.getRolAcceso(),
                Boolean.TRUE.equals(usuario.getActivo()));
    }

    public UsuarioVistaDTO aVista() {
        return new UsuarioVistaDTO(id, nombre, correo, rolAcceso, activo, empresaId);
    }

    // Spring Security exige el prefijo ROLE_ para usar hasRole("ADMIN")
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rolAcceso.name()));
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
