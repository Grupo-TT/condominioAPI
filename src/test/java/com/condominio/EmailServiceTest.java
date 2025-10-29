package com.condominio;

import com.condominio.persistence.model.Asamblea;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.dto.response.ObligacionDTO;
import com.condominio.service.implementation.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    private EmailService emailService;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService(mailSender, templateEngine);
        mimeMessage = mock(MimeMessage.class);
    }

    @Test
    void testEnviarPasswordTemporal_mockeado() throws MessagingException, InterruptedException {

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
    void testEnviarPago_mockeado() throws MessagingException, InterruptedException {
        // Arrange
        EmailService spyEmailService = spy(emailService);

        ObligacionDTO obligacionDTO = new ObligacionDTO();
        obligacionDTO.setMotivo("Pago de administración");
        obligacionDTO.setCasa(15);
        obligacionDTO.setMonto(150000);
        obligacionDTO.setFechaPago(LocalDate.now());

        // Simula que el HTML se genera correctamente
        doReturn("<html>Mock HTML Pago</html>")
                .when(spyEmailService)
                .generarHtmlPagoConThymeleaf(obligacionDTO);

        MimeMessage mensaje = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mensaje);

        // Act
        spyEmailService.enviarPago("usuario@correo.com", obligacionDTO);



        // Assert
        verify(mailSender).send(mensaje);
        verify(spyEmailService).generarHtmlPagoConThymeleaf(obligacionDTO);
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

    @Test
    void testGenerarHtmlPagoConThymeleaf_mockeado() {
        // Arrange
        ObligacionDTO obligacionDTO = new ObligacionDTO();
        obligacionDTO.setMotivo("Pago de mantenimiento");
        obligacionDTO.setCasa(8);
        obligacionDTO.setMonto(250000);
        obligacionDTO.setFechaPago(LocalDate.of(2025, 10, 13));

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Motivo: Pago de mantenimiento<br>Casa: 8<br>Monto: $250.000<br>Fecha: 2025-10-13</html>");

        // Act
        String html = emailService.generarHtmlPagoConThymeleaf(obligacionDTO);

        // Assert
        assertNotNull(html);
        assertTrue(html.contains(obligacionDTO.getMotivo()));
        assertTrue(html.contains(String.valueOf(obligacionDTO.getCasa())));
        assertTrue(html.contains("$250.000"));
        assertTrue(html.contains(obligacionDTO.getFechaPago().toString()));
        verify(templateEngine, times(1)).process(anyString(), any(Context.class));
    }

    @Test
    void testEnviarPazYSalvo_mockeado() throws InterruptedException, MessagingException {

        EmailService spyEmailService = spy(emailService);
        MimeMessage mensaje = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mensaje);

        byte[] pdfBytes = "dummy pdf bytes".getBytes();
        String destinatario = "usuario@correo.com";
        String nombreArchivo = "paz_y_salvo.pdf";

        spyEmailService.enviarPazYSalvo(destinatario, pdfBytes, nombreArchivo);



        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mensaje);
    }
    @Test
    void enviarPazYSalvo_EnvioExitoso_NoLanzaExcepcion() throws Exception {
        byte[] pdf = {1, 2, 3};
        String destinatario = "test@example.com";
        String nombreArchivo = "paz_y_salvo.pdf";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.enviarPazYSalvo(destinatario, pdf, nombreArchivo);



        verify(mailSender).send(mimeMessage);
    }
    @Test
    void enviarPazYSalvo_FallaEnvio_EjecutaLogError() throws Exception {
        byte[] pdf = {1, 2, 3};
        String destinatario = "fail@example.com";
        String nombreArchivo = "paz_y_salvo.pdf";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Fallo al enviar")).when(mailSender).send(any(MimeMessage.class));

        emailService.enviarPazYSalvo(destinatario, pdf, nombreArchivo);



        verify(mailSender).send(mimeMessage);
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
}