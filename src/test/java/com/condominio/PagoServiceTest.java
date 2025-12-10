package com.condominio;

import com.condominio.dto.response.ObligacionDTO;
import com.condominio.dto.response.PagoDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.*;
import com.condominio.service.implementation.PagoService;
import com.condominio.util.events.CreatedPagoEvent;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PagoDetalleRepository pagoDetalleRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private MovimientoRepository movimientoRepository;


    @Mock
    private ObligacionRepository obligacionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PagoService pagoService;

    private Obligacion obligacion;
    private Casa casa;
    private Persona propietario;
    private PagoDTO pagoDTO;

    @BeforeEach
    void setUp() {
        casa = new Casa();
        casa.setId(1L);
        casa.setNumeroCasa(101);

        propietario = new Persona();
        UserEntity user = new UserEntity();
        user.setEmail("propietario@correo.com");
        propietario.setUser(user);

        obligacion = new Obligacion();
        obligacion.setId(1L);
        obligacion.setCasa(casa);
        obligacion.setMonto(250000);
        obligacion.setValorPendiente(250000);
        obligacion.setMotivo("Mantenimiento");
        obligacion.setEstadoPago(EstadoPago.PENDIENTE);

        pagoDTO = new PagoDTO();
        pagoDTO.setIdObligacion(1L);
        pagoDTO.setMontoAPagar(250000); // igual al monto
        pagoDTO.setTipoObligacion(TipoObligacion.ADMINISTRACION);
    }

    //Caso 1: Monto igual a la deuda — pago exitoso
    @Test
    void registrarPago_montoIgual_guardadoYEventoPublicado() {
        when(obligacionRepository.findById(1L)).thenReturn(Optional.of(obligacion));
        when(personaRepository.findPropietarioByCasaId(1L)).thenReturn(Optional.of(propietario));

        SuccessResult<ObligacionDTO> result = pagoService.registrarPago(pagoDTO);
        assertEquals("Pago realizado correctamente", result.message());

        // Verificaciones de persistencia
        verify(pagoRepository, times(1)).save(any(Pago.class));
        verify(pagoDetalleRepository, times(1)).save(any(PagoDetalle.class));
        verify(obligacionRepository, times(1)).save(obligacion);

        // Verificación de evento
        verify(eventPublisher, times(1)).publishEvent(any(CreatedPagoEvent.class));
    }

    //Caso 2: Monto mayor a la deuda — lanza excepción
    @Test
    void registrarPago_montoMayor_lanzaExcepcion() {
        pagoDTO.setMontoAPagar(300000); // mayor que la deuda

        when(obligacionRepository.findById(1L)).thenReturn(Optional.of(obligacion));

        ApiException ex = assertThrows(ApiException.class, () -> pagoService.registrarPago(pagoDTO));

        assertEquals("El valor ingresado supera la deuda actual.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verifyNoInteractions(pagoRepository, pagoDetalleRepository, eventPublisher);
    }

    //Caso 3: Monto menor a la deuda — lanza excepción
    @Test
    void registrarPago_montoMenor_registraPagoParcial() {
        // Arrange
        pagoDTO.setMontoAPagar(100000); // menor que la deuda total (250000)
        pagoDTO.setTipoObligacion(TipoObligacion.ADMINISTRACION); // menor que la deuda total (250000)
        when(obligacionRepository.findById(1L)).thenReturn(Optional.of(obligacion));
        when(personaRepository.findPropietarioByCasaId(1L)).thenReturn(Optional.of(propietario));

        // Act
        SuccessResult<ObligacionDTO> result = pagoService.registrarPago(pagoDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Pago realizado correctamente", result.message());
        ObligacionDTO dto = result.data();
        assertNotNull(dto);

        // Verificar datos del DTO
        assertEquals(obligacion.getId(), dto.getId());
        assertEquals("Mantenimiento", dto.getMotivo());
        assertEquals(100000, dto.getMonto());
        assertEquals("POR_COBRAR", dto.getEstado());
        assertEquals(150000, dto.getSaldo()); // saldo = 250000 - 100000

        // Verificar llamadas a repositorios
        verify(pagoRepository, times(1)).save(any(Pago.class));
        verify(pagoDetalleRepository, times(1)).save(any(PagoDetalle.class));
        verify(obligacionRepository, times(1)).save(obligacion);

        // Verificar que se haya publicado el evento
        verify(eventPublisher, times(1)).publishEvent(any(CreatedPagoEvent.class));
    }

    @Test
    void obtenerFechaUltimoPagoPorCasa_deberiaRetornarFechaCuandoExistePago() {
        // given
        Long idCasa = 1L;
        LocalDate fechaEsperada = LocalDate.of(2025, 10, 22);
        when(pagoDetalleRepository.findFechaUltimoPagoByCasaId(idCasa))
                .thenReturn(Optional.of(fechaEsperada));

        // when
        Optional<LocalDate> resultado = pagoService.obtenerFechaUltimoPagoPorCasa(idCasa);

        // then
        assertTrue(resultado.isPresent());
        assertEquals(fechaEsperada, resultado.get());
        verify(pagoDetalleRepository, times(1)).findFechaUltimoPagoByCasaId(idCasa);
    }

    @Test
    void obtenerFechaUltimoPagoPorCasa_deberiaRetornarVacioCuandoNoHayPagos() {
        // given
        Long idCasa = 2L;
        when(pagoDetalleRepository.findFechaUltimoPagoByCasaId(idCasa))
                .thenReturn(Optional.empty());

        // when
        Optional<LocalDate> resultado = pagoService.obtenerFechaUltimoPagoPorCasa(idCasa);

        // then
        assertTrue(resultado.isEmpty());
        verify(pagoDetalleRepository, times(1)).findFechaUltimoPagoByCasaId(idCasa);
    }
}