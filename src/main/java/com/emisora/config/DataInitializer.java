package com.emisora.config;

import com.emisora.entity.Emisora;
import com.emisora.entity.Usuario;
import com.emisora.repository.EmisoraRepository;
import com.emisora.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Inicializador automático de datos para arranque local y despliegue.
 * Si la base de datos está vacía, inserta los usuarios de prueba con sus contraseñas
 * cifradas mediante BCrypt y las 14 emisoras de referencia del ejercicio 25.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final EmisoraRepository emisoraRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           EmisoraRepository emisoraRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.emisoraRepository = emisoraRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        inicializarUsuarios();
        inicializarEmisoras();
    }

    private void inicializarUsuarios() {
        if (usuarioRepository.count() == 0) {
            log.info("Cargando usuarios iniciales de prueba...");
            String defaultPassword = passwordEncoder.encode("Admin2026*");

            List<Usuario> usuarios = List.of(
                    new Usuario("U001", defaultPassword, "José Quintero", "jquinteroh2@unicartagena.edu.co", "ADMIN"),
                    new Usuario("U002", defaultPassword, "Laura Martínez", "operador.emisora@yopmail.com", "OPERADOR"),
                    new Usuario("U003", defaultPassword, "Carlos Pérez", "carlos.emisora@yopmail.com", "OPERADOR"),
                    new Usuario("U004", defaultPassword, "Ana Gómez", "consulta.emisora@yopmail.com", "CONSULTA"),
                    new Usuario("U005", defaultPassword, "Pedro Ramírez", "pedro.emisora@yopmail.com", "CONSULTA")
            );

            // Mismas fechas de registro que db/02_data.sql, para que el reporte por fechas coincida
            List<LocalDateTime> fechasRegistro = List.of(
                    LocalDateTime.of(2026, 8, 1, 8, 0),
                    LocalDateTime.of(2026, 8, 10, 9, 30),
                    LocalDateTime.of(2026, 8, 25, 14, 15),
                    LocalDateTime.of(2026, 9, 2, 11, 0),
                    LocalDateTime.of(2026, 9, 10, 16, 45)
            );
            for (int i = 0; i < usuarios.size(); i++) {
                usuarios.get(i).setCreatedAt(fechasRegistro.get(i));
            }

            usuarioRepository.saveAll(usuarios);
            log.info("Se crearon {} usuarios iniciales.", usuarios.size());
        }
    }

    private void inicializarEmisoras() {
        if (emisoraRepository.count() == 0) {
            log.info("Cargando emisoras iniciales de prueba (Ejercicio 25)...");

            List<Emisora> emisoras = List.of(
                    new Emisora("EM001", "Caribe Estéreo", "Cadena Costa Norte", 98.5, null, 12, "Tropical", "24 horas", "Cooperativa Pesquera del Caribe", "Colombia", "Música tropical, vallenato y champeta para la región Caribe.", 18, 9),
                    new Emisora("EM002", "Ondas del Magdalena", "Red Radial Ribereña", null, 1040, 6, "Noticias", "5:00 a.m. - 10:00 p.m.", "Almacenes El Puerto", "Colombia", "Noticias regionales, entrevistas y servicio social para los municipios ribereños.", 22, 14),
                    new Emisora("EM003", "Rumba Urbana", "Cadena Costa Norte", 104.3, null, 8, "Urbano", "24 horas", "Bebidas Tropicolor", "Colombia", "Reguetón, trap y música urbana para público joven.", 15, 6),
                    new Emisora("EM004", "Voz Andina", "Sistema Andino de Radio", 91.7, 690, 20, "Noticias", "24 horas", "Banco Cordillera", "Colombia", "Cadena informativa con corresponsales en las principales ciudades del país.", 40, 32),
                    new Emisora("EM005", "Radio Cultura Universitaria", "Red de Emisoras Públicas", 107.1, null, 5, "Cultural", "6:00 a.m. - 12:00 a.m.", null, "Colombia", "Emisora cultural y educativa con música clásica, jazz y divulgación científica.", 30, 2),
                    new Emisora("EM006", "Salsa y Sabor", "Onda Latina Medios", 95.9, null, 7, "Salsa", "24 horas", "Café Montebello", "Colombia", "Salsa clásica y salsa romántica con programas de dedicatorias.", 12, 4),
                    new Emisora("EM007", "Mariachi Estéreo", "Grupo Radial Azteca Sur", 89.3, null, 10, "Regional mexicano", "24 horas", "Tortillería La Esperanza", "México", "Rancheras, mariachi y corridos.", 16, 11),
                    new Emisora("EM008", "Noticias Metrópoli", "Grupo Radial Azteca Sur", null, 1220, 25, "Noticias", "24 horas", "Seguros del Valle", "México", "Noticias nacionales, tráfico y análisis político en vivo.", 45, 38),
                    new Emisora("EM009", "Tango Porteño", "Radiodifusora del Plata", 92.1, 870, 9, "Tango", "8:00 a.m. - 2:00 a.m.", "Bodega Los Álamos", "Argentina", "Tango, milonga y folclore rioplatense.", 20, 5),
                    new Emisora("EM010", "Rock del Sur", "Radiodifusora del Plata", 101.5, null, 6, "Rock", "24 horas", "Instrumentos Musicales Fénix", "Argentina", "Rock nacional e internacional, entrevistas a bandas emergentes.", 14, 3),
                    new Emisora("EM011", "Onda Deportiva", "Cadena Ibérica de Radio", 99.9, null, 15, "Deportes", "24 horas", "Deportes Olimpo", "España", "Transmisiones de fútbol, baloncesto y tertulias deportivas.", 25, 17),
                    new Emisora("EM012", "Pop Latino FM", "Cadena Ibérica de Radio", 88.4, null, 11, "Pop", "24 horas", "Telefonía Horizonte", "España", "Éxitos del pop en español e inglés.", 19, 12),
                    new Emisora("EM013", "Radio Inti", "Red Peruana de Emisoras", 96.3, 1160, 8, "Folclor", "5:00 a.m. - 11:00 p.m.", "Textiles Alpaca Real", "Perú", "Música andina, huaynos y programas en quechua.", 17, 8),
                    new Emisora("EM014", "Cumbia Andina", "Red Peruana de Emisoras", null, 950, 4, "Cumbia", "6:00 a.m. - 10:00 p.m.", null, "Perú", "Cumbia peruana y chicha para Lima y alrededores.", 9, 3)
            );

            emisoraRepository.saveAll(emisoras);
            log.info("Se crearon {} emisoras iniciales.", emisoras.size());
        }
    }
}
