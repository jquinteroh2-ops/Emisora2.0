package com.emisora.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Servicio para el envío de correos electrónicos.
 * Soporta configuración SMTP real y fallback seguro con registro en log
 * para pruebas locales y sustentación académica sin requerir SMTP externo.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.mail.from:noreply@emisora.com}")
    private String mailFrom;

    /**
     * Envía un correo con el enlace de restablecimiento de contraseña.
     * Si no hay credenciales SMTP configuradas o el envío falla por conectividad,
     * el servicio imprime el enlace en la consola para que el usuario o docente
     * pueda continuar la prueba sin interrupciones.
     *
     * @param destinatario correo de destino
     * @param nombreDestinatario nombre del usuario
     * @param enlaceRecuperacion enlace directo para restablecer la contraseña
     * @param token token generado
     */
    public boolean enviarRecuperacionContrasena(String destinatario, String nombreDestinatario,
                                                String enlaceRecuperacion, String token) {
        String asunto = "Recuperación de Contraseña - Emisora 2.0";

        String contenidoHtml = """
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
                            <p>Este enlace tiene una vigencia de <strong>30 minutos</strong> por seguridad.</p>
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
                """.formatted(nombreDestinatario, enlaceRecuperacion, enlaceRecuperacion);

        boolean enviadoPorSmtp = false;

        if (mailSender != null && mailUsername != null && !mailUsername.isBlank()) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
                helper.setFrom(mailFrom, "Emisora 2.0 - Soporte");
                helper.setTo(destinatario);
                helper.setSubject(asunto);
                helper.setText(contenidoHtml, true);

                mailSender.send(mimeMessage);
                enviadoPorSmtp = true;
                log.info("Correo de recuperación enviado exitosamente vía SMTP a {}", destinatario);
            } catch (Exception e) {
                log.warn("No se pudo enviar por SMTP a {}: {}. Se usará registro en consola.", destinatario, e.getMessage());
            }
        } else {
            log.info("Servidor SMTP no configurado. Operando en modo demostración/desarrollo.");
        }

        // Siempre registramos el enlace en el log para facilitar pruebas locales y sustentación
        System.out.println("================================================================================");
        System.out.println("SIMULACIÓN / REGISTRO DE CORREO DE RECUPERACIÓN DE CONTRASEÑA");
        System.out.println("Destinatario: " + destinatario + " (" + nombreDestinatario + ")");
        System.out.println("Asunto:       " + asunto);
        System.out.println("Token:        " + token);
        System.out.println("Enlace:       " + enlaceRecuperacion);
        System.out.println("Estado SMTP:  " + (enviadoPorSmtp ? "ENVIADO EXITOSAMENTE" : "MODO LOCAL / DEMOSTRACIÓN"));
        System.out.println("================================================================================");

        return true;
    }
}
