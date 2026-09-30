package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.LoginDTO;
import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.dto.UsuarioVistaDTO;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.services.UsuarioService;
import com.proyecto1.thymeleaf.util.EmpresaActual;
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

    public UsuarioController(UsuarioService usuarioService, ModelMapper modelMapper) {
        this.usuarioService = usuarioService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioVistaDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarPorEmpresa(EmpresaActual.id()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioVistaDTO> obtener(@PathVariable Long id) {
        Usuario u = usuarioService.obtenerPorIdYEmpresa(id, EmpresaActual.id());
        return ResponseEntity.ok(modelMapper.map(u, UsuarioVistaDTO.class));
    }

    // Invitar un companero a la empresa: nace inactivo y recibe el correo de verificacion
    @PostMapping
    public ResponseEntity<UsuarioVistaDTO> crear(@Valid @RequestBody UsuarioDTO datos) {
        Usuario creado = usuarioService.registrarUsuario(datos, EmpresaActual.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(creado, UsuarioVistaDTO.class));
    }

    /**
     * HU-03 (entrega 2): valida correo + contrasena + cuenta activa y devuelve
     * el usuario. Falla con 401 sin cuerpo, sin distinguir "correo no existe"
     * de "contrasena incorrecta" ni de "cuenta inactiva". La emision de un
     * token de sesion es de la entrega 3.
     */
    @PostMapping("/login")
    public ResponseEntity<UsuarioVistaDTO> login(@Valid @RequestBody LoginDTO login) {
        Optional<Usuario> usuario = usuarioService.login(login.getCorreo(), login.getPassword());
        return usuario.map(u -> ResponseEntity.ok(modelMapper.map(u, UsuarioVistaDTO.class)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/{id}/rol")
    public ResponseEntity<UsuarioVistaDTO> cambiarRol(@PathVariable Long id, @RequestParam Usuario.RolAcceso rolAcceso) {
        Usuario actualizado = usuarioService.cambiarRol(id, rolAcceso, EmpresaActual.id());
        return ResponseEntity.ok(modelMapper.map(actualizado, UsuarioVistaDTO.class));
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioVistaDTO> desactivar(@PathVariable Long id) {
        Usuario actualizado = usuarioService.desactivarUsuario(id, EmpresaActual.id());
        return ResponseEntity.ok(modelMapper.map(actualizado, UsuarioVistaDTO.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id, EmpresaActual.id());
        return ResponseEntity.noContent().build();
    }
}
