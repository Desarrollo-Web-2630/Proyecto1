package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Logica de negocio usuario
 *
 * Un usuario siempre pertenece a una empresa; las operaciones reciben el
 * empresaId del usuario autenticado para garantizar el aislamiento
 * multi-tenant.
 */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, EmpresaRepository empresaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
    }

    // 1. Registrar un usuario dentro de una empresa
    public Usuario registrarUsuario(Usuario usuario, Long empresaId) {
        validarDatosObligatorios(usuario);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La empresa no existe"));

        String correo = usuario.getCorreo().trim();
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo '" + correo + "'");
        }

        usuario.setNombre(usuario.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setPassword(usuario.getPassword().trim());
        usuario.setEmpresa(empresa);
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    // 2. Iniciar sesion (placeholder, mientras se implementa apropiadamente)
    @Transactional(readOnly = true)
    public Optional<Usuario> login(String correo, String password) {
        if (correo == null || correo.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }

        return usuarioRepository.findByCorreo(correo.trim())
                .filter(Usuario::getActivo)
                .filter(u -> u.getPassword().equals(password.trim()));
    }

    // 3. Listar los usuarios de la empresa
    @Transactional(readOnly = true)
    public List<Usuario> listarPorEmpresa(Long empresaId) {
        return usuarioRepository.findByEmpresaId(empresaId);
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

    private void validarDatosObligatorios(Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del usuario es obligatorio");
        }
        if (usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo del usuario es obligatorio");
        }
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contrasena del usuario es obligatoria");
        }
        if (usuario.getRolAcceso() == null) {
            throw new IllegalArgumentException("El rol de acceso es obligatorio");
        }
    }
}
