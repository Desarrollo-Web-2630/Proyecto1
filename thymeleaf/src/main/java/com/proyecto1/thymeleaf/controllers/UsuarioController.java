package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.LoginDTO;
import com.proyecto1.thymeleaf.dto.LoginResponseDTO;
import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.dto.UsuarioVistaDTO;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.security.TokenService;
import com.proyecto1.thymeleaf.services.UsuarioService;
import com.proyecto1.thymeleaf.util.EmpresaActual;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    static final String MENSAJE_LOGIN_FALLIDO = "Correo o contrasena incorrectos";

    private final UsuarioService usuarioService;
    private final ModelMapper modelMapper;
    private final TokenService tokenService;

    public UsuarioController(UsuarioService usuarioService, ModelMapper modelMapper, TokenService tokenService) {
        this.usuarioService = usuarioService;
        this.modelMapper = modelMapper;
        this.tokenService = tokenService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioVistaDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarPorEmpresa(EmpresaActual.id()));
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<UsuarioVistaDTO>> buscar(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        int tamano = Math.min(Math.max(size, 1), 100);
        return ResponseEntity.ok(usuarioService.buscarPorEmpresa(EmpresaActual.id(),
                PageRequest.of(Math.max(page, 0), tamano, Sort.by("nombre").ascending())));
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

    // HU-03: publico. Devuelve el JWT que el front envia en cada peticion.
    // Mismo 401 si el correo no existe, la clave esta mal o la cuenta esta inactiva:
    // asi no se revela que correos estan registrados.
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO login) {
        Usuario usuario = usuarioService.login(login.getCorreo(), login.getPassword())
                .orElseThrow(() -> new BadCredentialsException(MENSAJE_LOGIN_FALLIDO));
        TokenService.TokenEmitido token = tokenService.emitir(usuario);
        return ResponseEntity.ok(new LoginResponseDTO(token.token(), LoginResponseDTO.TIPO_BEARER,
                token.expiraEn(), modelMapper.map(usuario, UsuarioVistaDTO.class)));
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
