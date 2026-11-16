package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.dto.UsuarioVistaDTO;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.services.UsuarioService;
import com.proyecto1.thymeleaf.util.EmpresaActual;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/{id}/rol")
    public ResponseEntity<UsuarioVistaDTO> cambiarRol(@PathVariable Long id, @RequestParam Usuario.RolAcceso rolAcceso) {
        noEsElMismo(id, "No puede cambiar su propio rol");
        Usuario actualizado = usuarioService.cambiarRol(id, rolAcceso, EmpresaActual.id());
        return ResponseEntity.ok(modelMapper.map(actualizado, UsuarioVistaDTO.class));
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioVistaDTO> desactivar(@PathVariable Long id) {
        noEsElMismo(id, "No puede desactivar su propia cuenta");
        Usuario actualizado = usuarioService.desactivarUsuario(id, EmpresaActual.id());
        return ResponseEntity.ok(modelMapper.map(actualizado, UsuarioVistaDTO.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        noEsElMismo(id, "No puede eliminar su propia cuenta");
        usuarioService.eliminarUsuario(id, EmpresaActual.id());
        return ResponseEntity.noContent().build();
    }

    // Evita que el administrador se quite el acceso a si mismo
    private void noEsElMismo(Long id, String mensaje) {
        if (id.equals(EmpresaActual.usuario().getId())) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
