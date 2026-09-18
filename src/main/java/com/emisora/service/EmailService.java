package com.emisora.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.HtmlUtils;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Servicio para el envío de correos electrónicos de recuperación de contraseña.
 * Usa, en este orden, el primer medio que esté configurado:
 *   1. API HTTPS de Brevo (variable BREVO_API_KEY). Es el medio usado en la nube,
 *      porque el plan gratuito de Render bloquea los puertos SMTP.
 *   2. SMTP con JavaMailSender (variables MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD...).
 * Si ninguno está configurado (desarrollo local), el enlace se escribe en la consola.
 * Las claves nunca están en el código: se leen de variables de entorno.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String ASUNTO_RECUPERACION = "Recuperación de Contraseña - Emisora 2.0";

    private final JavaMailSender mailSender;
    private final RestClient restClient;
    private final String brevoApiKey;
    private final String mailUsername;
    private final String mailFrom;
    private final String mailFromName;
    private final int minutosVigencia;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                        @Value("${app.mail.brevo-api-key:}") String brevoApiKey,
                        @Value("${spring.mail.username:}") String mailUsername,
                        @Value("${app.mail.from:}") String mailFrom,
                        @Value("${app.mail.from-name:Emisora 2.0}") String mailFromName,
                        @Value("${app.reset-token-expiration-minutes:30}") int minutosVigencia) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.brevoApiKey = brevoApiKey;
        this.mailUsername = mailUsername;
        this.mailFrom = mailFrom;
        this.mailFromName = mailFromName;
        this.minutosVigencia = minutosVigencia;

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    /**
     * Envía un correo con el enlace de restablecimiento de contraseña.
     *
     * @param destinatario       correo de destino
     * @param nombreDestinatario nombre del usuario
     * @param enlaceRecuperacion enlace directo para restablecer la contraseña
     * @return true si el correo salió por Brevo o SMTP; false si solo quedó en la consola
     */
    public boolean enviarRecuperacionContrasena(String destinatario, String nombreDestinatario,
                                                String enlaceRecuperacion) {
        String html = construirHtml(nombreDestinatario, enlaceRecuperacion);
        String texto = "Hola " + nombreDestinatario + ",\n\n"
                + "Para restablecer su contraseña en Emisora 2.0 abra este enlace (vigente " + minutosVigencia + " minutos):\n"
                + enlaceRecuperacion + "\n\n"
                + "Si usted no solicitó este cambio, ignore este mensaje.";

        try {
            if (!brevoApiKey.isBlank()) {
                enviarPorApiBrevo(destinatario, nombreDestinatario, html, texto);
                log.info("Correo de recuperación enviado por la API de Brevo a {}", destinatario);
                return true;
            }
            if (mailSender != null && !mailUsername.isBlank()) {
                enviarPorSmtp(destinatario, html, texto);
                log.info("Correo de recuperación enviado por SMTP a {}", destinatario);
                return true;
            }
            log.warn("El envío de correo no está configurado (falta BREVO_API_KEY o MAIL_USERNAME).");
        } catch (RestClientResponseException e) {
            log.error("La API de Brevo rechazó el correo para {}: {} {}", destinatario,
                    e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("No se pudo enviar el correo de recuperación a {}: {}", destinatario, e.getMessage());
        }

        // Modo desarrollo: sin correo configurado, el enlace queda en la consola del servidor
        log.info("Enlace de recuperación para {}: {}", destinatario, enlaceRecuperacion);
        return false;
    }

    private void enviarPorApiBrevo(String destinatario, String nombre, String html, String texto) {
        Map<String, Object> cuerpo = Map.of(
                "sender", Map.of("name", mailFromName, "email", mailFrom),
                "to", List.of(Map.of("email", destinatario, "name", nombre)),
                "subject", ASUNTO_RECUPERACION,
                "htmlContent", html,
                "textContent", texto
        );

        restClient.post()
                .uri(BREVO_API_URL)
                .header("api-key", brevoApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .toBodilessEntity();
    }

    private void enviarPorSmtp(String destinatario, String html, String texto) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
        helper.setFrom(mailFrom.isBlank() ? mailUsername : mailFrom, mailFromName);
        helper.setTo(destinatario);
        helper.setSubject(ASUNTO_RECUPERACION);
        helper.setText(texto, html);
        mailSender.send(mimeMessage);
    }

    private String construirHtml(String nombreDestinatario, String enlaceRecuperacion) {
        String nombre = HtmlUtils.htmlEscape(nombreDestinatario);
        String enlace = HtmlUtils.htmlEscape(enlaceRecuperacion);
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                        .header { background: #0d6efd; color: #ffffff; padding: 25px; text-align: center; }
                        .content { padding: 30px; color: #333333; line-height: 1.6; }
                        .button { display: inline-block; padding: 12px 24px; background-color: #0d6efd; color: #ffffff !important; text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; }
                        .footer { background: #f8f9fa; padding: 15px; text-align: center; font-size: 12px; color: #6c757d; border-top: 1px solid #e9ecef; }
                        .code-box { background: #e9ecef; padding: 10px; border-radius: 4px; font-family: monospace; font-size: 14px; word-break: break-all; margin-top: 10px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>Sistema de Gestión de Emisoras Radiales</h2>
                        </div>
                        <div class="content">
                            <p>Estimado/a <strong>%s</strong>,</p>
                            <p>Hemos recibido una solicitud para restablecer la contraseña de su cuenta en <strong>Emisora 2.0</strong>.</p>
                            <p>Para crear una nueva clave, haga clic en el siguiente botón:</p>
                            <p style="text-align: center;">
                                <a href="%s" class="button">Restablecer Mi Contraseña</a>
                            </p>
                            <p>Este enlace tiene una vigencia de <strong>%d minutos</strong> y solo se puede usar una vez.</p>
                            <p>Si el botón no funciona, copie y pegue la siguiente URL en su navegador:</p>
                            <div class="code-box">%s</div>
                            <p><br>Si usted no solicitó este cambio, puede ignorar este mensaje; su contraseña actual no sufrirá alteraciones.</p>
                        </div>
                        <div class="footer">
                            <p>Desarrollo Web - Unidad 2 | Ejercicio 25: Emisora</p>
                            <p>&copy; 2026 Universidad de Cartagena</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(nombre, enlace, minutosVigencia, enlace);
    }
}
