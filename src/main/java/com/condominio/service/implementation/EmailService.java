package com.condominio.service.implementation;


import com.condominio.persistence.model.Asamblea;
import com.condominio.persistence.model.Persona;
import com.condominio.dto.response.ObligacionDTO;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import static com.condominio.util.constants.AppConstants.*;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

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
    public void enviarInvitacionAsamblea(
            String destinatario,
            String nombreAsamblea,
            Date fecha,
            LocalTime hora) throws MessagingException {

        String htmlContent = generarHtmlInvitacionAsamblea(nombreAsamblea, fecha, hora);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(destinatario);
        helper.setSubject("Invitación a la Asamblea: " + nombreAsamblea);
    public void enviarPago(String destinatario, ObligacionDTO obligacionDTO) throws MessagingException {
        String htmlContent = generarHtmlPagoConThymeleaf(obligacionDTO);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(destinatario);
        helper.setSubject(EMAIL_PAGO_SUBJECT);
        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }

    @Async("mailTaskExecutor")
    public void enviarInvitacionesAsambleaMasivas(List<Persona> personas, Asamblea asamblea) {
        personas.forEach(persona -> {
            try {
                enviarInvitacionAsamblea(
                        persona.getUser().getEmail(),
                        asamblea.getTitulo(),
                        asamblea.getFecha(),
                        asamblea.getHoraInicio()
                );
            } catch (MessagingException e) {

                System.err.println("No se pudo enviar correo a " + persona.getUser().getEmail() + ": " + e.getMessage());
            }
        });
    }
    public String generarHtmlInvitacionAsamblea(String nombreAsamblea, Date fecha, LocalTime hora) {
        Context context = new Context();
        context.setVariable("nombreAsamblea", nombreAsamblea);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        context.setVariable("fecha", sdf.format(fecha));

        context.setVariable("hora", hora.format(DateTimeFormatter.ofPattern("HH:mm")));
        return templateEngine.process("email/invitacion-asamblea", context);
    public String generarHtmlPagoConThymeleaf(ObligacionDTO obligacionDTO){
        Context context = new Context();
        context.setVariable("motivo", obligacionDTO.getMotivo());
        context.setVariable("casa", obligacionDTO.getCasa());
        context.setVariable("monto", obligacionDTO.getMonto());
        context.setVariable("fechaPago", obligacionDTO.getFechaPago());
        return templateEngine.process(PAGO_HTML, context);
    }

}