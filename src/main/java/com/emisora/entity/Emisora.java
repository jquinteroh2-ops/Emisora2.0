package com.emisora.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * Entidad Emisora (Ejercicio 25).
 * Mapeada con JPA hacia la tabla 'emisoras'.
 * Incluye validaciones Bean Validation para formularios web con Thymeleaf.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Entity
@Table(name = "emisoras")
public class Emisora implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código es obligatorio.")
    @Size(max = 20, message = "El código no puede tener más de 20 caracteres.")
    @Column(name = "codigo", unique = true, nullable = false, length = 20)
    private String codigo;

    @NotBlank(message = "El nombre de la emisora es obligatorio.")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres.")
    @Column(name = "nombre", unique = true, nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El canal o cadena radial es obligatorio.")
    @Size(max = 100, message = "El canal no puede tener más de 100 caracteres.")
    @Column(name = "canal", nullable = false, length = 100)
    private String canal;

    @Min(value = 87, message = "La frecuencia FM mínima es 87.5 MHz.")
    @Max(value = 108, message = "La frecuencia FM máxima es 108.0 MHz.")
    @Column(name = "banda_fm")
    private Double bandaFm;

    @Min(value = 530, message = "La frecuencia AM mínima es 530 kHz.")
    @Max(value = 1710, message = "La frecuencia AM máxima es 1710 kHz.")
    @Column(name = "banda_am")
    private Integer bandaAm;

    @NotNull(message = "El número de locutores es obligatorio.")
    @Min(value = 0, message = "El número de locutores no puede ser negativo.")
    @Column(name = "num_locutores", nullable = false)
    private Integer numLocutores = 0;

    @NotBlank(message = "El género de la emisora es obligatorio.")
    @Size(max = 50, message = "El género no puede tener más de 50 caracteres.")
    @Column(name = "genero", nullable = false, length = 50)
    private String genero;

    @NotBlank(message = "El horario es obligatorio.")
    @Size(max = 100, message = "El horario no puede tener más de 100 caracteres.")
    @Column(name = "horario", nullable = false, length = 100)
    private String horario;

    @Size(max = 100, message = "El patrocinador no puede tener más de 100 caracteres.")
    @Column(name = "patrocinador", length = 100)
    private String patrocinador;

    @NotBlank(message = "El país es obligatorio.")
    @Size(max = 60, message = "El país no puede tener más de 60 caracteres.")
    @Column(name = "pais", nullable = false, length = 60)
    private String pais;

    @Size(max = 500, message = "La descripción no puede tener más de 500 caracteres.")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull(message = "El número de programas es obligatorio.")
    @Min(value = 0, message = "El número de programas no puede ser negativo.")
    @Column(name = "num_programas", nullable = false)
    private Integer numProgramas = 0;

    @NotNull(message = "El número de ciudades es obligatorio.")
    @Min(value = 0, message = "El número de ciudades no puede ser negativo.")
    @Column(name = "num_ciudades", nullable = false)
    private Integer numCiudades = 0;

    public Emisora() {
    }

    public Emisora(String codigo, String nombre, String canal, Double bandaFm, Integer bandaAm,
                   Integer numLocutores, String genero, String horario, String patrocinador,
                   String pais, String descripcion, Integer numProgramas, Integer numCiudades) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.canal = canal;
        this.bandaFm = bandaFm;
        this.bandaAm = bandaAm;
        this.numLocutores = numLocutores;
        this.genero = genero;
        this.horario = horario;
        this.patrocinador = patrocinador;
        this.pais = pais;
        this.descripcion = descripcion;
        this.numProgramas = numProgramas;
        this.numCiudades = numCiudades;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo != null ? codigo.trim().toUpperCase() : null;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : null;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal != null ? canal.trim() : null;
    }

    public Double getBandaFm() {
        return bandaFm;
    }

    public void setBandaFm(Double bandaFm) {
        this.bandaFm = bandaFm;
    }

    public Integer getBandaAm() {
        return bandaAm;
    }

    public void setBandaAm(Integer bandaAm) {
        this.bandaAm = bandaAm;
    }

    public Integer getNumLocutores() {
        return numLocutores;
    }

    public void setNumLocutores(Integer numLocutores) {
        this.numLocutores = numLocutores;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero != null ? genero.trim() : null;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario != null ? horario.trim() : null;
    }

    public String getPatrocinador() {
        return patrocinador;
    }

    public void setPatrocinador(String patrocinador) {
        this.patrocinador = patrocinador != null ? patrocinador.trim() : null;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais != null ? pais.trim() : null;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion != null ? descripcion.trim() : null;
    }

    public Integer getNumProgramas() {
        return numProgramas;
    }

    public void setNumProgramas(Integer numProgramas) {
        this.numProgramas = numProgramas;
    }

    public Integer getNumCiudades() {
        return numCiudades;
    }

    public void setNumCiudades(Integer numCiudades) {
        this.numCiudades = numCiudades;
    }

    /**
     * Muestra la frecuencia formateada en texto legible para la vista.
     */
    public String getFrecuenciaDisplay() {
        StringBuilder sb = new StringBuilder();
        if (bandaFm != null) {
            sb.append(String.format("%.1f FM", bandaFm));
        }
        if (bandaAm != null) {
            if (sb.length() > 0) {
                sb.append(" / ");
            }
            sb.append(bandaAm).append(" AM");
        }
        return sb.length() > 0 ? sb.toString() : "Digital / En línea";
    }
}
