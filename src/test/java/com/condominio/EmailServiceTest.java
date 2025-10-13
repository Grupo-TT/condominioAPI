package com.condominio;

import com.condominio.persistence.model.Asamblea;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.service.implementation.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;



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

    @Test
    void testGenerarHtmlInvitacionAsamblea() {
        Date fecha = new GregorianCalendar(2025, Calendar.OCTOBER, 14).getTime();
        LocalTime hora = LocalTime.of(15, 30);

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Invitación Mock</html>");

        String html = emailService.generarHtmlInvitacionAsamblea("Reunión", fecha, hora);

        assertNotNull(html);
        assertTrue(html.contains("Invitación Mock"));


        verify(templateEngine).process(eq("email/invitacion-asamblea"), any(Context.class));
    }
    @Test
    void testEnviarInvitacionAsamblea() throws MessagingException {
        EmailService spyService = spy(emailService);

        Date fecha = new GregorianCalendar(2025, Calendar.OCTOBER, 14).getTime();
        LocalTime hora = LocalTime.of(15, 30);

        doReturn("<html>Mock HTML</html>")
                .when(spyService)
                .generarHtmlInvitacionAsamblea("Reunión", fecha, hora);

        MimeMessage mensaje = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mensaje);

        spyService.enviarInvitacionAsamblea("user@correo.com", "Reunión", fecha, hora);

        verify(mailSender).send(mensaje);
    }

    @Test
    void testEnviarInvitacionesAsambleaMasivas() throws MessagingException {
        EmailService spyService = spy(emailService);

        Date fecha = new GregorianCalendar(2025, Calendar.OCTOBER, 14).getTime();
        LocalTime hora = LocalTime.of(15, 30);

        doNothing().when(spyService).enviarInvitacionAsamblea(anyString(), anyString(), any(Date.class), any(LocalTime.class));

        // Creamos personas de prueba
        UserEntity user1 = new UserEntity();
        user1.setEmail("a@correo.com");
        Persona p1 = new Persona();
        p1.setUser(user1);

        UserEntity user2 = new UserEntity();
        user2.setEmail("b@correo.com");
        Persona p2 = new Persona();
        p2.setUser(user2);

        Asamblea asamblea = new Asamblea();
        asamblea.setTitulo("Reunión");
        asamblea.setFecha(fecha);
        asamblea.setHoraInicio(hora);

        spyService.enviarInvitacionesAsambleaMasivas(Arrays.asList(p1, p2), asamblea);


        verify(spyService, times(2))
                .enviarInvitacionAsamblea(anyString(), anyString(), any(Date.class), any(LocalTime.class));
    }
}