package com.emisora.repository;

import com.emisora.entity.Emisora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Emisora.
 * Encapsula el acceso a datos y las consultas parametrizadas requeridas.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Repository
public interface EmisoraRepository extends JpaRepository<Emisora, Long> {

    Optional<Emisora> findByCodigo(String codigo);

    Optional<Emisora> findByNombre(String nombre);

    boolean existsByCodigo(String codigo);

    boolean existsByNombre(String nombre);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    /**
     * Búsqueda general por término en los principales campos de texto.
     */
    @Query("SELECT e FROM Emisora e WHERE " +
           "LOWER(e.codigo) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(e.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(e.canal) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(e.genero) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(e.pais) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "ORDER BY e.nombre ASC")
    List<Emisora> buscarPorTermino(@Param("term") String term);

    /**
     * REPORTE PARAMETRIZADO 1 DE EMISORA:
     * Búsqueda por país y género musical/temático.
     */
    @Query("SELECT e FROM Emisora e WHERE " +
           "LOWER(e.pais) = LOWER(:pais) AND " +
           "(:genero IS NULL OR :genero = '' OR LOWER(e.genero) = LOWER(:genero)) " +
           "ORDER BY e.nombre ASC")
    List<Emisora> reportePorPaisYGenero(@Param("pais") String pais, @Param("genero") String genero);

    /**
     * REPORTE PARAMETRIZADO 2 DE EMISORA:
     * Cobertura por rango de ciudades y mínimo de locutores en nómina.
     */
    @Query("SELECT e FROM Emisora e WHERE " +
           "e.numCiudades >= :minCiudades AND " +
           "e.numCiudades <= :maxCiudades AND " +
           "e.numLocutores >= :minLocutores " +
           "ORDER BY e.numCiudades DESC, e.numLocutores DESC")
    List<Emisora> reportePorCobertura(@Param("minCiudades") int minCiudades,
                                      @Param("maxCiudades") int maxCiudades,
                                      @Param("minLocutores") int minLocutores);

    /**
     * Lista de países distintos registrados para poblar selects en la vista.
     */
    @Query("SELECT DISTINCT e.pais FROM Emisora e ORDER BY e.pais ASC")
    List<String> findDistinctPaises();

    /**
     * Lista de géneros distintos registrados para poblar selects en la vista.
     */
    @Query("SELECT DISTINCT e.genero FROM Emisora e ORDER BY e.genero ASC")
    List<String> findDistinctGeneros();
}
