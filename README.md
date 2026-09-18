# Emisora 2.0 &mdash; Sistema de Gestión de Emisoras Radiales

**Desarrollo Web &bull; Unidad 2** &mdash; Actividad Académica: *Spring Boot MVC con Thymeleaf: desarrollo web basado en framework*  
**Estudiante:** Jose Antonio Quintero Herrera  
**Código Estudiantil:** 7502510055  
**Asignatura:** Desarrollo Web (Cuarto Semestre)  
**Institución:** Universidad de Cartagena  
**Ejercicio Asignado:** N.º 25 &mdash; **Emisora**

---

## 1. Descripción del Proyecto

**Emisora 2.0** es una aplicación web empresarial basada en el framework **Spring Boot**, construida estrictamente bajo el patrón arquitectónico tradicional **Modelo-Vista-Controlador (MVC)** con renderizado del lado del servidor mediante el motor de plantillas **Thymeleaf**.

A diferencia de una API RESTful que retorna datos serializados en JSON hacia un frontend desacoplado (como React, Angular o Vue), esta solución genera el código HTML completo directamente en el servidor Java, cumpliendo rigurosamente los requisitos pedagógicos y técnicos de la asignatura.

La aplicación permite la administración completa (CRUD), autenticación con control de acceso por roles, recuperación de contraseñas por correo electrónico y generación de reportes analíticos parametrizados para dos entidades principales:

1. **Emisora** (Ejercicio 25): Atributos de negocio propios de estaciones radiales.
2. **Usuario**: Gestión de cuentas de acceso, credenciales cifradas y roles.

---

## 2. Tecnologías Utilizadas

- **Lenguaje de Programación:** Java 21 LTS (OpenJDK / Eclipse Temurin).
- **Framework Principal:** Spring Boot 3.3.4.
- **Capa Web:** Spring MVC (Servlets, `@Controller`, binding de modelos, redirecciones y flash attributes).
- **Motor de Plantillas:** Thymeleaf 3 con dialecto Spring Security 6 (`th:action`, `th:object`, `th:field`, `th:each`, `th:if`, `th:text`, `sec:authorize`).
- **Capa de Persistencia:** Spring Data JPA con Hibernate ORM 6.5.
- **Bases de Datos Compatibles:**
  - **H2 Database:** Base de datos relacional embebida para ejecución local instantánea sin dependencias externas.
  - **MySQL 8.0+ / 9.x:** Perfil de producción local o servidor dedicado.
  - **PostgreSQL:** Perfil listo para despliegues en la nube (Render, Supabase, Neon, Railway).
- **Seguridad:** Spring Security 6 (Autenticación basada en formularios, BCrypt Password Encoder, control de sesiones y protección CSRF).
- **Servicio de Correo:** Spring Boot Starter Mail (`JavaMailSender`) con fallback automático y registro en consola para entornos de desarrollo.
- **Validaciones:** Jakarta Bean Validation (Hibernate Validator) con mensajes localizados en español.
- **Diseño Visual:** HTML5, CSS3, Bootstrap 5.3 y Bootstrap Icons.
- **Gestor de Construcción:** Apache Maven 3.9.
- **Control de Versiones:** Git y GitHub con historial progresivo de desarrollo.
- **Contenedores y Despliegue:** Docker (construcción multi-etapa) y Render.com (`render.yaml`).

---

## 3. Arquitectura del Sistema y Flujo MVC

El proyecto implementa una separación estricta de responsabilidades por capas:

```
src/main/java/com/emisora/
├── EmisoraApplication.java           # Punto de entrada de Spring Boot
├── config/                           # Configuraciones de la aplicación
│   ├── SecurityConfig.java           # Reglas de seguridad, filtros y roles
│   └── DataInitializer.java          # Carga automática de datos de prueba
├── controller/                       # Controladores Spring MVC
│   ├── HomeController.java           # Dashboard y rutas de inicio
│   ├── AuthController.java           # Login y recuperación de clave
│   ├── EmisoraController.java        # CRUD de Emisoras
│   ├── UsuarioController.java        # CRUD de Usuarios (solo ADMIN)
│   └── ReporteController.java        # 4 reportes parametrizados
├── entity/                           # Entidades de dominio JPA
│   ├── Emisora.java                  # Modelo Emisora (Ejercicio 25)
│   └── Usuario.java                  # Modelo Usuario con roles y tokens
├── repository/                       # Repositorios Spring Data JPA
│   ├── EmisoraRepository.java        # Consultas de persistencia y reportes
│   └── UsuarioRepository.java        # Consultas de autenticación y reportes
├── service/                          # Lógica y reglas de negocio
│   ├── EmisoraService.java           # Servicios de Emisora y validaciones
│   ├── UsuarioService.java           # Servicios de Usuario y tokens
│   └── EmailService.java             # Envío de correos o fallback seguro
└── exception/                        # Manejo global de errores
    ├── ResourceNotFoundException.java
    ├── DuplicateResourceException.java
    ├── BusinessRuleException.java
    └── GlobalExceptionHandler.java   # Captura excepciones y renderiza error 404/500
```

