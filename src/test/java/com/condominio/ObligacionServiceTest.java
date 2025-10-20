package com.condominio;

import com.condominio.dto.response.EstadoCuentaDTO;
import com.condominio.dto.response.PersonaSimpleDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.persistence.repository.ObligacionRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.implementation.EmailService;
import com.condominio.service.implementation.ObligacionService;
import com.condominio.service.implementation.PdfService;
import com.condominio.service.implementation.PersonaService;
import com.condominio.util.exception.ApiException;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ObligacionServiceTest {

    @Mock
    private ObligacionRepository obligacionRepository;

    @Mock
    private CasaRepository casaRepository;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private ObligacionService obligacionService;

    @Mock
    private PdfService pdfService;

    @Mock
    private EmailService emailService;

    @Mock
    private PersonaService personaService;

    private Casa casa;
    private Persona propietario;
    private UserEntity user;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        casa = new Casa();
        casa.setId(1L);
        casa.setNumeroCasa(101);

        user = new UserEntity();
        user.setEmail("propietario@mail.com");

        propietario = new Persona();
        propietario.setId(1L);
        propietario.setPrimerNombre("Ana");
        propietario.setPrimerApellido("Gómez");
        propietario.setTelefono(3112234567L);
        propietario.setUser(user);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testEstadoDeCuentaCasa_WhenCasaExistsAndHasPendientes() {
        // given
        when(casaRepository.findById(1L)).thenReturn(Optional.of(casa));
        when(personaRepository.findPropietarioByCasaId(1L)).thenReturn(Optional.of(propietario));

        Obligacion obligacion1 = new Obligacion();
        obligacion1.setId(1L);
        obligacion1.setMonto(500);
        obligacion1.setEstadoPago(EstadoPago.PENDIENTE);
        obligacion1.setFechaGenerada(LocalDate.now());

        Obligacion obligacion2 = new Obligacion();
        obligacion2.setId(2L);
        obligacion2.setMonto(300);
        obligacion2.setEstadoPago(EstadoPago.CONDONADO);

        when(obligacionRepository.findByCasaId(1L))
                .thenReturn(List.of(obligacion1, obligacion2));

        // when
        SuccessResult<EstadoCuentaDTO> result = obligacionService.estadoDeCuentaCasa(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Estado de cuenta obtenido correctamente");
        assertThat(result.data().getNumeroCasa()).isEqualTo(101);
        assertThat(result.data().getSaldoPendienteTotal()).isEqualTo(500);
        assertThat(result.data().getDeudasActivas()).hasSize(1);

        PersonaSimpleDTO propietarioDTO = result.data().getPropietario();
        assertThat(propietarioDTO.getNombreCompleto()).isEqualTo("Ana Gómez");
        assertThat(propietarioDTO.getCorreo()).isEqualTo("propietario@mail.com");
        assertThat(propietarioDTO.getTelefono()).isEqualTo(3112234567L);

        verify(casaRepository).findById(1L);
        verify(personaRepository).findPropietarioByCasaId(1L);
        verify(obligacionRepository).findByCasaId(1L);
    }

    @Test
    void testEstadoDeCuentaCasa_WhenCasaDoesNotExist_ShouldThrowApiException() {
        when(casaRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> obligacionService.estadoDeCuentaCasa(99L));

        assertThat(exception.getMessage()).isEqualTo("No se encontró una casa con el ID 99");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);

        verify(casaRepository).findById(99L);
        verifyNoInteractions(personaRepository);
        verifyNoInteractions(obligacionRepository);
    }
    @Test
    void generarPazYSalvo_CuandoTodoSaleBien() throws Exception {

        Long idCasa = 1L;
        Persona persona = new Persona();
        UserEntity user = new UserEntity();
        user.setEmail("test@mail.com");
        persona.setUser(user);
        persona.setPrimerNombre("Ana");
        persona.setPrimerApellido("Gómez");

        byte[] fakePdf = new byte[]{1, 2, 3};

        ObligacionService spyService = Mockito.spy(obligacionService);
        doReturn(true).when(spyService).estaAlDia(idCasa);
        when(personaService.obtenerSolicitantePorCasa(idCasa)).thenReturn(persona);
        when(pdfService.generarPdf(anyString(), eq(idCasa), anyString())).thenReturn(fakePdf);


        ResponseEntity<?> response = spyService.generarPazYSalvo(idCasa);


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat((byte[]) response.getBody()).isEqualTo(fakePdf);

        verify(pdfService).generarPdf(anyString(), eq(idCasa), anyString());
        verify(emailService).enviarPazYSalvo(eq("test@mail.com"), eq(fakePdf), anyString());
    }
    @Test
    void generarPazYSalvo_CuandoTieneDeudasDebeLanzarExcepcion() {
        Long idCasa = 1L;

        ObligacionService spyService = Mockito.spy(obligacionService);
        doReturn(false).when(spyService).estaAlDia(idCasa);

        ApiException ex = assertThrows(ApiException.class, () -> spyService.generarPazYSalvo(idCasa));

        assertThat(ex.getMessage()).contains("deudas pendientes");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(pdfService);
        verifyNoInteractions(emailService);
    }

    @Test
    void generarPazYSalvo_SiFallaGeneracionPdfDebeLanzarApiException() throws Exception {
        Long idCasa = 1L;
        Persona persona = new Persona();
        UserEntity user = new UserEntity();
        user.setEmail("fail@mail.com");
        persona.setUser(user);
        persona.setPrimerNombre("Carlos");
        persona.setPrimerApellido("Pérez");

        ObligacionService spyService = Mockito.spy(obligacionService);
        doReturn(true).when(spyService).estaAlDia(idCasa);
        when(personaService.obtenerSolicitantePorCasa(idCasa)).thenReturn(persona);
        when(pdfService.generarPdf(anyString(), eq(idCasa), anyString())).thenThrow(new IOException("Error generando PDF"));

        ApiException ex = assertThrows(ApiException.class, () -> spyService.generarPazYSalvo(idCasa));

        assertThat(ex.getMessage()).contains("Error al generar el PDF");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(pdfService).generarPdf(anyString(), eq(idCasa), anyString());
        verifyNoInteractions(emailService);
    }

    @Test
    void generarPazYSalvo_SiFallaEnvioCorreoNoLanzaExcepcion() throws Exception {
        Long idCasa = 1L;
        Persona persona = new Persona();
        UserEntity user = new UserEntity();
        user.setEmail("fail@mail.com");
        persona.setUser(user);
        persona.setPrimerNombre("Carlos");
        persona.setPrimerApellido("Pérez");

        byte[] fakePdf = new byte[]{9, 9, 9};

        ObligacionService spyService = Mockito.spy(obligacionService);
        doReturn(true).when(spyService).estaAlDia(idCasa);
        when(personaService.obtenerSolicitantePorCasa(idCasa)).thenReturn(persona);
        when(pdfService.generarPdf(anyString(), eq(idCasa), anyString())).thenReturn(fakePdf);
        doThrow(new MessagingException("SMTP error")).when(emailService)
                .enviarPazYSalvo(eq("fail@mail.com"), eq(fakePdf), anyString());

        ResponseEntity<?> response = spyService.generarPazYSalvo(idCasa);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((byte[]) response.getBody()).isEqualTo(fakePdf);

        verify(emailService).enviarPazYSalvo(eq("fail@mail.com"), eq(fakePdf), anyString());
    }
}
