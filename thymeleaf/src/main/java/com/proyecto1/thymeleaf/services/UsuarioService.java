package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.dto.UsuarioVistaDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Logica de negocio de los usuarios (HU-02 registro, HU-03 inicio de sesion).
 *
 * Un usuario siempre pertenece a una empresa; las operaciones reciben el
 * empresaId del usuario autenticado para garantizar el aislamiento
 * multi-tenant.
 *
 * Las lecturas devuelven UsuarioVistaDTO en vez de la entidad, para que la
 * contrasena no salga de esta capa.
 */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ModelMapper modelMapper;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          EmpresaRepository empresaRepository,
                          ModelMapper modelMapper) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.modelMapper = modelMapper;
    }

    // 1. Registrar un usuario dentro de una empresa
    public Usuario registrarUsuario(UsuarioDTO datos, Long empresaId) {
        validarDatosObligatorios(datos);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La empresa no existe"));

        String correo = datos.getCorreo().trim();
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo '" + correo + "'");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(datos.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setPassword(datos.getPassword().trim());
        usuario.setRolAcceso(datos.getRolAcceso());
        usuario.setEmpresa(empresa);
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    // 2. Iniciar sesion (provisional, mientras entra Spring Security)
    @Transactional(readOnly = true)
    public Optional<Usuario> login(String correo, String password) {
        if (correo == null || correo.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }

        return usuarioRepository.findByCorreo(correo.trim())
                .filter(Usuario::getActivo)
                .filter(u -> u.getPassword().equals(password.trim()));
    }

    // 3. Listar los usuarios de la empresa, sin exponer la contrasena
    @Transactional(readOnly = true)
    public List<UsuarioVistaDTO> listarPorEmpresa(Long empresaId) {
        return usuarioRepository.findByEmpresaId(empresaId).stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioVistaDTO.class))
                .toList();
    }

    // 4. Obtener un usuario verificando que pertenezca a la empresa
    @Transactional(readOnly = true)
    public Usuario obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return usuarioRepository.findById(id)
                .filter(u -> u.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe o no pertenece a su empresa"));
    }

    // 5. Desactivar un usuario
    public Usuario desactivarUsuario(Long id, Long empresaId) {
        Usuario usuario = obtenerPorIdYEmpresa(id, empresaId);
        usuario.setActivo(false);
        return usuarioRepository.save(usuario);
    }

    // 6. Cambiar el rol de acceso de un usuario
    public Usuario cambiarRol(Long id, Usuario.RolAcceso nuevoRol, Long empresaId) {
        if (nuevoRol == null) {
            throw new IllegalArgumentException("El nuevo rol de acceso es obligatorio");
        }
        Usuario usuario = obtenerPorIdYEmpresa(id, empresaId);
        usuario.setRolAcceso(nuevoRol);
        return usuarioRepository.save(usuario);
    }

    // 7. Eliminar un usuario
    public void eliminarUsuario(Long id, Long empresaId) {
        Usuario usuario = obtenerPorIdYEmpresa(id, empresaId);
        usuarioRepository.delete(usuario);
    }

    private void validarDatosObligatorios(UsuarioDTO datos) {
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del usuario es obligatorio");
        }
        if (datos.getCorreo() == null || datos.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo del usuario es obligatorio");
        }
        if (datos.getPassword() == null || datos.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña del usuario es obligatoria");
        }
        if (datos.getRolAcceso() == null) {
            throw new IllegalArgumentException("El rol de acceso es obligatorio");
        }
    }
}
