package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.EmpresaDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import com.proyecto1.thymeleaf.security.ContextoSeguridad;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * Logica de negocio de las empresas (HU-01 Registro de empresa).
 *
 * La empresa es la raiz del aislamiento multi-tenant: no recibe empresaId
 * porque ella misma es el tenant.
 *
 * El administrador inicial nunca recibe una contrasena fija ni utilizable al
 * crearse: se le asigna un valor aleatorio, jamas revelado, y queda inactivo
 * hasta que active su cuenta con POST /api/auth/activar-cuenta, donde elige
 * su propia contrasena. Antes de este cambio la contrasena era un literal
 * fijo, igual para todas las empresas y visible en el codigo fuente publico:
 * cualquiera podia iniciar sesion como administrador de cualquier empresa
 * solo sabiendo su correo de contacto.
 */
@Service
@Transactional
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificacionCorreoService verificacionCorreoService;

    public EmpresaService(EmpresaRepository empresaRepository,
                         UsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder,
                         VerificacionCorreoService verificacionCorreoService) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificacionCorreoService = verificacionCorreoService;
    }

    // 1. "Listar" para un usuario normal es ver la propia empresa, no las de
    //    los demas: sin este filtro, cualquier usuario autenticado de
    //    cualquier empresa podia ver el nombre, NIT y correo de contacto de
    //    todas las empresas del sistema con GET /api/v1/empresas.
    @Transactional(readOnly = true)
    public List<Empresa> listarTodas() {
        return empresaRepository.findById(ContextoSeguridad.empresaIdActual())
                .map(List::of)
                .orElseGet(List::of);
    }

    // 2. Obtener una empresa por su id: solo la propia, nunca la de otro tenant
    @Transactional(readOnly = true)
    public Empresa obtenerPorId(Long id) {
        ContextoSeguridad.exigirPropiaEmpresa(id);
        return empresaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La empresa no existe"));
    }

    // 3. Registrar una empresa nueva junto con su administrador inicial
    public Empresa registrarEmpresa(EmpresaDTO datos) {
        validarEmpresa(datos);

        String nit = normalizar(datos.getNit(), "El NIT de la empresa es obligatorio");
        String correo = normalizar(datos.getCorreo(), "El correo de la empresa es obligatorio").toLowerCase();

        if (empresaRepository.existsByNit(nit)) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con el NIT '" + nit + "'");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con el correo '" + correo + "'");
        }

        Empresa empresa = new Empresa();
        empresa.setNombre(normalizar(datos.getNombre(), "El nombre de la empresa es obligatorio"));
        empresa.setNit(nit);
        empresa.setCorreo(correo);
        Empresa empresaGuardada = empresaRepository.save(empresa);

        Usuario administrador = new Usuario();
        administrador.setNombre("Administrador de " + empresaGuardada.getNombre());
        administrador.setCorreo(correo);
        // Contrasena aleatoria y descartable: nadie la conoce, ni siquiera este
        // servicio despues de este punto. Solo sirve para que la columna NOT
        // NULL tenga un valor; nunca podra usarse para iniciar sesion porque
        // BCrypt no es reversible y el valor en claro no se guarda en ningun
        // lado. La cuenta solo se vuelve utilizable via activar-cuenta.
        administrador.setPassword(passwordEncoder.encode(generarPasswordDescartable()));
        administrador.setRolAcceso(Usuario.RolAcceso.ADMIN);
        administrador.setActivo(false);
        administrador.setEmpresa(empresaGuardada);
        Usuario administradorGuardado = usuarioRepository.save(administrador);

        verificacionCorreoService.crearYEnviarToken(administradorGuardado);

        return empresaGuardada;
    }

    // 4. Actualizar los datos de una empresa (solo el administrador de esa empresa)
    public Empresa actualizarEmpresa(Long id, EmpresaDTO datos) {
        Empresa empresaExistente = obtenerPorId(id);
        ContextoSeguridad.exigirAdmin("editar los datos de la empresa");
        validarEmpresa(datos);

        String nit = normalizar(datos.getNit(), "El NIT de la empresa es obligatorio");
        String correo = normalizar(datos.getCorreo(), "El correo de la empresa es obligatorio").toLowerCase();

        if (!nit.equals(empresaExistente.getNit()) && empresaRepository.existsByNit(nit)) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con el NIT '" + nit + "'");
        }

        if (!correo.equalsIgnoreCase(empresaExistente.getCorreo())
                && usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("El correo de contacto ya esta asociado a otra empresa");
        }

        empresaExistente.setNombre(normalizar(datos.getNombre(), "El nombre de la empresa es obligatorio"));
        empresaExistente.setNit(nit);
        empresaExistente.setCorreo(correo);

        return empresaRepository.save(empresaExistente);
    }

    // 5. Eliminar (borrado logico via @SQLDelete; solo el administrador de esa empresa)
    public void eliminarEmpresa(Long id) {
        Empresa empresa = obtenerPorId(id);
        ContextoSeguridad.exigirAdmin("eliminar la empresa");
        empresaRepository.delete(empresa);
    }

    private void validarEmpresa(EmpresaDTO datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos de la empresa son obligatorios");
        }
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio");
        }
        if (datos.getNombre().trim().length() < 2) {
            throw new IllegalArgumentException("El nombre de la empresa debe tener al menos 2 caracteres");
        }
        if (datos.getNit() == null || datos.getNit().isBlank()) {
            throw new IllegalArgumentException("El NIT de la empresa es obligatorio");
        }
        if (!datos.getNit().trim().matches("^\\d{9,12}(?:-\\d)?$")) {
            throw new IllegalArgumentException("El NIT debe tener formato numerico valido, por ejemplo 900123456-7");
        }
        if (datos.getCorreo() == null || datos.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo de la empresa es obligatorio");
        }
        if (!datos.getCorreo().trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("El correo no tiene un formato valido");
        }
    }

    private String normalizar(String valor, String mensajeSiFalta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensajeSiFalta);
        }
        return valor.trim();
    }

    private String generarPasswordDescartable() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
