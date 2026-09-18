package com.emisora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de inicio de la aplicación Spring Boot.
 * Sistema de Gestión de Emisoras Radiales - Ejercicio 25.
 * Arquitectura Spring Boot MVC + Thymeleaf + Spring Data JPA.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@SpringBootApplication
public class EmisoraApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmisoraApplication.class, args);
    }
}
