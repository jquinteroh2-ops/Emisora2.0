package com.emisora.controller;

import com.emisora.entity.Usuario;
import com.emisora.exception.BusinessRuleException;
import com.emisora.exception.DuplicateResourceException;
import com.emisora.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador Spring MVC para la administración de Usuarios.
 * Acceso restringido exclusivamente a usuarios con rol ADMIN.
 * Implementa el CRUD completo: Crear, Consultar/Listar, Actualizar y Eliminar.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Listado general y búsqueda de usuarios.
     */
    @GetMapping
    public String listar(@RequestParam(value = "buscar", required = false) String buscar, Model model) {
        List<Usuario> lista = (buscar != null && !buscar.isBlank())
                ? usuarioService.buscarPorTermino(buscar)
                : usuarioService.listarTodos();

        model.addAttribute("usuarios", lista);
        model.addAttribute("buscar", buscar != null ? buscar : "");
        model.addAttribute("totalUsuarios", usuarioService.contarTotal());
        return "usuarios/lista";
    }

    /**
     * Consulta detallada de un usuario por su ID.
     */
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable("id") Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        model.addAttribute("usuario", usuario);
        return "usuarios/detalle";
    }

    /**
     * Formulario para crear un nuevo usuario.
     */
    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        if (!model.containsAttribute("usuario")) {
            Usuario usuario = new Usuario();
            usuario.setRol("CONSULTA");
            model.addAttribute("usuario", usuario);
        }
        model.addAttribute("roles", usuarioService.obtenerRoles());
        model.addAttribute("esEdicion", false);
        return "usuarios/formulario";
    }

    /**
     * Procesa la creación de un nuevo usuario.
     */
    @PostMapping("/nuevo")
    public String guardarNuevo(@Valid @ModelAttribute("usuario") Usuario usuario,
                               BindingResult bindingResult,
                               @RequestParam(value = "passwordRaw", required = false) String passwordRaw,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", usuarioService.obtenerRoles());
            model.addAttribute("esEdicion", false);
            return "usuarios/formulario";
        }

        try {
            usuarioService.crear(usuario, passwordRaw);
            redirectAttributes.addFlashAttribute("exitoMensaje",
                    "Usuario '" + usuario.getNombre() + "' (" + usuario.getUsername() + ") creado con éxito.");
            return "redirect:/usuarios";
        } catch (DuplicateResourceException | BusinessRuleException e) {
            model.addAttribute("errorMensaje", e.getMessage());
            model.addAttribute("roles", usuarioService.obtenerRoles());
            model.addAttribute("esEdicion", false);
            return "usuarios/formulario";
        }
    }

    /**
     * Formulario para editar un usuario existente.
     */
    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable("id") Long id, Model model) {
        if (!model.containsAttribute("usuario")) {
            Usuario usuario = usuarioService.buscarPorId(id);
            model.addAttribute("usuario", usuario);
        }
        model.addAttribute("roles", usuarioService.obtenerRoles());
        model.addAttribute("esEdicion", true);
        return "usuarios/formulario";
    }

    /**
     * Procesa la actualización de un usuario.
     */
    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("usuario") Usuario usuario,
                             BindingResult bindingResult,
                             @RequestParam(value = "passwordRaw", required = false) String passwordRaw,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", usuarioService.obtenerRoles());
            model.addAttribute("esEdicion", true);
            return "usuarios/formulario";
        }

        try {
            usuarioService.actualizar(id, usuario, passwordRaw);
            redirectAttributes.addFlashAttribute("exitoMensaje",
                    "Usuario '" + usuario.getNombre() + "' actualizado exitosamente.");
            return "redirect:/usuarios";
        } catch (DuplicateResourceException | BusinessRuleException e) {
            model.addAttribute("errorMensaje", e.getMessage());
            model.addAttribute("roles", usuarioService.obtenerRoles());
            model.addAttribute("esEdicion", true);
            return "usuarios/formulario";
        }
    }

    /**
     * Elimina un usuario del sistema, previniendo auto-eliminación o eliminar al último admin.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            String nombre = usuario.getNombre();
            String usernameActual = authentication != null ? authentication.getName() : "";
            usuarioService.eliminar(id, usernameActual);
            redirectAttributes.addFlashAttribute("exitoMensaje",
                    "Usuario '" + nombre + "' eliminado correctamente.");
        } catch (BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("errorMensaje", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMensaje", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