### Flujo Completo de una Petición (MVC Tradicional)

```
[ Navegador Web ]
       │  (1) Solicitud HTTP GET / POST
       ▼
[ Controller (Spring MVC) ]  ── Valida datos / Binding de formularios
       │  (2) Invoca lógica de negocio
       ▼
[ Service (Capa de Negocio) ]  ── Aplica reglas, cifrado, validaciones
       │  (3) Solicita persistencia
       ▼
[ Repository (Spring Data JPA) ]
       │  (4) Consultas SQL / JPQL
       ▼
[ Base de Datos (H2 / MySQL) ]
       │  (5) Retorna datos relacionales
       ▼
[ Repository ➔ Service ➔ Controller ]
       │  (6) Añade entidades y variables al objeto 'Model'
       ▼
[ Thymeleaf (Motor de Plantillas) ]  ── Renderiza HTML combinando plantilla y Model
       │  (7) Genera documento HTML completo del lado del servidor
       ▼
[ Navegador Web ]  ── Muestra la interfaz gráfica final al usuario
```

---

## 4. Entidades y Atributos

### Entidad 1: Emisora (Ejercicio 25)
Representa las emisoras o cadenas radiales con todos los atributos requeridos por la especificación:

| Atributo | Tipo de Dato | Restricción / Descripción |
|---|---|---|
| `id` | `Long` | Clave primaria autoincremental en base de datos |
| `codigo` | `String` (20) | Identificador único alfanumérico (ej: `EM001`) |
| `nombre` | `String` (100) | Nombre comercial único de la emisora |
| `canal` | `String` (100) | Canal o cadena radial a la que pertenece |
| `bandaFm` | `Double` | Frecuencia FM en MHz (rango 87.0 - 108.0) |
| `bandaAm` | `Integer` | Frecuencia AM en kHz (rango 530 - 1710) |
| `numLocutores` | `Integer` | Cantidad de locutores en cabina ($\ge 0$) |
| `genero` | `String` (50) | Género temático o musical (Noticias, Tropical, Rock...) |
| `horario` | `String` (100) | Horario de transmisión (ej: "24 horas") |
| `patrocinador` | `String` (100) | Patrocinador principal (opcional) |
| `pais` | `String` (60) | País de origen de la emisión |
| `descripcion` | `String` (500) | Perfil o reseña descriptiva de la emisora |
| `numProgramas` | `Integer` | Número de programas al aire ($\ge 0$) |
| `numCiudades` | `Integer` | Cantidad de ciudades cubiertas ($\ge 0$) |

### Entidad 2: Usuario
Representa las cuentas de acceso y credenciales del sistema:

| Atributo | Tipo de Dato | Descripción |
|---|---|---|
| `id` | `Long` | Clave primaria |
| `username` | `String` (50) | Código de usuario para login (ej: `U001`) |
| `clave` | `String` (255) | Contraseña cifrada de forma irreversible con **BCrypt** |
| `nombre` | `String` (100) | Nombre completo del usuario |
| `email` | `String` (100) | Correo institucional único para recuperación de clave |
| `rol` | `String` (20) | Nivel de acceso: `ADMIN`, `OPERADOR`, `CONSULTA` |
| `resetToken` | `String` (100) | Token temporal UUID para recuperación de clave |
| `resetTokenExpires`| `LocalDateTime` | Fecha y hora de vencimiento del token (30 min) |
| `createdAt` | `LocalDateTime` | Timestamp automático de creación de la cuenta |

---

## 5. Reportes Parametrizados Implementados

La aplicación cuenta con los **4 reportes parametrizados funcionales** exigidos en el documento académico:

### Reportes de Emisora
1. **Reporte por País y Género (`/reportes/emisoras/pais-genero`):**
   - **Parámetros:** `pais` (obligatorio, dropdown dinámico) y `genero` (opcional, dropdown dinámico).
   - **Propósito:** Permite segmentar el catálogo radial por nación y estilo musical.
2. **Reporte por Cobertura y Capacidad Operativa (`/reportes/emisoras/cobertura`):**
   - **Parámetros:** `minCiudades` (mínimo de cobertura), `maxCiudades` (máximo de cobertura) y `minLocutores` (personal en nómina).
   - **Propósito:** Evalúa el impacto territorial y la robustez operativa de las emisoras.

