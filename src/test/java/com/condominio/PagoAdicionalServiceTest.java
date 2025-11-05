package com.condominio;

import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.ActualizacionHelper;
import com.condominio.persistence.model.PagoAdicional;
import com.condominio.persistence.repository.PagoAdicionalRepository;
import com.condominio.service.implementation.PagoAdicionalService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.OffsetDateTime;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoAdicionalServiceTest {

    @Mock
    private PagoAdicionalRepository pagoAdicionalRepository;

    @Mock
    private ActualizacionHelper actualizacionHelper;

    @InjectMocks
    private PagoAdicionalService pagoAdicionalService;

    private PagoAdicional pagoAdicional;

    @BeforeEach
    void setUp() {
        pagoAdicional = new PagoAdicional();
        pagoAdicional.setId(1L);
        pagoAdicional.setValorActual(1000.0);
        pagoAdicional.setNuevoValor(1200.0);
    }

    @Test
    void testActualizarPagoAdicional_WhenValorInvalido_ShouldThrowException() {
        assertThatThrownBy(() -> pagoAdicionalService.actualizarPagoAdicional(0))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Ingrese un nuevo valor válido");
    }

    @Test
    void testActualizarPagoAdicional_WhenNoExistePago_ShouldThrowException() {
        when(pagoAdicionalRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagoAdicionalService.actualizarPagoAdicional(2000))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No existe un pago adicional registrado");

        verify(pagoAdicionalRepository).findById(1L);
    }

    @Test
    void testActualizarPagoAdicional_WhenSuccessful() {
        when(pagoAdicionalRepository.findById(1L)).thenReturn(Optional.of(pagoAdicional));

        PagoAdicional actualizado = new PagoAdicional();
        actualizado.setId(1L);
        actualizado.setValorActual(1200.0);
        actualizado.setNuevoValor(1500.0);
        actualizado.setCorreoActualizador("admin@example.com");
        actualizado.setNombreActualizador("Carlos Ruiz");
        actualizado.setFechaAplicacion(OffsetDateTime.now());

        when(actualizacionHelper.aplicarDatosComunes(pagoAdicional, 1500.0))
                .thenReturn(actualizado);

        when(pagoAdicionalRepository.save(any(PagoAdicional.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SuccessResult<PagoAdicional> result =
                pagoAdicionalService.actualizarPagoAdicional(1500.0);

        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Pago adicional actualizado correctamente");
        assertThat(result.data().getValorActual()).isEqualTo(1200.0);
        assertThat(result.data().getNuevoValor()).isEqualTo(1500.0);
        assertThat(result.data().getCorreoActualizador()).isEqualTo("admin@example.com");
        assertThat(result.data().getNombreActualizador()).isEqualTo("Carlos Ruiz");
        assertThat(result.data().getFechaAplicacion()).isNotNull();

        verify(pagoAdicionalRepository).findById(1L);
        verify(actualizacionHelper).aplicarDatosComunes(pagoAdicional, 1500.0);
        verify(pagoAdicionalRepository).save(any(PagoAdicional.class));
    }

    @Test
    void testSave_ShouldCallRepositorySave() {
        pagoAdicionalService.save(pagoAdicional);
        verify(pagoAdicionalRepository).save(pagoAdicional);
    }
}