package com.emisora.config;

import com.emisora.entity.Usuario;
import com.emisora.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

import java.util.List;

/**
 * Configuración de Seguridad con Spring Security.
 * Implementa autenticación por formulario HTML, control de sesiones,
 * autorización basada en roles (ADMIN, OPERADOR, CONSULTA) y protección CSRF.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;

    public SecurityConfig(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Servicio que busca al usuario tanto por su código/username como por su correo electrónico.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return usernameOrEmail -> {
            Usuario usuario = usuarioRepository.findByUsername(usernameOrEmail)
                    .or(() -> usuarioRepository.findByEmail(usernameOrEmail))
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con credencial: " + usernameOrEmail));

            String rolConPrefijo = "ROLE_" + usuario.getRol().toUpperCase();
            return new User(
                    usuario.getUsername(),
                    usuario.getClave(),
                    List.of(new SimpleGrantedAuthority(rolConPrefijo))
            );
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Rutas estáticas y públicas
                .requestMatchers(
                        "/",
                        "/login",
                        "/recuperar-clave/**",
                        "/restablecer-clave/**",
                        "/error",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/h2-console/**"
                ).permitAll()

                // Gestión de usuarios: exclusivo para administradores
                .requestMatchers("/usuarios/**").hasRole("ADMIN")

                // Creación, edición y eliminación de emisoras: ADMIN y OPERADOR
                .requestMatchers(
                        "/emisoras/nuevo",
                        "/emisoras/editar/**",
                        "/emisoras/eliminar/**"
                ).hasAnyRole("ADMIN", "OPERADOR")

                // Consulta de emisoras, dashboard y reportes: todos los roles autenticados
                .requestMatchers(
                        "/dashboard",
                        "/emisoras",
                        "/emisoras/ver/**",
                        "/reportes/**"
                ).hasAnyRole("ADMIN", "OPERADOR", "CONSULTA")

                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // Sin sesión: redirige al login. Sin permiso: Spring Boot muestra templates/error/403.html
            // Permitir consola H2 local en desarrollo
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
            );

        return http.build();
    }
}
