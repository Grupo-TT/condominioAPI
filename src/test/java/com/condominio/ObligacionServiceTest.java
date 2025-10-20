package com.condominio;

import com.condominio.dto.request.MultaActualizacionDTO;
import com.condominio.dto.request.MultaRegistroDTO;
import com.condominio.dto.response.EstadoCuentaDTO;
import com.condominio.dto.response.PersonaSimpleDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.persistence.repository.ObligacionRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.implementation.ObligacionService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

    private Casa casa;
    private Persona propietario;
    private Obligacion obligacion;
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

        obligacion = Obligacion.builder()
                .id(1L)
                .monto(20000)
                .motivo("Anterior")
                .casa(casa)
                .tipoObligacion(TipoObligacion.MULTA)
                .tipoPago(TipoPago.DINERO)
                .estadoPago(EstadoPago.POR_COBRAR)
                .build();

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
    void save_DebeGuardarMultaCuandoCasaExiste() {
        MultaRegistroDTO dto = new MultaRegistroDTO(1L, 20000, "Basura mal dispuesta");

        when(casaRepository.findById(1L)).thenReturn(Optional.of(casa));
        when(obligacionRepository.save(any(Obligacion.class))).thenReturn(obligacion);

        SuccessResult<Obligacion> result = obligacionService.save(dto);

        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Multa registrada correctamente");
        assertThat(result.data().getTipoObligacion()).isEqualTo(TipoObligacion.MULTA);
        assertThat(result.data().getTipoPago()).isEqualTo(TipoPago.DINERO);
        verify(casaRepository).findById(1L);
        verify(obligacionRepository).save(any(Obligacion.class));
    }

    @Test
    void save_DebeFallarCuandoCasaNoExiste() {
        MultaRegistroDTO dto = new MultaRegistroDTO(99L, 15000, "Basura mal dispuesta");

        when(casaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> obligacionService.save(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Casa no encontrada con ID: 99");

        verify(obligacionRepository, never()).save(any());
    }

    @Test
    void update_DebeActualizarMultaCorrectamente() {
        MultaActualizacionDTO dto = new MultaActualizacionDTO(1L, 30000, "Pago tardío", TipoPago.DINERO);

        when(obligacionRepository.findById(1L)).thenReturn(Optional.of(obligacion));
        when(casaRepository.findById(1L)).thenReturn(Optional.of(casa));
        when(obligacionRepository.save(any(Obligacion.class))).thenReturn(obligacion);

        SuccessResult<Obligacion> result = obligacionService.update(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Multa actualizada correctamente");
        assertThat(result.data().getTipoPago()).isEqualTo(TipoPago.DINERO);
        verify(obligacionRepository).save(any(Obligacion.class));
    }

    @Test
    void update_DebeFallarSiMultaNoExiste() {
        MultaActualizacionDTO dto = new MultaActualizacionDTO(1L, 30000, "Pago tardío", TipoPago.DINERO);

        when(obligacionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> obligacionService.update(1L, dto))
                .isInstanceOf(ApiException.class)
                .hasMessage("La multa no existe");

        verify(casaRepository, never()).findById(any());
        verify(obligacionRepository, never()).save(any());
    }

    @Test
    void update_DebeFallarSiCasaNoExiste() {
        MultaActualizacionDTO dto = new MultaActualizacionDTO(99L, 30000, "Pago tardío", TipoPago.DINERO);

        when(obligacionRepository.findById(1L)).thenReturn(Optional.of(obligacion));
        when(casaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> obligacionService.update(1L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Casa no encontrada con ID: 99");

        verify(obligacionRepository, never()).save(any());
    }

    @Test
    void update_NoDebeCambiarTipoPagoSiNoSeEnvia() {
        MultaActualizacionDTO dto = new MultaActualizacionDTO(1L, 50000, "Pago tardío", null);

        when(obligacionRepository.findById(1L)).thenReturn(Optional.of(obligacion));
        when(casaRepository.findById(1L)).thenReturn(Optional.of(casa));
        when(obligacionRepository.save(any(Obligacion.class))).thenReturn(obligacion);

        obligacionService.update(1L, dto);

        // Verificamos que el tipo de pago original (DINERO) se conserve
        assertThat(obligacion.getTipoPago()).isEqualTo(TipoPago.DINERO);
    }
}
