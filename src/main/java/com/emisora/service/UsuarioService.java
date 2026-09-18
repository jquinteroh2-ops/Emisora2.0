package com.emisora.service;

import com.emisora.entity.Usuario;
import com.emisora.exception.BusinessRuleException;
import com.emisora.exception.DuplicateResourceException;
import com.emisora.exception.ResourceNotFoundException;
import com.emisora.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

/**
 * Capa de servicio para la entidad Usuario.
 * Gestiona operaciones CRUD, validaciones, cifrado de contraseñas con BCrypt,
 * generación y validación de tokens de recuperación y reportes parametrizados.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Service
@Transactional
public class UsuarioService {

    public static final List<String> ROLES_PERMITIDOS = List.of("ADMIN", "OPERADOR", "CONSULTA");
    public static final int MIN_PASSWORD_LENGTH = 6;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.reset-token-expiration-minutes:30}")
    private int resetTokenExpirationMinutes;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con código/username: " + username));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con correo: " + email));
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarPorTermino(String termino) {
        if (termino == null || termino.isBlank()) {
            return listarTodos();
        }
        return usuarioRepository.buscarPorTermino(termino.trim());
    }

    public Usuario crear(Usuario usuario, String rawPassword) {
        if (rawPassword == null || rawPassword.trim().length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessRuleException("La contraseña debe tener como mínimo " + MIN_PASSWORD_LENGTH + " caracteres.");
        }

        validarUnicidad(usuario, null);
        validarRol(usuario.getRol());

        usuario.setClave(passwordEncoder.encode(rawPassword.trim()));
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario datosActualizados, String rawPasswordOpcional) {
        Usuario existente = buscarPorId(id);
        validarUnicidad(datosActualizados, id);
        validarRol(datosActualizados.getRol());

        existente.setUsername(datosActualizados.getUsername());
        existente.setNombre(datosActualizados.getNombre());
        existente.setEmail(datosActualizados.getEmail());
        existente.setRol(datosActualizados.getRol());

        // Si se proporcionó una nueva contraseña, actualizarla
        if (rawPasswordOpcional != null && !rawPasswordOpcional.isBlank()) {
            if (rawPasswordOpcional.trim().length() < MIN_PASSWORD_LENGTH) {
                throw new BusinessRuleException("La nueva contraseña debe tener como mínimo " + MIN_PASSWORD_LENGTH + " caracteres.");
            }
            existente.setClave(passwordEncoder.encode(rawPasswordOpcional.trim()));
        }

        return usuarioRepository.save(existente);
    }

    public void eliminar(Long id, String usernameSesionActual) {
        Usuario usuario = buscarPorId(id);

        // Regla: un administrador no puede eliminarse a sí mismo mientras usa su cuenta
        if (usuario.getUsername().equalsIgnoreCase(usernameSesionActual)) {
            throw new BusinessRuleException("No puede eliminar su propio usuario mientras tiene la sesión activa.");
        }

        // Si es ADMIN, verificar que quede al menos otro administrador
        if (usuario.esAdmin()) {
            long totalAdmins = usuarioRepository.findAll().stream().filter(Usuario::esAdmin).count();
            if (totalAdmins <= 1) {
                throw new BusinessRuleException("No es posible eliminar al único administrador del sistema.");
            }
        }

        usuarioRepository.delete(usuario);
    }

    @Transactional(readOnly = true)
    public long contarTotal() {
        return usuarioRepository.count();
    }

    // ---------------------------------------------------------------------
    // RECUPERACIÓN DE CONTRASEÑA POR CORREO
    // ---------------------------------------------------------------------

    public void solicitarRecuperacion(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessRuleException("Debe ingresar un correo electrónico.");
        }

        // Si el correo no existe no se informa nada distinto, para no revelar qué cuentas existen
        usuarioRepository.findByEmail(email.trim().toLowerCase()).ifPresent(usuario -> {
            String token = generarToken();
            // En la base de datos solo se guarda el hash SHA-256: quien lea la tabla no puede usar el enlace
            usuario.setResetToken(hashToken(token));
            usuario.setResetTokenExpires(LocalDateTime.now().plusMinutes(resetTokenExpirationMinutes));
            usuarioRepository.save(usuario);

            String enlace = baseUrl.replaceAll("/+$", "") + "/restablecer-clave?token=" + token;
            emailService.enviarRecuperacionContrasena(usuario.getEmail(), usuario.getNombre(), enlace);
        });
    }

    @Transactional(readOnly = true)
    public Usuario validarTokenRecuperacion(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessRuleException("El enlace o token de recuperación no es válido.");
        }

        Usuario usuario = usuarioRepository.findByResetToken(hashToken(token.trim()))
                .orElseThrow(() -> new BusinessRuleException("El enlace de recuperación es inválido o ya ha sido utilizado."));

        if (usuario.getResetTokenExpires() == null || usuario.getResetTokenExpires().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("El enlace de recuperación ha vencido. Por favor solicite uno nuevo.");
        }

        return usuario;
    }

    public void restablecerClave(String token, String nuevaClave, String confirmacionClave) {
        if (nuevaClave == null || nuevaClave.trim().length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessRuleException("La nueva clave debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres.");
        }

        if (!nuevaClave.equals(confirmacionClave)) {
            throw new BusinessRuleException("La confirmación de la contraseña no coincide.");
        }

        Usuario usuario = validarTokenRecuperacion(token);
        usuario.setClave(passwordEncoder.encode(nuevaClave.trim()));
        usuario.setResetToken(null);
        usuario.setResetTokenExpires(null);
        usuarioRepository.save(usuario);
    }

    /**
     * Token aleatorio de 256 bits en Base64 URL (sirve directo en el enlace del correo).
     */
    private static String generarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Hash SHA-256 en hexadecimal (64 caracteres) del token de recuperación.
     */
    static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible en la JVM", e);
        }
    }

    // ---------------------------------------------------------------------
    // REPORTES PARAMETRIZADOS
    // ---------------------------------------------------------------------

    /**
     * REPORTE 1: Usuarios por rol.
     */
    @Transactional(readOnly = true)
    public List<Usuario> reportePorRol(String rol) {
        if (rol == null || rol.isBlank() || !ROLES_PERMITIDOS.contains(rol.trim().toUpperCase())) {
            throw new BusinessRuleException("Debe seleccionar un rol válido (" + String.join(", ", ROLES_PERMITIDOS) + ").");
        }
        return usuarioRepository.findByRolOrderByNombreAsc(rol.trim().toUpperCase());
    }

    /**
     * REPORTE 2: Usuarios por rango de fechas de creación.
     */
    @Transactional(readOnly = true)
    public List<Usuario> reportePorRangoFechas(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new BusinessRuleException("Debe ingresar tanto la fecha inicial como la fecha final.");
        }
        if (desde.isAfter(hasta)) {
            throw new BusinessRuleException("La fecha inicial no puede ser posterior a la fecha final.");
        }

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay().minusNanos(1000);
        return usuarioRepository.reportePorRangoFechas(inicio, fin);
    }

    public List<String> obtenerRoles() {
        return ROLES_PERMITIDOS;
    }

    private void validarUnicidad(Usuario usuario, Long idExistente) {
        if (idExistente == null) {
            if (usuarioRepository.existsByUsername(usuario.getUsername())) {
                throw new DuplicateResourceException("El código/usuario '" + usuario.getUsername() + "' ya está registrado.");
            }
            if (usuarioRepository.existsByEmail(usuario.getEmail())) {
                throw new DuplicateResourceException("El correo '" + usuario.getEmail() + "' ya está registrado.");
            }
        } else {
            if (usuarioRepository.existsByUsernameAndIdNot(usuario.getUsername(), idExistente)) {
                throw new DuplicateResourceException("El código/usuario '" + usuario.getUsername() + "' ya está en uso.");
            }
            if (usuarioRepository.existsByEmailAndIdNot(usuario.getEmail(), idExistente)) {
                throw new DuplicateResourceException("El correo '" + usuario.getEmail() + "' ya está en uso.");
            }
        }
    }

    private void validarRol(String rol) {
        if (rol == null || !ROLES_PERMITIDOS.contains(rol.trim().toUpperCase())) {
            throw new BusinessRuleException("El rol debe ser uno de los siguientes: " + String.join(", ", ROLES_PERMITIDOS));
        }
    }
}
