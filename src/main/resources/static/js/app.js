/**
 * JavaScript para Emisora 2.0 (Spring Boot MVC + Thymeleaf)
 * Funcionalidades auxiliares de UI, autocompletado de prueba y confirmaciones.
 */

document.addEventListener('DOMContentLoaded', function () {
    // Cierre automático de alertas tras 6 segundos
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) {
                bsAlert.close();
            }
        }, 6000);
    });
});

/**
 * Función para rellenar credenciales de prueba en el formulario de login.
 * Facilita la sustentación académica en video.
 */
function rellenarLogin(username, password) {
    const userInput = document.getElementById('username');
    const passInput = document.getElementById('password');
    if (userInput && passInput) {
        userInput.value = username;
        passInput.value = password;
        userInput.focus();
    }
}
