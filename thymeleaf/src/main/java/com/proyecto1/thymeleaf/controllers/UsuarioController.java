package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.services.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controlador MVC de usuarios (HU-02 registro en empresa, HU-03 inicio de sesion).
 *
 * El login de aqui es provisional: guarda el usuario en la sesion HTTP.
 * Cuando entre Spring Security debe reemplazarse por su formulario de
 * autenticacion y el EMPRESA_ID_MOCK desaparece.
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ID de empresa simulado (reemplazar por el ID de la sesion cuando entre Spring Security)
    private final Long EMPRESA_ID_MOCK = 1L;

    // 1. Listar los usuarios de la empresa
    @GetMapping
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioService.listarPorEmpresa(EMPRESA_ID_MOCK);

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("rolesAcceso", Usuario.RolAcceso.values());
        return "usuarios/lista";
    }

    // 2. Registrar un usuario: mostrar formulario
    @GetMapping("/nuevo")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("rolesAcceso", Usuario.RolAcceso.values());
        return "usuarios/formulario";
    }

    // 3. Registrar
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute("usuario") Usuario usuario,
                                 RedirectAttributes redirectAttributes) {
        try {
            usuarioService.registrarUsuario(usuario, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario registrado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    // 4. Formulario de inicio de sesion
    @GetMapping("/login")
    public String mostrarFormularioLogin() {
        return "usuarios/login";
    }

    // 5. Iniciar sesion
    @PostMapping("/login")
    public String iniciarSesion(@RequestParam String correo,
                                @RequestParam String password,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuario = usuarioService.login(correo, password);

        if (usuario.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Correo o contraseña incorrectos.");
            return "redirect:/usuarios/login";
        }

        session.setAttribute("usuarioId", usuario.get().getId());
        session.setAttribute("empresaId", usuario.get().getEmpresa().getId());
        return "redirect:/procesos";
    }

    // 6. Cerrar sesion
    @PostMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/usuarios/login";
    }

    // 7. Cambiar el rol de acceso de un usuario
    @PostMapping("/rol/{id}")
    public String cambiarRol(@PathVariable Long id,
                             @RequestParam Usuario.RolAcceso rolAcceso,
                             RedirectAttributes redirectAttributes) {
        try {
            usuarioService.cambiarRol(id, rolAcceso, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Rol actualizado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    // 8. Desactivar un usuario sin borrarlo
    @PostMapping("/desactivar/{id}")
    public String desactivarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.desactivarUsuario(id, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario desactivado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    // 9. Eliminar
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
