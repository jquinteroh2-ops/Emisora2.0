package com.emisora.controller;

import com.emisora.entity.Emisora;
import com.emisora.entity.Usuario;
import com.emisora.exception.BusinessRuleException;
import com.emisora.service.EmisoraService;
import com.emisora.service.UsuarioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Controlador Spring MVC para la generación de Reportes Parametrizados.
 * Cumple con el requisito de mínimo 2 reportes para Emisora y 2 reportes para Usuario:
 * 1. Emisoras por País y Género.
 * 2. Emisoras por Cobertura (rango de ciudades y mínimo de locutores).
 * 3. Usuarios por Rol.
 * 4. Usuarios por Rango de Fechas de Registro.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final EmisoraService emisoraService;
    private final UsuarioService usuarioService;

    public ReporteController(EmisoraService emisoraService, UsuarioService usuarioService) {
        this.emisoraService = emisoraService;
        this.usuarioService = usuarioService;
    }

    /**
     * Índice general de reportes con accesos rápidos.
     */
    @GetMapping
    public String index(Model model) {
        model.addAttribute("totalEmisoras", emisoraService.contarTotal());
        model.addAttribute("totalUsuarios", usuarioService.contarTotal());
        model.addAttribute("paisesDisponibles", emisoraService.obtenerPaises().size());
        model.addAttribute("generosDisponibles", emisoraService.obtenerGeneros().size());
        return "reportes/index";
    }

    /**
     * REPORTE 1: Emisoras por País y Género.
     */
    @GetMapping("/emisoras/pais-genero")
    public String reporteEmisoraPaisGenero(@RequestParam(value = "pais", required = false) String pais,
                                          @RequestParam(value = "genero", required = false) String genero,
                                          Model model) {
        model.addAttribute("paises", emisoraService.obtenerPaises());
        model.addAttribute("generos", emisoraService.obtenerGeneros());
        model.addAttribute("paisSeleccionado", pais);
        model.addAttribute("generoSeleccionado", genero);

        if (pais != null && !pais.isBlank()) {
            try {
                List<Emisora> resultados = emisoraService.reportePorPaisYGenero(pais, genero);
                model.addAttribute("resultados", resultados);
                model.addAttribute("haBuscado", true);
            } catch (BusinessRuleException e) {
                model.addAttribute("errorMensaje", e.getMessage());
                model.addAttribute("resultados", Collections.emptyList());
                model.addAttribute("haBuscado", false);
            }
        } else {
            model.addAttribute("haBuscado", false);
        }

        return "reportes/emisora-pais-genero";
    }

    /**
     * REPORTE 2: Emisoras por Cobertura (ciudades y locutores).
     */
    @GetMapping("/emisoras/cobertura")
    public String reporteEmisoraCobertura(@RequestParam(value = "minCiudades", required = false) Integer minCiudades,
                                         @RequestParam(value = "maxCiudades", required = false) Integer maxCiudades,
                                         @RequestParam(value = "minLocutores", required = false) Integer minLocutores,
                                         Model model) {
        model.addAttribute("minCiudades", minCiudades != null ? minCiudades : 1);
        model.addAttribute("maxCiudades", maxCiudades != null ? maxCiudades : 50);
        model.addAttribute("minLocutores", minLocutores != null ? minLocutores : 0);

        if (minCiudades != null && maxCiudades != null && minLocutores != null) {
            try {
                List<Emisora> resultados = emisoraService.reportePorCobertura(minCiudades, maxCiudades, minLocutores);
                model.addAttribute("resultados", resultados);
                model.addAttribute("haBuscado", true);
            } catch (BusinessRuleException e) {
                model.addAttribute("errorMensaje", e.getMessage());
                model.addAttribute("resultados", Collections.emptyList());
                model.addAttribute("haBuscado", false);
            }
        } else {
            model.addAttribute("haBuscado", false);
        }

        return "reportes/emisora-cobertura";
    }

    /**
     * REPORTE 3: Usuarios por Rol.
     */
    @GetMapping("/usuarios/rol")
    public String reporteUsuarioRol(@RequestParam(value = "rol", required = false) String rol,
                                    Model model) {
        model.addAttribute("roles", usuarioService.obtenerRoles());
        model.addAttribute("rolSeleccionado", rol);

        if (rol != null && !rol.isBlank()) {
            try {
                List<Usuario> resultados = usuarioService.reportePorRol(rol);
                model.addAttribute("resultados", resultados);
                model.addAttribute("haBuscado", true);
            } catch (BusinessRuleException e) {
                model.addAttribute("errorMensaje", e.getMessage());
                model.addAttribute("resultados", Collections.emptyList());
                model.addAttribute("haBuscado", false);
            }
        } else {
            model.addAttribute("haBuscado", false);
        }

        return "reportes/usuario-rol";
    }

    /**
     * REPORTE 4: Usuarios por Rango de Fechas.
     */
    @GetMapping("/usuarios/fechas")
    public String reporteUsuarioFechas(@RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                       @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                       Model model) {
        LocalDate hoy = LocalDate.now();
        model.addAttribute("desde", desde != null ? desde : hoy.minusMonths(1));
        model.addAttribute("hasta", hasta != null ? hasta : hoy);

        if (desde != null && hasta != null) {
            try {
                List<Usuario> resultados = usuarioService.reportePorRangoFechas(desde, hasta);
                model.addAttribute("resultados", resultados);
                model.addAttribute("haBuscado", true);
            } catch (BusinessRuleException e) {
                model.addAttribute("errorMensaje", e.getMessage());
                model.addAttribute("resultados", Collections.emptyList());
                model.addAttribute("haBuscado", false);
            }
        } else {
            model.addAttribute("haBuscado", false);
        }

        return "reportes/usuario-fechas";
    }
}