### Reportes de Usuario
3. **Reporte de Usuarios por Rol (`/reportes/usuarios/rol`):**
   - **Parámetros:** `rol` (`ADMIN`, `OPERADOR`, `CONSULTA`).
   - **Propósito:** Auditoría y control del personal con privilegios en el sistema.
4. **Reporte de Usuarios por Rango de Fechas (`/reportes/usuarios/fechas`):**
   - **Parámetros:** `desde` (fecha inicial) y `hasta` (fecha final).
   - **Propósito:** Seguimiento histórico y cronológico del registro de cuentas.

---

## 6. Usuarios de Prueba y Roles

El sistema cuenta con un componente `DataInitializer` que inicializa automáticamente los usuarios de prueba al arrancar (tanto en H2 como en MySQL si las tablas están vacías):

| Código | Usuario / Correo | Contraseña | Rol | Permisos |
|---|---|---|---|---|
| `U001` | `jquinteroh2@unicartagena.edu.co` | `Admin2026*` | **ADMIN** | Control total (Usuarios, Emisoras, Reportes) |
| `U002` | `operador.emisora@yopmail.com` | `Admin2026*` | **OPERADOR** | Gestión completa de Emisoras y Reportes |
| `U003` | `carlos.emisora@yopmail.com` | `Admin2026*` | **OPERADOR** | Gestión completa de Emisoras y Reportes |
| `U004` | `consulta.emisora@yopmail.com` | `Admin2026*` | **CONSULTA** | Solo visualización de Emisoras y Reportes |
| `U005` | `pedro.emisora@yopmail.com` | `Admin2026*` | **CONSULTA** | Solo visualización de Emisoras y Reportes |

*(Nota: En la pantalla de login se incluyeron botones de autocompletado rápido para facilitar la demostración fluida en video).*

---

## 7. Instrucciones para Ejecución Local

### Opción A: Ejecución Directa con H2 (Recomendada &ndash; Cero Configuración)
No requiere instalar ni encender bases de datos externas:

```bash
# 1. Clonar el repositorio o ingresar a la carpeta del proyecto
cd Emisora2.0

# 2. Ejecutar la aplicación mediante Maven
mvn spring-boot:run
```

O en Windows, ejecutar haciendo doble clic en el script:
```cmd
run-local.bat
```

La aplicación abrirá en: **`http://localhost:8080/`**  
Consola H2 disponible en: **`http://localhost:8080/h2-console`** (`JDBC URL: jdbc:h2:mem:emisora_db`, usuario: `sa`, clave en blanco).

---

### Opción B: Ejecución con MySQL Local
Si se desea utilizar el motor MySQL local:

1. Iniciar el servicio de MySQL:
   ```cmd
   net start MySQL97
   ```
2. Cargar los scripts SQL en la base de datos:
   ```bash
   mysql -u root -p < db/01_schema.sql
   mysql -u root -p < db/02_data.sql
   ```
3. Ejecutar Spring Boot activando el perfil `mysql`:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

---

## 8. Recuperación de Contraseña por Correo

El sistema implementa el flujo seguro de restablecimiento de clave:
1. El usuario accede a "¿Olvidaste tu contraseña?" (`/recuperar-clave`).
2. Digita su correo (ej: `jquinteroh2@unicartagena.edu.co`).
3. El servicio genera un token temporal UUID con vigencia de **30 minutos**.
4. **Envío de Correo:**
   - Si se configuran las variables de entorno SMTP (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`), se envía un correo HTML con botón de restablecimiento.
   - Si no se configuran variables SMTP o se ejecutan pruebas locales offline, el servicio **EmailService** imprime el enlace de restablecimiento directamente en la terminal de la consola, permitiendo probar y demostrar el 100% del flujo sin bloqueos.
5. Al hacer clic en el enlace (`/restablecer-clave?token=...`), el usuario ingresa su nueva contraseña y su confirmación.
6. La clave se cifra con BCrypt y el token queda invalidado de inmediato.

---

## 9. Despliegue en la Nube

El proyecto incluye:
- `Dockerfile` multi-etapa con Java 21 optimizado para contenedores.
- `render.yaml` para despliegue automatizado en **Render.com**.
- Compatible con **Railway**, **Fly.io** o **Koyeb** conectando directamente el repositorio de GitHub.

---

## 10. Estructura de Scripts de Base de Datos

En la carpeta `db/`:
- `db/01_schema.sql`: Definición DDL de tablas `usuarios` y `emisoras`, restricciones `CHECK`, índices y claves foráneas.
- `db/02_data.sql`: Registros iniciales DML con 5 usuarios y 14 emisoras con cobertura en Colombia, México, Argentina, España y Perú.
