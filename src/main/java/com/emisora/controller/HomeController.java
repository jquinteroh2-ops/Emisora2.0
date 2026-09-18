package com.emisora.controller;

import com.emisora.service.EmisoraService;
import com.emisora.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para la página principal y el panel de control (Dashboard).
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Controller
public class HomeController {

    private final EmisoraService emisoraService;
    private final UsuarioService usuarioService;

    public HomeController(EmisoraService emisoraService, UsuarioService usuarioService) {
        this.emisoraService = emisoraService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/")
    public String inicio(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("totalEmisoras", emisoraService.contarTotal());
        model.addAttribute("totalUsuarios", usuarioService.contarTotal());
        model.addAttribute("totalPaises", emisoraService.obtenerPaises().size());
        model.addAttribute("totalGeneros", emisoraService.obtenerGeneros().size());
        model.addAttribute("emisorasRecientes", emisoraService.listarTodas());
        model.addAttribute("usuarioActual", authentication != null ? authentication.getName() : "");
        return "dashboard";
    }
}
