package com.emisora.repository;

import com.emisora.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Usuario.
 * Proporciona métodos de persistencia, autenticación y reportes parametrizados.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByResetToken(String resetToken);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Búsqueda general por término en username, nombre o email.
     */
    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "ORDER BY u.nombre ASC")
    List<Usuario> buscarPorTermino(@Param("term") String term);

    /**
     * REPORTE PARAMETRIZADO 1 DE USUARIO:
     * Listar usuarios filtrados por su rol en el sistema.
     */
    List<Usuario> findByRolOrderByNombreAsc(String rol);

    /**
     * REPORTE PARAMETRIZADO 2 DE USUARIO:
     * Listar usuarios registrados dentro de un rango de fechas.
     */
    @Query("SELECT u FROM Usuario u WHERE u.createdAt >= :desde AND u.createdAt <= :hasta ORDER BY u.createdAt DESC")
    List<Usuario> reportePorRangoFechas(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
