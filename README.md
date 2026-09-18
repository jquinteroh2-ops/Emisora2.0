# Emisora 2.0 &mdash; Sistema de Gestión de Emisoras Radiales

**Desarrollo Web &bull; Unidad 2** &mdash; Actividad: *Spring Boot MVC con Thymeleaf: desarrollo web basado en framework*  
**Estudiante:** Jose Antonio Quintero Herrera &bull; **Código:** 7502510055  
**Asignatura:** Desarrollo Web (cuarto semestre) &bull; **Institución:** Universidad de Cartagena  
**Ejercicio asignado:** N.º 25 &mdash; **Emisora**

**Aplicación desplegada:** _(se completa al publicar en Render)_

---

## 1. Descripción del proyecto

**Emisora 2.0** es una aplicación web construida con **Spring Boot** bajo el patrón **Modelo-Vista-Controlador (MVC)**
tradicional: los controladores reciben las peticiones del navegador, preparan los datos en el `Model` y retornan el
nombre de una plantilla **Thymeleaf**, que Spring convierte en HTML del lado del servidor.

A diferencia de una API RESTful (que devuelve JSON a un frontend separado como React, Angular o Vue), aquí el
servidor genera las páginas completas. No hay controladores `@RestController` ni frontend desacoplado.

La aplicación ofrece, para las entidades **Emisora** (ejercicio 25) y **Usuario**:

- CRUD completo (crear, listar/consultar, editar y eliminar) con formularios Thymeleaf y validaciones.
- Inicio de sesión, sesión HTTP y control de acceso por roles (`ADMIN`, `OPERADOR`, `CONSULTA`).
- Cuatro reportes parametrizados (dos por entidad).
- Recuperación de contraseña por correo electrónico con enlace temporal de un solo uso.

---

## 2. Requisitos y tecnologías

| Requisito | Versión |
|---|---|
| Java (JDK) | **21** (probado con Eclipse Temurin 21.0.11) |
| Apache Maven | 3.9 o superior |
| Base de datos | H2 embebida (local, sin instalar nada) · MySQL 8+/9.x · TiDB Cloud Starter (nube) |
| Docker | Opcional, solo para construir la imagen del despliegue |

- **Spring Boot 3.3.4**: Spring MVC (`@Controller`, `Model`, binding de formularios, redirecciones y *flash attributes*).
- **Thymeleaf 3** + *extras Spring Security 6* (`th:object`, `th:field`, `th:each`, `th:if`, `sec:authorize`).
- **Spring Data JPA / Hibernate 6.5**: repositorios `JpaRepository`, métodos derivados y consultas `@Query` (JPQL).
- **Spring Security 6**: login por formulario, sesión, CSRF, contraseñas cifradas con **BCrypt**.
- **Jakarta Bean Validation**: validaciones en las entidades con mensajes en español.
- **Correo**: API HTTPS de **Brevo** (plan gratuito) o SMTP con `JavaMailSender`.
- **Interfaz**: HTML5, CSS3, Bootstrap 5.3 y Bootstrap Icons.
- **Despliegue**: Docker (multi-etapa) en **Render** + base de datos **TiDB Cloud Starter** (compatible con MySQL).

---

## 3. Arquitectura y flujo MVC

La organización sigue la arquitectura Spring Web MVC por capas vista en tutorías:
**Controller → Service → Repository → Entity**, más las plantillas Thymeleaf.

