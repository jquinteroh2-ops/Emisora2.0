package com.emisora.controller;

import com.emisora.entity.Emisora;
import com.emisora.exception.BusinessRuleException;
import com.emisora.exception.DuplicateResourceException;
import com.emisora.service.EmisoraService;
import jakarta.validation.Valid;
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
 * Controlador Spring MVC para la gestión de Emisoras (Ejercicio 25).
 * Implementa el CRUD completo: Crear, Listar/Consultar, Editar y Eliminar.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Controller
@RequestMapping("/emisoras")
public class EmisoraController {

    private final EmisoraService emisoraService;

    public EmisoraController(EmisoraService emisoraService) {
        this.emisoraService = emisoraService;
    }

    /**
     * Listado general y búsqueda de emisoras.
     */
    @GetMapping
    public String listar(@RequestParam(value = "buscar", required = false) String buscar, Model model) {
        List<Emisora> lista = (buscar != null && !buscar.isBlank())
                ? emisoraService.buscarPorTermino(buscar)
                : emisoraService.listarTodas();

        model.addAttribute("emisoras", lista);
        model.addAttribute("buscar", buscar != null ? buscar : "");
        model.addAttribute("totalEmisoras", emisoraService.contarTotal());
        return "emisoras/lista";
    }

    /**
     * Consulta detallada de una emisora por su ID.
     */
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable("id") Long id, Model model) {
        Emisora emisora = emisoraService.buscarPorId(id);
        model.addAttribute("emisora", emisora);
        return "emisoras/detalle";
    }

    /**
     * Formulario para registrar una nueva emisora.
     */
    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        if (!model.containsAttribute("emisora")) {
            Emisora emisora = new Emisora();
            emisora.setNumLocutores(0);
            emisora.setNumProgramas(0);
            emisora.setNumCiudades(0);
            emisora.setHorario("24 horas");
            model.addAttribute("emisora", emisora);
        }
        model.addAttribute("esEdicion", false);
        return "emisoras/formulario";
    }

    /**
     * Procesa la creación de una emisora con validaciones Bean Validation y reglas de negocio.
     */
    @PostMapping("/nuevo")
    public String guardarNuevo(@Valid @ModelAttribute("emisora") Emisora emisora,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("esEdicion", false);
            return "emisoras/formulario";
        }

        try {
            emisoraService.guardar(emisora);
            redirectAttributes.addFlashAttribute("exitoMensaje",
                    "Emisora '" + emisora.getNombre() + "' registrada exitosamente.");
            return "redirect:/emisoras";
        } catch (DuplicateResourceException | BusinessRuleException e) {
            model.addAttribute("errorMensaje", e.getMessage());
            model.addAttribute("esEdicion", false);
            return "emisoras/formulario";
        }
    }

    /**
     * Formulario para editar una emisora existente.
     */
    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable("id") Long id, Model model) {
        if (!model.containsAttribute("emisora")) {
            Emisora emisora = emisoraService.buscarPorId(id);
            model.addAttribute("emisora", emisora);
        }
        model.addAttribute("esEdicion", true);
        return "emisoras/formulario";
    }

    /**
     * Procesa la actualización de una emisora.
     */
    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("emisora") Emisora emisora,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("esEdicion", true);
            return "emisoras/formulario";
        }

        try {
            emisoraService.actualizar(id, emisora);
            redirectAttributes.addFlashAttribute("exitoMensaje",
                    "Emisora '" + emisora.getNombre() + "' actualizada exitosamente.");
            return "redirect:/emisoras";
        } catch (DuplicateResourceException | BusinessRuleException e) {
            model.addAttribute("errorMensaje", e.getMessage());
            model.addAttribute("esEdicion", true);
            return "emisoras/formulario";
        }
    }

    /**
     * Elimina una emisora. Solo por POST, para que el formulario viaje con el token CSRF.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Emisora emisora = emisoraService.buscarPorId(id);
            String nombre = emisora.getNombre();
            emisoraService.eliminar(id);
            redirectAttributes.addFlashAttribute("exitoMensaje",
                    "Emisora '" + nombre + "' eliminada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMensaje",
                    "No se pudo eliminar la emisora: " + e.getMessage());
        }
        return "redirect:/emisoras";
    }
}
