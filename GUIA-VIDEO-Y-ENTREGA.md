# Guía de Sustentación en Video y Ficha de Entrega

**Proyecto:** Ejercicio 25 &mdash; **Emisora**  
**Asignatura:** Desarrollo Web &bull; Unidad 2  
**Estudiante:** Jose Antonio Quintero Herrera  
**Código:** 7502510055  
**Semestre:** Cuarto Semestre  
**Universidad:** Universidad de Cartagena  

---

## 1. Ficha de Datos para el PDF de Entrega

La entrega académica consiste en un único archivo PDF que debe contener esta ficha con los enlaces:

| Campo | Información a Incluir |
|---|---|
| **Estudiante** | 7502510055 &mdash; Jose Antonio Quintero Herrera |
| **Datos académicos** | Cuarto semestre &mdash; Desarrollo Web (Unidad 2) |
| **Actividad** | Spring Boot MVC con Thymeleaf: desarrollo web basado en framework |
| **Ejercicio asignado** | N.º 25 &mdash; Emisora |
| **Guía utilizada** | Arquitectura Spring Web MVC en capas con Thymeleaf y Spring Data JPA |
| **Código fuente (GitHub)** | https://github.com/jquinteroh2-ops/Emisora2.0 |
| **Sustentación (Video)** | *(Pegar aquí el enlace de YouTube o Vimeo: modo No listado o Público)* |
| **Aplicación desplegada** | *(Pegar aquí el enlace de Render o plataforma seleccionada)* |
| **Usuarios de prueba** | **ADMIN:** `jquinteroh2@unicartagena.edu.co` &bull; **OPERADOR:** `operador.emisora@yopmail.com` &bull; **CONSULTA:** `consulta.emisora@yopmail.com` &bull; Clave común: `Admin2026*` |

---

## 2. Guion Estructurado para la Grabación del Video (YouTube / Vimeo)

> [!IMPORTANT]
> El video debe ser individual, mostrando **rostro (cámara web)** y **voz propia** en todo momento mientras se comparte la pantalla.

### Parte 1: Introducción y Ejercicio Asignado (1 min)
1. Saludar y presentarse: *"Mi nombre es Jose Antonio Quintero Herrera, código 7502510055, estudiante de cuarto semestre de Desarrollo Web en la Universidad de Cartagena. Mi ejercicio asignado es el número 25: Emisora"*.
2. Explicar brevemente el problema: gestionar emisoras radiales, frecuencias FM/AM, programación, locutores, cobertura en ciudades, así como la seguridad de usuarios.

### Parte 2: Arquitectura MVC vs API RESTful (2 min)
1. Abrir VS Code o el IDE y mostrar la estructura de paquetes:
   - `com.emisora.controller`
   - `com.emisora.service`
   - `com.emisora.repository`
   - `com.emisora.entity`
   - `templates/` (Thymeleaf)
2. **Explicar la diferencia clave con una API RESTful:**
   - *"A diferencia de una API RESTful que devuelve JSON a un frontend separado como React o Angular, aquí utilizamos Spring Boot con Spring MVC tradicional, donde el servidor renderiza directamente las vistas HTML mediante plantillas Thymeleaf"*.
3. Explicar el flujo completo de una petición:
   *Navegador &rarr; Controller &rarr; Service &rarr; Repository &rarr; Base de datos &rarr; Service &rarr; Controller &rarr; Model &rarr; Thymeleaf &rarr; HTML al Navegador*.

### Parte 3: Demostración Funcional en Vivo (4-5 min)
1. **Inicio de sesión y control de acceso:**
   - Mostrar el login en `http://localhost:8080/login`.
   - Iniciar sesión como `U004` (CONSULTA): mostrar que no puede acceder a `/usuarios` ni ver botones de crear emisora.
   - Cerrar sesión e iniciar como `U001` (ADMIN): mostrar acceso total.
2. **CRUD Completo de Emisora:**
   - Mostrar el catálogo de emisoras (`/emisoras`).
   - Crear una nueva emisora con frecuencia FM/AM (`/emisoras/nuevo`).
   - Ver el detalle de la emisora creada (`/emisoras/ver/{id}`).
   - Modificarla (`/emisoras/editar/{id}`).
   - Eliminar una emisora de prueba.
3. **CRUD Completo de Usuario:**
   - Ir a `/usuarios`.
   - Crear un nuevo usuario con rol OPERADOR o CONSULTA.
   - Editar y verificar que la contraseña se cifra con BCrypt.
4. **Los 4 Reportes Parametrizados:**
   - **Reporte 1:** Emisoras por País (`Colombia`) y Género (`Tropical`).
   - **Reporte 2:** Emisoras por Cobertura (ej: ciudades entre 5 y 35, mínimo 10 locutores).
   - **Reporte 3:** Usuarios por Rol (`ADMIN` o `OPERADOR`).
   - **Reporte 4:** Usuarios por Rango de Fechas.
5. **Recuperación de Contraseña por Correo:**
   - Cerrar sesión, hacer clic en *"¿Olvidaste tu contraseña?"*.
   - Digitar el correo `jquinteroh2@unicartagena.edu.co`.
   - Mostrar la notificación y abrir la consola/terminal para mostrar el log del correo con el enlace generado.
   - Abrir el enlace (`/restablecer-clave?token=...`), ingresar nueva clave y volver a iniciar sesión.

### Parte 4: Despliegue y Control de Versiones (1-2 min)
1. Mostrar la aplicación funcionando en el enlace de Internet (Render o nube).
2. Abrir GitHub y mostrar:
   - Repositorio público `https://github.com/jquinteroh2-ops/Emisora2.0`.
   - Historial de commits lógicos y progresivos evidenciando el desarrollo paso a paso.