```
src/main/java/com/emisora/
├── EmisoraApplication.java           # Punto de entrada (@SpringBootApplication)
├── config/
│   ├── SecurityConfig.java           # Login, sesión, reglas de acceso por rol, BCrypt
│   └── DataInitializer.java          # Datos de prueba si la base está vacía (modo H2)
├── controller/                       # @Controller: reciben la petición y retornan una vista
│   ├── HomeController.java           # Panel principal (dashboard)
│   ├── AuthController.java           # Login y recuperación de clave
│   ├── EmisoraController.java        # CRUD de emisoras
│   ├── UsuarioController.java        # CRUD de usuarios (solo ADMIN)
│   └── ReporteController.java        # 4 reportes parametrizados
├── service/                          # @Service: reglas de negocio y transacciones
│   ├── EmisoraService.java           # Validación de frecuencias FM/AM, unicidad, reportes
│   ├── UsuarioService.java           # Cifrado de claves, tokens de recuperación, reportes
│   └── EmailService.java             # Envío del correo de recuperación
├── repository/                       # Spring Data JPA: acceso a datos
│   ├── EmisoraRepository.java
│   └── UsuarioRepository.java
├── entity/                           # @Entity: tablas de la base de datos
│   ├── Emisora.java
│   └── Usuario.java
└── exception/                        # Excepciones de negocio y @ControllerAdvice
src/main/resources/
├── templates/                        # Vistas Thymeleaf (auth, emisoras, usuarios, reportes, error, fragments)
├── static/                           # css/ y js/
├── application.properties            # Configuración común (perfil por defecto: H2)
└── application-mysql.properties      # Perfil mysql (MySQL local o TiDB Cloud)
db/
├── 01_schema.sql                     # Creación de la base emisora2_db y sus tablas
└── 02_data.sql                       # Datos iniciales (5 usuarios y 14 emisoras)
```

### Recorrido de una petición (ejemplo: guardar una emisora)

```
Navegador  ── POST /emisoras/nuevo (formulario th:object="${emisora}") ──►
EmisoraController.guardarNuevo()      @Valid + BindingResult (binding del formulario a la entidad)
      │
      ▼
EmisoraService.guardar()              reglas de negocio (código/nombre únicos, FM 87.5–108.0, AM 530–1710)
      │
      ▼
EmisoraRepository.save()              Spring Data JPA → Hibernate → INSERT en la tabla emisoras
      │
      ▼
Base de datos (H2 / MySQL / TiDB Cloud)
      │
      ▼
Controller: redirect:/emisoras + mensaje flash
      │
      ▼
EmisoraController.listar()            model.addAttribute("emisoras", ...)  →  return "emisoras/lista"
      │
      ▼
Thymeleaf (templates/emisoras/lista.html) genera el HTML  ──►  Navegador
```

**Inyección de dependencias:** Spring crea los componentes (`@Controller`, `@Service`, repositorios, `@Configuration`)
y los conecta por **constructor**; por ejemplo, `EmisoraController` recibe un `EmisoraService`, y este un
`EmisoraRepository`, sin usar `new`.

---

## 4. Entidades

### Emisora (ejercicio 25) &mdash; tabla `emisoras`

| Atributo | Tipo | Regla |
|---|---|---|
| `id` | `Long` | Clave primaria autoincremental |
| `codigo` | `String(20)` | Único, ej. `EM001` |
| `nombre` | `String(100)` | Obligatorio y único |
| `canal` | `String(100)` | Cadena radial, obligatorio |
| `bandaFm` | `Double` | Opcional, **87.5 – 108.0 MHz** |
| `bandaAm` | `Integer` | Opcional, **530 – 1710 kHz** |
| `numLocutores` | `Integer` | ≥ 0 |
| `genero` | `String(50)` | Obligatorio (Noticias, Tropical, Rock...) |
| `horario` | `String(100)` | Obligatorio (ej. "24 horas") |
| `patrocinador` | `String(100)` | Opcional |
| `pais` | `String(60)` | Obligatorio |
| `descripcion` | `String(500)` | Opcional |
| `numProgramas` | `Integer` | ≥ 0 |
| `numCiudades` | `Integer` | ≥ 0 |

Regla de negocio: toda emisora debe tener **al menos una frecuencia** (FM o AM).

### Usuario &mdash; tabla `usuarios`

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | Clave primaria |
| `clave` | `String(255)` | Contraseña cifrada con **BCrypt** (nunca se guarda en texto plano) |
| `nombre` | `String(100)` | Nombre completo |
| `rol` | `String(20)` | `ADMIN`, `OPERADOR` o `CONSULTA` |
| `username` | `String(50)` | Código para iniciar sesión (ej. `U001`) |
| `email` | `String(100)` | Único; destino del correo de recuperación |
| `resetToken` | `String(100)` | Hash SHA-256 del token de recuperación |
| `resetTokenExpires` | `LocalDateTime` | Vencimiento del token (30 minutos) |
| `createdAt` | `LocalDateTime` | Fecha de registro (usada en un reporte) |

