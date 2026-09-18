package com.emisora.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad Usuario.
 * Requerida por la actividad: id, clave, nombre y rol.
 * Adicionalmente incluye email y token de recuperación para cumplir con
 * la recuperación de clave por correo requerida en el punto 9.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código o identificador de usuario es obligatorio.")
    @Size(max = 50, message = "El código de usuario no puede exceder 50 caracteres.")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "clave", nullable = false, length = 255)
    private String clave;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres.")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "Debe proporcionar un formato de correo válido.")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres.")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "El rol es obligatorio.")
    @Pattern(regexp = "ADMIN|OPERADOR|CONSULTA", message = "El rol debe ser ADMIN, OPERADOR o CONSULTA.")
    @Column(name = "rol", nullable = false, length = 20)
    private String rol = "CONSULTA";

    @Column(name = "reset_token", length = 100)
    private String resetToken;

    @Column(name = "reset_token_expires")
    private LocalDateTime resetTokenExpires;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Usuario() {
    }

    public Usuario(String username, String clave, String nombre, String email, String rol) {
        this.username = username;
        this.clave = clave;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.rol == null || this.rol.isBlank()) {
            this.rol = "CONSULTA";
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : null;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol != null ? rol.trim().toUpperCase() : null;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpires() {
        return resetTokenExpires;
    }

    public void setResetTokenExpires(LocalDateTime resetTokenExpires) {
        this.resetTokenExpires = resetTokenExpires;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Helper para comprobar si el rol es Administrador.
     */
    public boolean esAdmin() {
        return "ADMIN".equalsIgnoreCase(this.rol);
    }

    /**
     * Helper para comprobar si el rol es Operador.
     */
    public boolean esOperador() {
        return "OPERADOR".equalsIgnoreCase(this.rol);
    }

    /**
     * Helper para comprobar si el rol es Consulta.
     */
    public boolean esConsulta() {
        return "CONSULTA".equalsIgnoreCase(this.rol);
    }
}
