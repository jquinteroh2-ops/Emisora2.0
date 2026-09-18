package com.emisora.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Manejador global de excepciones para controladores Spring MVC.
 * Evita mostrar trazas de error (stack traces) al usuario final y renderiza
 * vistas de error amigables con el código HTTP correspondiente.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String manejarRecursoNoEncontrado(ResourceNotFoundException ex, Model model) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        model.addAttribute("tituloError", "Recurso No Encontrado");
        model.addAttribute("mensajeError", ex.getMessage());
        model.addAttribute("codigoEstado", 404);
        return "error/404";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String manejarRutaInexistente(NoResourceFoundException ex, Model model) {
        model.addAttribute("tituloError", "Página No Encontrada (404)");
        model.addAttribute("mensajeError", "La página solicitada no existe.");
        model.addAttribute("codigoEstado", 404);
        return "error/404";
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public String manejarMetodoNoPermitido(HttpRequestMethodNotSupportedException ex, Model model) {
        model.addAttribute("tituloError", "Operación No Permitida");
        model.addAttribute("mensajeError", "Esta operación solo se puede realizar desde el formulario de la aplicación.");
        model.addAttribute("codigoEstado", 405);
        return "error/500";
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String manejarParametroInvalido(Exception ex, Model model) {
        log.warn("Parámetro inválido en la solicitud: {}", ex.getMessage());
        model.addAttribute("tituloError", "Solicitud Inválida");
        model.addAttribute("mensajeError", "Los datos enviados en la dirección no son válidos.");
        model.addAttribute("codigoEstado", 400);
        return "error/500";
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String manejarRecursoDuplicado(DuplicateResourceException ex, Model model) {
        log.warn("Conflicto de recurso duplicado: {}", ex.getMessage());
        model.addAttribute("tituloError", "Dato Duplicado");
        model.addAttribute("mensajeError", ex.getMessage());
        model.addAttribute("codigoEstado", 409);
        return "error/500";
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String manejarReglaDeNegocio(BusinessRuleException ex, Model model) {
        log.warn("Violación de regla de negocio: {}", ex.getMessage());
        model.addAttribute("tituloError", "Operación No Permitida");
        model.addAttribute("mensajeError", ex.getMessage());
        model.addAttribute("codigoEstado", 400);
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String manejarExcepcionGeneral(Exception ex, Model model) {
        log.error("Error inesperado en la aplicación", ex);
        model.addAttribute("tituloError", "Error Inesperado");
        model.addAttribute("mensajeError", "Ha ocurrido un inconveniente interno en el servidor. Intente nuevamente más tarde.");
        model.addAttribute("codigoEstado", 500);
        return "error/500";
    }
}