---

## 5. Roles y control de acceso

| Rol | Emisoras | Usuarios | Reportes |
|---|---|---|---|
| `ADMIN` | Consultar, crear, editar, eliminar | CRUD completo | Sí |
| `OPERADOR` | Consultar, crear, editar, eliminar | Sin acceso | Sí |
| `CONSULTA` | Solo consultar | Sin acceso | Sí |

- Sin sesión, cualquier página protegida redirige a `/login`. Sin permiso, se muestra la página **403**.
- Se puede iniciar sesión con el código (`U001`) o con el correo.
- Un administrador no puede eliminarse a sí mismo ni eliminar al único `ADMIN`.
- Eliminar solo se permite por `POST` con token CSRF (no hay enlaces GET que borren datos).

---

## 6. Reportes parametrizados

| # | Entidad | Ruta | Parámetros | Consulta en el repositorio |
|---|---|---|---|---|
| 1 | Emisora | `/reportes/emisoras/pais-genero` | `pais` (obligatorio), `genero` (opcional) | `@Query` JPQL `reportePorPaisYGenero` |
| 2 | Emisora | `/reportes/emisoras/cobertura` | `minCiudades`, `maxCiudades`, `minLocutores` | `@Query` JPQL `reportePorCobertura` |
| 3 | Usuario | `/reportes/usuarios/rol` | `rol` | Método derivado `findByRolOrderByNombreAsc` |
| 4 | Usuario | `/reportes/usuarios/fechas` | `desde`, `hasta` (fechas de registro) | `@Query` JPQL `reportePorRangoFechas` |

Ejemplos con los datos iniciales: país *Colombia* + género *Tropical* → *Caribe Estéreo*; cobertura de 10 a 40
ciudades con 15 o más locutores → *Voz Andina*; fechas del 1 al 31 de agosto de 2026 → U001, U002 y U003.

---

## 7. Ejecución local

### Opción A: con H2 (recomendada, no requiere instalar base de datos)

```bash
git clone https://github.com/jquinteroh2-ops/Emisora2.0.git
cd Emisora2.0
mvn spring-boot:run
```

En Windows también se puede hacer doble clic en `run-local.bat`. Abrir **http://localhost:8080/**.
La base H2 vive en memoria: al arrancar, `DataInitializer` carga los mismos usuarios y emisoras de `db/02_data.sql`
(los cambios se pierden al detener la aplicación). Consola H2: `http://localhost:8080/h2-console`
(JDBC URL `jdbc:h2:mem:emisora_db`, usuario `sa`, sin clave).

Usuarios de prueba (clave **`Admin2026*`** para todos, solo en local):

| Código | Correo | Rol |
|---|---|---|
| `U001` | `jquinteroh2@unicartagena.edu.co` | ADMIN |
| `U002` | `operador.emisora@yopmail.com` | OPERADOR |
| `U003` | `carlos.emisora@yopmail.com` | OPERADOR |
| `U004` | `consulta.emisora@yopmail.com` | CONSULTA |
| `U005` | `pedro.emisora@yopmail.com` | CONSULTA |

> En la aplicación desplegada las claves son otras y se entregan al docente en la ficha de entrega.

### Opción B: con MySQL local

1. Crear la base y cargar los datos (crea la base `emisora2_db`):
   ```bat
   mysql -u root -p < db\01_schema.sql
   mysql -u root -p < db\02_data.sql
   ```
