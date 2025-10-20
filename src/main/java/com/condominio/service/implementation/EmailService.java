package com.condominio.service.implementation;


import com.condominio.dto.response.ObligacionDTO;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
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
        context.setVariable("fechaPago", obligacionDTO.getFechaPago());
        return templateEngine.process(PAGO_HTML, context);
    }

}