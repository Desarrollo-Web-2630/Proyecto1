package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.LoginDTO;
import com.proyecto1.thymeleaf.dto.LoginResponseDTO;
import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.dto.UsuarioVistaDTO;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.security.ContextoSeguridad;
import com.proyecto1.thymeleaf.security.JwtService;
import com.proyecto1.thymeleaf.services.UsuarioService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;

    public UsuarioController(UsuarioService usuarioService, ModelMapper modelMapper, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.modelMapper = modelMapper;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioVistaDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarPorEmpresa(ContextoSeguridad.empresaIdActual()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioVistaDTO> obtener(@PathVariable Long id) {
        Usuario u = usuarioService.obtenerPorIdYEmpresa(id, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.ok(modelMapper.map(u, UsuarioVistaDTO.class));
    }

    // Invitar un compañero a la empresa (solo ADMIN, verificado en el servicio)
    @PostMapping
    public ResponseEntity<UsuarioVistaDTO> crear(@Valid @RequestBody UsuarioDTO datos) {
        Usuario creado = usuarioService.registrarUsuario(datos, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(creado, UsuarioVistaDTO.class));
    }

    /**
     * HU-03: respuesta generica cuando falla (401 sin cuerpo), sin distinguir
     * "correo no existe" de "contraseña incorrecta" ni de "cuenta inactiva":
     * las tres dan exactamente la misma respuesta al cliente.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO login) {
        Optional<Usuario> usuario = usuarioService.login(login.getCorreo(), login.getPassword());

        return usuario.map(u -> {
            String token = jwtService.generarToken(u);
            LoginResponseDTO respuesta = new LoginResponseDTO(token, modelMapper.map(u, UsuarioVistaDTO.class));
            return ResponseEntity.ok(respuesta);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/{id}/rol")
    public ResponseEntity<UsuarioVistaDTO> cambiarRol(@PathVariable Long id, @RequestParam Usuario.RolAcceso rolAcceso) {
        Usuario actualizado = usuarioService.cambiarRol(id, rolAcceso, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.ok(modelMapper.map(actualizado, UsuarioVistaDTO.class));
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioVistaDTO> desactivar(@PathVariable Long id) {
        Usuario actualizado = usuarioService.desactivarUsuario(id, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.ok(modelMapper.map(actualizado, UsuarioVistaDTO.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.noContent().build();
    }
}