2. Ejecutar con el perfil `mysql` (en Windows `cmd`):
   ```bat
   set DB_PASSWORD=clave_de_root
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

### Pruebas automáticas

```bash
mvn test
```

Pruebas de integración con MockMvc sobre H2: login y acceso por rol, CRUD de ambas entidades, los 4 reportes y el
flujo completo de recuperación de clave (token cifrado y de un solo uso).

---

## 8. Variables de entorno

Ninguna clave está escrita en el código ni en el repositorio; todo se configura con variables de entorno.

| Variable | Obligatoria | Ejemplo / valor por defecto | Uso |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | No | vacío = H2 · `mysql` | Elige la base de datos |
| `DB_URL` | Con perfil `mysql` | `jdbc:mysql://localhost:3306/emisora2_db?...` | Dirección JDBC |
| `DB_USERNAME` | Con perfil `mysql` | `root` | Usuario de la base |
| `DB_PASSWORD` | Con perfil `mysql` | vacío | Clave de la base |
| `BREVO_API_KEY` | Para enviar correos | &mdash; | Clave de la API de Brevo |
| `MAIL_FROM` | Para enviar correos | &mdash; | Remitente verificado en Brevo |
| `MAIL_FROM_NAME` | No | `Emisora 2.0` | Nombre del remitente |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | Alternativa a Brevo API | `smtp-relay.brevo.com`, `587` | Envío por SMTP |
| `APP_BASE_URL` | No | `RENDER_EXTERNAL_URL` o `http://localhost:8080` | Base de los enlaces del correo |
| `PORT` | No | `8080` | Puerto HTTP (Render lo asigna solo) |

---

## 9. Recuperación de contraseña por correo

1. En el login, **¿Olvidaste tu contraseña?** (`/recuperar-clave`) y escribir el correo registrado.
2. `UsuarioService` genera un token aleatorio de 256 bits (`SecureRandom`), guarda en la base **solo su hash
   SHA-256** y una fecha de vencimiento de **30 minutos**. La respuesta es la misma exista o no el correo.
3. `EmailService` envía un correo HTML con el enlace `/restablecer-clave?token=...`:
   - con `BREVO_API_KEY`: por la **API HTTPS de Brevo** (el plan gratuito de Render bloquea los puertos SMTP);
   - con `MAIL_USERNAME`: por SMTP;
   - sin ninguna de las dos (desarrollo local): el enlace se escribe en la consola del servidor.
4. Al abrir el enlace se valida el token; la nueva clave se cifra con BCrypt y el token se borra, así que el
   enlace **no se puede usar dos veces**.

Para usar Brevo gratis: crear cuenta en <https://www.brevo.com>, verificar el remitente en
*Senders, Domains & Dedicated IPs* y generar una clave en *SMTP & API → API Keys*.

---

## 10. Despliegue en Internet (Render + TiDB Cloud)

```
Navegador ──HTTPS──► Render (Docker: Spring Boot, Java 21) ──JDBC + TLS──► TiDB Cloud Starter (MySQL)
                               └──HTTPS──► API de Brevo ──► correo de recuperación
```

### Base de datos en TiDB Cloud Starter (gratis)

1. Crear una instancia **Starter** en <https://tidbcloud.com> (región AWS N. Virginia, cerca de Render).
2. En **Connect**, generar la contraseña y anotar host, puerto `4000` y usuario (`xxxxxxxx.root`).
3. Cargar los scripts (TiDB exige conexión cifrada):
   ```bat
   mysql --host=HOST --port=4000 --user=USUARIO --password --ssl-mode=REQUIRED --default-character-set=utf8mb4 < db\01_schema.sql
   mysql --host=HOST --port=4000 --user=USUARIO --password --ssl-mode=REQUIRED --default-character-set=utf8mb4 < db\02_data.sql
   ```
   TiDB no aplica las restricciones `CHECK`; esas reglas también las valida `EmisoraService`.

### Aplicación en Render (gratis)

1. En <https://render.com>: **New → Blueprint** y elegir este repositorio. Render lee `render.yaml`
   (servicio Docker gratuito con el perfil `mysql`).
2. Escribir las variables que pide:

   | Variable | Valor |
   |---|---|
   | `DB_URL` | `jdbc:mysql://HOST:4000/emisora2_db?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3` |
   | `DB_USERNAME` | usuario de TiDB (`xxxxxxxx.root`) |
   | `DB_PASSWORD` | contraseña de TiDB |
   | `BREVO_API_KEY` | clave de la API de Brevo |
   | `MAIL_FROM` | remitente verificado en Brevo |

3. **Apply**. La primera construcción tarda varios minutos. Los enlaces del correo usan la dirección pública que
   Render entrega en `RENDER_EXTERNAL_URL`.
4. Plan gratuito: tras 15 minutos sin visitas el servicio se suspende y la siguiente visita tarda cerca de un minuto.

Para probar la imagen en local: `docker build -t emisora2 .` y
`docker run -p 8080:8080 emisora2` (sin variables usa H2).
