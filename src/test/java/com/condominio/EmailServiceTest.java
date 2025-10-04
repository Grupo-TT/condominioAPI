package com.condominio;

import com.condominio.service.implementation.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, templateEngine);

        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
    }

    @Test
    void testEnviarPasswordTemporal_mockeado() throws MessagingException {

        EmailService spyEmailService = spy(emailService);


        doReturn("<html>Mock HTML</html>")
                .when(spyEmailService)
                .generarHtmlConThymeleaf("abc123");

        MimeMessage mensaje = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mensaje);


        spyEmailService.enviarPasswordTemporal("user@correo.com", "abc123");


        verify(mailSender).send(mensaje);
    }

    @Test
    void testGenerarHtmlConThymeleaf_mockeado() {

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>password: clavePrueba123<br>login: http://localhost:8080</html>");

        String password = "clavePrueba123";
        String html = emailService.generarHtmlConThymeleaf(password);


        assertNotNull(html);
        assertTrue(html.contains(password));
        assertTrue(html.contains("http://localhost:8080"));
    }

}