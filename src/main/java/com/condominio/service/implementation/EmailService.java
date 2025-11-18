package com.condominio.service.implementation;


import com.condominio.dto.response.MostrarObligacionDTO;
import com.condominio.dto.response.ObligacionDTO;
import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import com.condominio.persistence.model.Persona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import static com.condominio.util.constants.AppConstants.*;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async("mailTaskExecutor")
    public void enviarPasswordTemporal(String destinatario, String passwordTemporal) throws MessagingException {
        String htmlContent = generarHtmlConThymeleaf(passwordTemporal);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(destinatario);
        helper.setSubject(EMAIL_SUBJECT);
        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }

    public String generarHtmlConThymeleaf(String passwordTemporal) {
        Context context = new Context();
        context.setVariable("passwordTemporal", passwordTemporal);
        context.setVariable("loginUrl", LOGIN_URL);
        return templateEngine.process(PASSWORD_HTML, context);
    }

    @Async("mailTaskExecutor")
    public void enviarPago(String destinatario, ObligacionDTO obligacionDTO) throws MessagingException {
        String htmlContent = generarHtmlPagoConThymeleaf(obligacionDTO);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(destinatario);
        helper.setSubject(EMAIL_PAGO_SUBJECT);
        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }

    public String generarHtmlPagoConThymeleaf(ObligacionDTO obligacionDTO){
        Context context = new Context();
        context.setVariable("motivo", obligacionDTO.getMotivo());
        context.setVariable("casa", obligacionDTO.getCasa());
        context.setVariable("monto", obligacionDTO.getMonto());
        context.setVariable("saldo", obligacionDTO.getSaldo());
        context.setVariable("fechaPago", obligacionDTO.getFechaPago());
        return templateEngine.process(PAGO_HTML, context);
    }

    @Async("mailTaskExecutor")
    public void enviarPazYSalvo(String destinatario, byte[] pdfBytes, String nombreArchivo) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();


        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setTo(destinatario);
        helper.setSubject("Paz y Salvo - Condominio");
        helper.setText("Los administradores del condominio flor del campo han generado " +
                        "tu  paz y salvo.",
                false);


        helper.addAttachment(nombreArchivo, new org.springframework.core.io.ByteArrayResource(pdfBytes));

        try {
            mailSender.send(mimeMessage);
            log.info("Correo enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar correo a {}: {}", destinatario, e.getMessage());
        }
    }

    @Async("mailTaskExecutor")
    public void enviarSolicitud(String destinatario, SolicitudReservaRecursoDTO soliReservaDTO) throws MessagingException {
        log.info("EmailService.enviarSolicitud invoked para {}", destinatario);
        try {
        String htmlContent = generarHtmlSolicitudConThymeleaf(soliReservaDTO);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(destinatario);
        helper.setSubject(EMAIL_SOLICITUD_SUBJECT);
        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
        log.info("Correo enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar correo a {}: {}", destinatario, e.getMessage());
        }
    }
    public String generarHtmlSolicitudConThymeleaf(SolicitudReservaRecursoDTO soliReservaDTO){
        Context context = new Context();
        context.setVariable("recurso", soliReservaDTO.getRecursoComun().getNombre());
        context.setVariable("horaInicio", soliReservaDTO.getHoraInicio());
        context.setVariable("horaFin", soliReservaDTO.getHoraFin());
        context.setVariable("cantidadInvitados", soliReservaDTO.getNumeroInvitados());
        context.setVariable("fechaSolicitud", soliReservaDTO.getFechaSolicitud());
        context.setVariable("estado", soliReservaDTO.getEstadoSolicitud());
        return templateEngine.process(SOLICITUD_HTML, context);
    }

    @Async("mailTaskExecutor")
    public void enviarObligacionMensual(
            String destinatario,
            MostrarObligacionDTO obligacionDTO) throws MessagingException {

        String mesActual = obligacionDTO.getFecha()
                .getMonth()
                .getDisplayName(TextStyle.FULL, Locale.of("es", "ES"));
        mesActual = mesActual.substring(0, 1).toUpperCase() + mesActual.substring(1);

        String htmlContent = generarHtmlObligacionMensual(obligacionDTO);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(destinatario);
        helper.setSubject("Se ha generado tu mensualidad por administración de " + mesActual);
        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }

    @Async("mailTaskExecutor")
    public void enviarObligacionesMensualesMasivas(List<Persona> personas, MostrarObligacionDTO mostrarObligacionDTO) {
        personas.forEach(persona -> {
            try {
                enviarObligacionMensual(
                        persona.getUser().getEmail(),
                        mostrarObligacionDTO
                );
            } catch (MessagingException e) {

                log.error("No se pudo enviar correo a {}: {}", persona.getUser().getEmail(), e.getMessage());
            }
        });
    }

    public String generarHtmlObligacionMensual(MostrarObligacionDTO obligacionDTO) {
        Context context = new Context();

        context.setVariable("titulo", obligacionDTO.getTitulo());
        context.setVariable("motivo", obligacionDTO.getMotivo());
        context.setVariable("casa", obligacionDTO.getCasa());
        context.setVariable("monto", obligacionDTO.getMonto());

        return templateEngine.process("email/obligacion-mensual", context);
    }

    @Async("mailTaskExecutor")
    public void enviarPasswordOlvidada(String destinatario,
                                       String passwordTemporal,
                                       String nombreUsuario) {

        try {
            String htmlContent = generarHtmlOlvidarPw(passwordTemporal, nombreUsuario);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(destinatario);
            helper.setSubject("Tu contraseña temporal - Condominio Flor del campo");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            log.error("Error enviando correo a {}", destinatario, e);
        }
    }

    public String generarHtmlOlvidarPw(String passwordTemporal, String nombreUsuario) {
        Context context = new Context();
        context.setVariable("passwordTemporal", passwordTemporal);
        context.setVariable("nombreUsuario", nombreUsuario);

        context.setVariable("loginUrl", "http://localhost:8080");

        return templateEngine.process("email/password-olvidada.html", context);
    }
}

