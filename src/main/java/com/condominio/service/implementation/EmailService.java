package com.condominio.service.implementation;


import com.condominio.dto.request.SendEmailsDTO;
import com.condominio.dto.response.ObligacionDTO;
import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import com.condominio.util.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import static com.condominio.util.constants.AppConstants.*;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final int maxFileSizeMB = 10;

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



    public void sendToMany(SendEmailsDTO request) {


        if (request.getEmails() == null || request.getEmails().isEmpty()) {
            throw new ApiException(
                    "Debe enviar al menos un correo",
                    HttpStatus.OK
            );
        }

        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            throw new ApiException("El asunto es obligatorio", HttpStatus.BAD_REQUEST);
        }


        MultipartFile file = request.getFile();

        boolean hasFile = (file != null && !file.isEmpty());


        boolean messageEmpty = (request.getMessage() == null || request.getMessage().trim().isEmpty());


        if (!hasFile && messageEmpty) {
            throw new ApiException(
                    "Debe diligenciar el cuerpo del correo si no adjunta una imagen o archivo",
                    HttpStatus.OK
            );
        }
        if (file != null && !file.isEmpty()) {

            long maxBytes = maxFileSizeMB * 1024L * 1024L;


            if (file.getSize() > maxBytes) {
                throw new ApiException(
                        "El archivo es muy grande. Máximo " + maxFileSizeMB + " MB",
                        HttpStatus.OK
                );
            }


            String type = file.getContentType();
            if (!isAllowed(type)) {
                throw new ApiException(
                        "Tipo de archivo no permitido",
                        HttpStatus.OK
                );
            }
        }


        String body = request.getMessage();
        if (body == null || body.trim().isEmpty()) {
            body = " ";
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, file != null);

            helper.setSubject(request.getSubject());
            helper.setText(body ,false);
            helper.setTo(request.getEmails().toArray(new String[0])); // muchos destinatarios


            if (file != null && !file.isEmpty()) {
                String filename = (file.getOriginalFilename() != null)
                        ? file.getOriginalFilename()
                        : "archivo";
                helper.addAttachment(filename, file);
            }

            mailSender.send(message);

        } catch (Exception e) {
            throw new ApiException(
                    "Error al enviar el correo",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
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
}

