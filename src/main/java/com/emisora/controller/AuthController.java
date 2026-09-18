package com.emisora.controller;

import com.emisora.entity.Usuario;
import com.emisora.exception.BusinessRuleException;
import com.emisora.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para autenticación, inicio de sesión y recuperación de contraseña por correo.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("errorMensaje", "Credenciales incorrectas. Verifique su usuario o correo y contraseña.");
        }
        if (logout != null) {
            model.addAttribute("exitoMensaje", "Ha cerrado sesión de forma segura.");
        }
        return "auth/login";
    }

    @GetMapping("/recuperar-clave")
    public String formularioRecuperarClave() {
        return "auth/recuperar-clave";
    }

    @PostMapping("/recuperar-clave")
    public String procesarRecuperarClave(@RequestParam("email") String email,
                                         RedirectAttributes redirectAttributes) {
        try {
            usuarioService.solicitarRecuperacion(email);
            redirectAttributes.addFlashAttribute("exitoMensaje",
                    "Si el correo está registrado en el sistema, hemos enviado un enlace para restablecer la contraseña. " +
                    "Revise su bandeja de entrada o la carpeta de spam.");
        } catch (BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("errorMensaje", e.getMessage());
            return "redirect:/recuperar-clave";
        }
        return "redirect:/login";
    }

    @GetMapping("/restablecer-clave")
    public String formularioRestablecerClave(@RequestParam(value = "token", required = false) String token,
                                            Model model,
                                            RedirectAttributes redirectAttributes) {
        if (token == null || token.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMensaje", "Token de recuperación no suministrado.");
            return "redirect:/login";
        }

        try {
            Usuario usuario = usuarioService.validarTokenRecuperacion(token);
            model.addAttribute("token", token);
            model.addAttribute("usuarioNombre", usuario.getNombre());
            return "auth/restablecer-clave";
        } catch (BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("errorMensaje", e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/restablecer-clave")
    public String procesarRestablecerClave(@RequestParam("token") String token,
                                           @RequestParam("nuevaClave") String nuevaClave,
                                           @RequestParam("confirmarClave") String confirmarClave,
                                           RedirectAttributes redirectAttributes) {
        try {
            usuarioService.restablecerClave(token, nuevaClave, confirmarClave);
            redirectAttributes.addFlashAttribute("exitoMensaje",
                    "Su contraseña ha sido actualizada con éxito. Ahora puede iniciar sesión con su nueva clave.");
            return "redirect:/login";
        } catch (BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("errorMensaje", e.getMessage());
            return "redirect:/restablecer-clave?token=" + token;
        }
    }
}
