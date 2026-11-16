package com.proyecto1.thymeleaf.security;

import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Carga el usuario por correo para el login y para cada peticion con token
@Service
public class UsuarioDetallesServicio implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetallesServicio(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) {
        String valor = correo == null ? "" : correo.trim();
        return usuarioRepository.findByCorreoIgnoreCase(valor)
                .map(UsuarioAutenticado::desde)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
