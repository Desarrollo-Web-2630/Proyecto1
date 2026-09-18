package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.LoginDTO;
import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.services.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listarUsuarios(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Long empresaId = obtenerEmpresaId(session, redirectAttributes);
        if (empresaId == null) {
            return "redirect:/usuarios/login";
        }

        List<Usuario> usuarios = usuarioService.listarPorEmpresa(empresaId);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("rolesAcceso", Usuario.RolAcceso.values());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new UsuarioDTO());
        model.addAttribute("rolesAcceso", Usuario.RolAcceso.values());
        return "usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@Valid @ModelAttribute("usuario") UsuarioDTO usuario,
                                BindingResult resultado,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        model.addAttribute("rolesAcceso", Usuario.RolAcceso.values());
        Long empresaId = obtenerEmpresaId(session, redirectAttributes);
        if (empresaId == null) {
            return "redirect:/usuarios/login";
        }
        if (resultado.hasErrors()) {
            return "usuarios/formulario";
        }
        try {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(usuario.getNombre());
            nuevoUsuario.setCorreo(usuario.getCorreo());
            nuevoUsuario.setPassword(usuario.getPassword());
            nuevoUsuario.setRolAcceso(usuario.getRolAcceso());
            usuarioService.registrarUsuario(nuevoUsuario, empresaId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario registrado con exito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "usuarios/formulario";
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/login")
    public String mostrarFormularioLogin(Model model) {
        model.addAttribute("login", new LoginDTO());
        return "usuarios/login";
    }

    @PostMapping("/login")
    public String iniciarSesion(@Valid @ModelAttribute("login") LoginDTO login,
                                BindingResult resultado,
                                HttpSession session,
                                Model model) {
        if (resultado.hasErrors()) {
            return "usuarios/login";
        }

        Optional<Usuario> usuario = usuarioService.login(login.getCorreo(), login.getPassword());
        if (usuario.isEmpty()) {
            model.addAttribute("mensajeError", "Correo o contrasena incorrectos.");
            return "usuarios/login";
        }

        session.setAttribute("usuarioId", usuario.get().getId());
        session.setAttribute("empresaId", usuario.get().getEmpresa().getId());
        return "redirect:/procesos";
    }

    @PostMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/usuarios/login";
    }

    @PostMapping("/rol/{id}")
    public String cambiarRol(@PathVariable Long id,
                            @RequestParam Usuario.RolAcceso rolAcceso,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Long empresaId = obtenerEmpresaId(session, redirectAttributes);
        if (empresaId == null) {
            return "redirect:/usuarios/login";
        }
        try {
            usuarioService.cambiarRol(id, rolAcceso, empresaId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Rol actualizado con exito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/desactivar/{id}")
    public String desactivarUsuario(@PathVariable Long id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Long empresaId = obtenerEmpresaId(session, redirectAttributes);
        if (empresaId == null) {
            return "redirect:/usuarios/login";
        }
        try {
            usuarioService.desactivarUsuario(id, empresaId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario desactivado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long empresaId = obtenerEmpresaId(session, redirectAttributes);
        if (empresaId == null) {
            return "redirect:/usuarios/login";
        }
        try {
            usuarioService.eliminarUsuario(id, empresaId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    private Long obtenerEmpresaId(HttpSession session, RedirectAttributes redirectAttributes) {
        Object empresaId = session.getAttribute("empresaId");
        if (empresaId == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe iniciar sesion para acceder a su empresa.");
            return null;
        }
        return Long.valueOf(empresaId.toString());
    }
}
