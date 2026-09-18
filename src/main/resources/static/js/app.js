/**
 * JavaScript para Emisora 2.0 (Spring Boot MVC + Thymeleaf)
 * Funcionalidades auxiliares de la interfaz (cierre automático de alertas).
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
