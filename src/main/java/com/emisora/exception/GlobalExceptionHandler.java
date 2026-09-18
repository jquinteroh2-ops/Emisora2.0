package com.emisora.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Manejador global de excepciones para controladores Spring MVC.
 * Evita mostrar trazas de error (stack traces) al usuario final y renderiza
 * vistas de error amigables.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public String manejarRecursoNoEncontrado(ResourceNotFoundException ex, Model model) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        model.addAttribute("tituloError", "Recurso No Encontrado");
        model.addAttribute("mensajeError", ex.getMessage());
        model.addAttribute("codigoEstado", 404);
        return "error/404";
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public String manejarRecursoDuplicado(DuplicateResourceException ex, Model model) {
        log.warn("Conflicto de recurso duplicado: {}", ex.getMessage());
        model.addAttribute("tituloError", "Dato Duplicado");
        model.addAttribute("mensajeError", ex.getMessage());
        model.addAttribute("codigoEstado", 409);
        return "error/500";
    }

    @ExceptionHandler(BusinessRuleException.class)
    public String manejarReglaDeNegocio(BusinessRuleException ex, Model model) {
        log.warn("Violación de regla de negocio: {}", ex.getMessage());
        model.addAttribute("tituloError", "Operación No Permitida");
        model.addAttribute("mensajeError", ex.getMessage());
        model.addAttribute("codigoEstado", 400);
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    public String manejarExcepcionGeneral(Exception ex, Model model) {
        log.error("Error inesperado en la aplicación", ex);
        model.addAttribute("tituloError", "Error Inesperado");
        model.addAttribute("mensajeError", "Ha ocurrido un inconveniente interno en el servidor. Intente nuevamente más tarde.");
        model.addAttribute("codigoEstado", 500);
        return "error/500";
    }
}
