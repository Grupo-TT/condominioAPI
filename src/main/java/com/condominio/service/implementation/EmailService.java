package com.condominio.service.implementation;

import com.condominio.dto.request.SendEmailsDTO;
import com.condominio.dto.response.MostrarObligacionDTO;
import com.condominio.dto.response.ObligacionDTO;
import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import com.condominio.persistence.model.CorreoDestinatario;
import com.condominio.persistence.model.CorreoEnviado;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.repository.CorreoEnviadoRepository;
import com.condominio.util.exception.ApiException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.condominio.util.constants.AppConstants.*;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final int maxFileSizeMB = 10;
    private final CorreoEnviadoRepository correoEnviadoRepository;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine,CorreoEnviadoRepository correoEnviadoRepository) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.correoEnviadoRepository = correoEnviadoRepository;
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
    @Autowired
    @Lazy
    private EmailService self;

    public void sendToMany(SendEmailsDTO request) {
        if (request.getEmails() == null || request.getEmails().isEmpty()) {
            throw new ApiException("Debe enviar al menos un correo", HttpStatus.OK);
        }

        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            throw new ApiException("El asunto es obligatorio", HttpStatus.BAD_REQUEST);
        }

        MultipartFile file = request.getFile();
        boolean hasFile = (file != null && !file.isEmpty());
        boolean messageEmpty = (request.getMessage() == null || request.getMessage().trim().isEmpty());

        if (!hasFile && messageEmpty) {
            throw new ApiException("Debe diligenciar el cuerpo del correo si no adjunta una imagen o archivo", HttpStatus.OK);
        }

        byte[] fileBytes = null;
        String filename = null;

        if (hasFile) {
            long maxBytes = maxFileSizeMB * 1024L * 1024L;
            if (file.getSize() > maxBytes) {
                throw new ApiException("El archivo es muy grande. Máximo " + maxFileSizeMB + " MB", HttpStatus.OK);
            }

            String type = file.getContentType();
            if (!isAllowed(type)) {
                throw new ApiException("Tipo de archivo no permitido", HttpStatus.OK);
            }

            try {
                fileBytes = file.getBytes();
                filename = file.getOriginalFilename();
            } catch (java.io.IOException e) {
                log.error("Error al leer el archivo adjunto.", e);
                throw new ApiException("Error al leer el archivo adjunto.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        String body = request.getMessage();
        if (body == null || body.trim().isEmpty()) {
            body = " ";
        }
        String cleanTitle = superClean(request.getSubject());
        String cleanBody = superClean(body);
        saveEmailLog(cleanTitle, cleanBody, request.getEmails());
        self.sendToManyAsync(request.getEmails(), request.getSubject(), body, fileBytes, filename);
    }

    @Async("mailTaskExecutor")
    public void sendToManyAsync(java.util.List<String> emails, String subject, String body, byte[] fileBytes, String filename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, fileBytes != null);

            helper.setSubject(subject);
            helper.setText(body, false);
            helper.setTo(emails.toArray(new String[0]));

            if (fileBytes != null) {
                String originalFilename = (filename != null) ? filename : "archivo";
                helper.addAttachment(originalFilename, new org.springframework.core.io.ByteArrayResource(fileBytes));
            }

            mailSender.send(message);
            log.info("Correo masivo asíncrono enviado a {} destinatarios.", emails.size());
        } catch (Exception e) {
            log.error("Error al enviar el correo masivo asíncrono: {}", e.getMessage(), e);

        }
    }

    private boolean isAllowed(String type) {
        return type != null && (
                type.startsWith("image/") ||
                        type.equals("application/pdf") ||
                        type.equals("application/msword") ||
                        type.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );
    }

    private void saveEmailLog(String subject, String body, List<String> emails) {

        // 1. Crear el correo principal
        CorreoEnviado correo = new CorreoEnviado();
        correo.setTitulo(subject);
        correo.setCuerpo(body);
        correo.setFechaEnvio(LocalDateTime.now());

        // 2. Crear la lista de destinatarios
        List<CorreoDestinatario> destinatarios = emails.stream()
                .map(email -> {
                    CorreoDestinatario d = new CorreoDestinatario();
                    d.setEmailDestinatario(email);
                    d.setCorreoEnviado(correo);  // link
                    return d;
                })
                .collect(Collectors.toList());

        correo.setDestinatarios(destinatarios);

        correoEnviadoRepository.save(correo);
    }

    public String superClean(String input) {
        if (input == null) return null;

        String noHtml = org.jsoup.Jsoup.clean(input, org.jsoup.safety.Safelist.none());


        String normalized = java.text.Normalizer.normalize(noHtml, java.text.Normalizer.Form.NFC);

        return normalized.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ,.!?()\"'\\-\\n]", "");
    }
}

