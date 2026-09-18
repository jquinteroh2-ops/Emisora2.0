package com.emisora;

import com.emisora.entity.Emisora;
import com.emisora.entity.Usuario;
import com.emisora.repository.EmisoraRepository;
import com.emisora.repository.UsuarioRepository;
import com.emisora.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmisoraIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmisoraRepository emisoraRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Test
    @DisplayName("1. La página de login carga correctamente sin autenticación")
    void loginPageLoads() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(content().string(containsString("Emisora 2.0")))
                .andExpect(content().string(containsString("Iniciar Sesión")));
    }

    @Test
    @DisplayName("2. La raíz redirige al login cuando no hay sesión")
    void rootRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("3. Las rutas protegidas redirigen al login si el usuario es anónimo")
    void protectedRoutesRequireAuth() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        mockMvc.perform(get("/emisoras"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "U001", roles = {"ADMIN"})
    @DisplayName("4. El Dashboard carga con métricas para usuario autenticado")
    void dashboardLoadsForAdmin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("totalEmisoras", "totalUsuarios", "totalPaises", "totalGeneros"))
                .andExpect(content().string(containsString("Sistema de Gestión de Emisoras Radiales")));
    }

    @Test
    @WithMockUser(username = "U001", roles = {"ADMIN"})
    @DisplayName("5. CRUD Emisora: Listar catálogo")
    void emisorasListLoads() throws Exception {
        mockMvc.perform(get("/emisoras"))
                .andExpect(status().isOk())
                .andExpect(view().name("emisoras/lista"))
                .andExpect(model().attributeExists("emisoras"))
                .andExpect(content().string(containsString("Caribe Estéreo")));
    }

    @Test
    @WithMockUser(username = "U001", roles = {"ADMIN"})
    @DisplayName("6. CRUD Emisora: Ver detalle")
    void emisoraDetailLoads() throws Exception {
        Emisora em = emisoraRepository.findByCodigo("EM001").orElseThrow();
        mockMvc.perform(get("/emisoras/ver/" + em.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("emisoras/detalle"))
                .andExpect(model().attributeExists("emisora"))
                .andExpect(content().string(containsString("Caribe Estéreo")));
    }

    @Test
    @WithMockUser(username = "U001", roles = {"ADMIN"})
    @DisplayName("7. CRUD Emisora: Registrar nueva emisora exitosamente")
    void createEmisoraSuccess() throws Exception {
        mockMvc.perform(post("/emisoras/nuevo")
                .with(csrf())
                .param("codigo", "EM099")
                .param("nombre", "Radio Test Integración")
                .param("canal", "Cadena de Pruebas")
                .param("bandaFm", "99.1")
                .param("numLocutores", "4")
                .param("genero", "Jazz")
                .param("horario", "24 horas")
                .param("pais", "Colombia")
                .param("numProgramas", "8")
                .param("numCiudades", "5")
                .param("patrocinador", "Patrocinador Demo")
                .param("descripcion", "Emisora de prueba automatizada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emisoras"))
                .andExpect(flash().attributeExists("exitoMensaje"));

        assertTrue(emisoraRepository.existsByCodigo("EM099"));
        assertTrue(emisoraRepository.existsByNombre("Radio Test Integración"));
    }

    @Test
    @WithMockUser(username = "U001", roles = {"ADMIN"})
    @DisplayName("8. CRUD Emisora: Editar emisora existente")
    void updateEmisoraSuccess() throws Exception {
        Emisora em = emisoraRepository.findByCodigo("EM002").orElseThrow();

        mockMvc.perform(post("/emisoras/editar/" + em.getId())
                .with(csrf())
                .param("codigo", em.getCodigo())
                .param("nombre", "Ondas del Magdalena Modificada")
                .param("canal", em.getCanal())
                .param("bandaAm", "1040")
                .param("numLocutores", "10")
                .param("genero", em.getGenero())
                .param("horario", em.getHorario())
                .param("pais", em.getPais())
                .param("numProgramas", "25")
                .param("numCiudades", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emisoras"))
                .andExpect(flash().attributeExists("exitoMensaje"));

        Emisora actualizada = emisoraRepository.findById(em.getId()).orElseThrow();
        assertEquals("Ondas del Magdalena Modificada", actualizada.getNombre());
        assertEquals(10, actualizada.getNumLocutores());
    }

    @Test
    @WithMockUser(username = "U001", roles = {"ADMIN"})
    @DisplayName("9. CRUD Emisora: Eliminar emisora")
    void deleteEmisoraSuccess() throws Exception {
        Emisora nueva = new Emisora("EM088", "Para Eliminar", "Canal Temporal", 102.5, null, 2, "Pop", "12 horas", null, "Chile", "Desc", 4, 2);
        nueva = emisoraRepository.save(nueva);
        Long id = nueva.getId();

        mockMvc.perform(post("/emisoras/eliminar/" + id).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emisoras"))
                .andExpect(flash().attributeExists("exitoMensaje"));

        assertFalse(emisoraRepository.existsById(id));
    }

    @Test
    @WithMockUser(username = "U001", roles = {"ADMIN"})
    @DisplayName("10. CRUD Usuario: Listar usuarios (ADMIN)")
    void usuariosListLoads() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/lista"))
                .andExpect(model().attributeExists("usuarios"))
                .andExpect(content().string(containsString("José Quintero")));
    }

    @Test
    @WithMockUser(username = "U004", roles = {"CONSULTA"})
    @DisplayName("11. Control de Acceso: Usuario CONSULTA recibe 403 al acceder a /usuarios")
    void consultaUserCannotAccessUsuarios() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "U001", roles = {"ADMIN"})
    @DisplayName("12. CRUD Usuario: Crear usuario")
    void createUsuarioSuccess() throws Exception {
        mockMvc.perform(post("/usuarios/nuevo")
                .with(csrf())
                .param("username", "U099")
                .param("nombre", "Usuario Prueba")
                .param("email", "prueba.unit@unicartagena.edu.co")
                .param("rol", "OPERADOR")
                .param("passwordRaw", "ClaveSegura123*"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attributeExists("exitoMensaje"));

        assertTrue(usuarioRepository.existsByUsername("U099"));
    }

    @Test
    @WithMockUser(username = "U001", roles = {"ADMIN"})
    @DisplayName("13. Reporte Emisora 1: País y Género")
    void reportEmisoraPaisGeneroWorks() throws Exception {
        mockMvc.perform(get("/reportes/emisoras/pais-genero")
                .param("pais", "Colombia")
                .param("genero", "Tropical"))
                .andExpect(status().isOk())
                .andExpect(view().name("reportes/emisora-pais-genero"))
                .andExpect(model().attribute("haBuscado", true))
                .andExpect(content().string(containsString("Caribe Estéreo")));
    }

    @Test
    @WithMockUser(username = "U001", roles = {"ADMIN"})
    @DisplayName("14. Reporte Emisora 2: Cobertura")
    void reportEmisoraCoberturaWorks() throws Exception {
        mockMvc.perform(get("/reportes/emisoras/cobertura")
                .param("minCiudades", "5")
                .param("maxCiudades", "40")
                .param("minLocutores", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("reportes/emisora-cobertura"))
                .andExpect(model().attribute("haBuscado", true))
                .andExpect(content().string(containsString("Voz Andina")));
    }

    @Test
    @WithMockUser(username = "U001", roles = {"ADMIN"})
    @DisplayName("15. Reporte Usuario 1: Por Rol")
    void reportUsuarioRolWorks() throws Exception {
        mockMvc.perform(get("/reportes/usuarios/rol")
                .param("rol", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(view().name("reportes/usuario-rol"))
                .andExpect(model().attribute("haBuscado", true))
                .andExpect(content().string(containsString("José Quintero")));
    }

    @Test
    @WithMockUser(username = "U001", roles = {"ADMIN"})
    @DisplayName("16. Reporte Usuario 2: Por Rango de Fechas")
    void reportUsuarioFechasWorks() throws Exception {
        LocalDate hoy = LocalDate.now();
        mockMvc.perform(get("/reportes/usuarios/fechas")
                .param("desde", hoy.minusYears(1).toString())
                .param("hasta", hoy.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("reportes/usuario-fechas"))
                .andExpect(model().attribute("haBuscado", true));
    }

    @Test
    @DisplayName("17. Recuperación de Contraseña: Flujo completo de solicitud y restablecimiento")
    void passwordRecoveryFullFlow() throws Exception {
        // Solicitar recuperación
        mockMvc.perform(post("/recuperar-clave")
                .with(csrf())
                .param("email", "jquinteroh2@unicartagena.edu.co"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("exitoMensaje"));

        Usuario usuario = usuarioRepository.findByEmail("jquinteroh2@unicartagena.edu.co").orElseThrow();
        String token = usuario.getResetToken();
        assertNotNull(token, "El token de recuperación debe haber sido generado y guardado.");

        // Abrir formulario con el token
        mockMvc.perform(get("/restablecer-clave").param("token", token))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/restablecer-clave"))
                .andExpect(model().attribute("token", token));

        // Enviar nueva contraseña
        mockMvc.perform(post("/restablecer-clave")
                .with(csrf())
                .param("token", token)
                .param("nuevaClave", "NuevaClave2026*")
                .param("confirmarClave", "NuevaClave2026*"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("exitoMensaje"));

        Usuario usuarioActualizado = usuarioRepository.findByEmail("jquinteroh2@unicartagena.edu.co").orElseThrow();
        assertNull(usuarioActualizado.getResetToken(), "El token debe limpiarse tras el restablecimiento.");
    }
}
