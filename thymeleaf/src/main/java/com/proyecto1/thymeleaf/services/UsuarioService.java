package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.dto.UsuarioVistaDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import com.proyecto1.thymeleaf.util.PasswordUtil;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                        EmpresaRepository empresaRepository,
                        ModelMapper modelMapper,
                        PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Registrar un usuario dentro de una empresa
    public Usuario registrarUsuario(UsuarioDTO datos, Long empresaId) {
        validarDatosObligatorios(datos);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La empresa no existe"));

        String correo = normalizarCorreo(datos.getCorreo());
        String nombre = normalizarNombre(datos.getNombre());
        String password = normalizarPassword(datos.getPassword());

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo '" + correo + "'");
        }

        if (!PasswordUtil.cumpleReglasBasicas(password)) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, incluir mayúscula, minúscula y un número");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(password));
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

        return usuarioRepository.findByCorreoIgnoreCase(correo.trim())
                .filter(Usuario::getActivo)
                .filter(u -> passwordEncoder.matches(password.trim(), u.getPassword()));
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
        if (datos == null) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios");
        }
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del usuario es obligatorio");
        }
        if (datos.getNombre().trim().length() < 2) {
            throw new IllegalArgumentException("El nombre del usuario debe tener al menos 2 caracteres");
        }
        if (datos.getCorreo() == null || datos.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo del usuario es obligatorio");
        }
        if (!datos.getCorreo().trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido");
        }
        if (datos.getPassword() == null || datos.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña del usuario es obligatoria");
        }
        if (datos.getRolAcceso() == null) {
            throw new IllegalArgumentException("El rol de acceso es obligatorio");
        }
    }

    private String normalizarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo del usuario es obligatorio");
        }
        return correo.trim().toLowerCase();
    }

    private String normalizarNombre(String nombre) {
        String texto = nombre == null ? "" : nombre.trim();
        if (texto.length() < 2) {
            throw new IllegalArgumentException("El nombre del usuario debe tener al menos 2 caracteres");
        }
        return texto;
    }

    private String normalizarPassword(String password) {
        String texto = password == null ? "" : password.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("La contraseña del usuario es obligatoria");
        }
        return texto;
    }
}
