package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.LoginDTO;
import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.dto.UsuarioVistaDTO;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.services.UsuarioService;
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
    private final Long EMPRESA_ID_MOCK = 1L;

    public UsuarioController(UsuarioService usuarioService, ModelMapper modelMapper) {
        this.usuarioService = usuarioService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioVistaDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarPorEmpresa(EMPRESA_ID_MOCK));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioVistaDTO> obtener(@PathVariable Long id) {
        Usuario u = usuarioService.obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK);
        return ResponseEntity.ok(modelMapper.map(u, UsuarioVistaDTO.class));
    }

    @PostMapping
    public ResponseEntity<UsuarioVistaDTO> crear(@RequestBody UsuarioDTO datos) {
        Usuario creado = usuarioService.registrarUsuario(datos, EMPRESA_ID_MOCK);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(creado, UsuarioVistaDTO.class));
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioVistaDTO> login(@RequestBody LoginDTO login) {
        Optional<Usuario> usuario = usuarioService.login(login.getCorreo(), login.getPassword());
        return usuario.map(u -> ResponseEntity.ok(modelMapper.map(u, UsuarioVistaDTO.class)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/{id}/rol")
    public ResponseEntity<UsuarioVistaDTO> cambiarRol(@PathVariable Long id, @RequestParam Usuario.RolAcceso rolAcceso) {
        Usuario actualizado = usuarioService.cambiarRol(id, rolAcceso, EMPRESA_ID_MOCK);
        return ResponseEntity.ok(modelMapper.map(actualizado, UsuarioVistaDTO.class));
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioVistaDTO> desactivar(@PathVariable Long id) {
        Usuario actualizado = usuarioService.desactivarUsuario(id, EMPRESA_ID_MOCK);
        return ResponseEntity.ok(modelMapper.map(actualizado, UsuarioVistaDTO.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id, EMPRESA_ID_MOCK);
        return ResponseEntity.noContent().build();
    }
}
