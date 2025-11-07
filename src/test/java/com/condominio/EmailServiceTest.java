package com.condominio;

import com.condominio.dto.response.ObligacionDTO;
import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.persistence.model.RecursoComun;
import com.condominio.service.implementation.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
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
    void enviarSolicitud_debeConstruirContextYCambiarHtmlYEnviar() throws Exception {

        String destinatario = "prueba@correo.com";
        SolicitudReservaRecursoDTO dto = new SolicitudReservaRecursoDTO();
        RecursoComun recurso = new RecursoComun();
        recurso.setNombre("Salón comunal");
        dto.setRecursoComun(recurso);
        dto.setHoraInicio(LocalTime.of(10, 0));
        dto.setHoraFin(LocalTime.of(12, 0));
        dto.setNumeroInvitados(8);
        dto.setFechaSolicitud(LocalDate.of(2025, 11, 7));
        dto.setEstadoSolicitud(EstadoSolicitud.APROBADA);

        String expectedHtml = "<html>OK</html>";
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(expectedHtml);

        MimeMessage mensaje = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mensaje);

        emailService.enviarSolicitud(destinatario, dto);

        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine, times(1)).process(anyString(), ctxCaptor.capture());

        Context captured = ctxCaptor.getValue();
        assertThat(captured.getVariable("recurso")).isEqualTo("Salón comunal");
        assertThat(captured.getVariable("horaInicio")).isEqualTo(dto.getHoraInicio());
        assertThat(captured.getVariable("horaFin")).isEqualTo(dto.getHoraFin());
        assertThat(captured.getVariable("cantidadInvitados")).isEqualTo(dto.getNumeroInvitados());
        assertThat(captured.getVariable("fechaSolicitud")).isEqualTo(dto.getFechaSolicitud());
        assertThat(captured.getVariable("estado")).isEqualTo(dto.getEstadoSolicitud());

        // Verificamos que se creó y envió el MimeMessage
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mensaje);
    }

    @Test
    void enviarSolicitud_siTemplateLanzaExcepcion_noLlamaMailSenderSend() throws Exception {

        String destinatario = "fallo@correo.com";
        SolicitudReservaRecursoDTO dto = new SolicitudReservaRecursoDTO();
        RecursoComun recurso = new RecursoComun();
        recurso.setNombre("Piscina");
        dto.setRecursoComun(recurso);
        dto.setHoraInicio(LocalTime.now());
        dto.setHoraFin(LocalTime.now().plusHours(2));
        dto.setNumeroInvitados(2);
        dto.setFechaSolicitud(LocalDate.now());
        dto.setEstadoSolicitud(EstadoSolicitud.RECHAZADA);

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("error template"));

        emailService.enviarSolicitud(destinatario, dto);

        verify(templateEngine, times(1)).process(anyString(), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class)); // no debe intentar enviar
    }
}