package com.condominio;

import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.ActualizacionHelper;
import com.condominio.persistence.model.TasaDeInteres;
import com.condominio.persistence.repository.TasaDeInteresRepository;
import com.condominio.service.implementation.TasaDeInteresService;
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
class TasaDeInteresServiceTest {

    @Mock
    private TasaDeInteresRepository tasaDeInteresRepository;

    @Mock
    private ActualizacionHelper actualizacionHelper;

    @InjectMocks
    private TasaDeInteresService tasaDeInteresService;

    private TasaDeInteres tasaDeInteres;

    @BeforeEach
    void setUp() {
        tasaDeInteres = new TasaDeInteres();
        tasaDeInteres.setId(1L);
        tasaDeInteres.setValorActual(5.0);
        tasaDeInteres.setNuevoValor(10.0);
    }

    @Test
    void testActualizarTasaDeInteres_WhenValorInvalido_ShouldThrowException() {
        assertThatThrownBy(() -> tasaDeInteresService.actualizarTasaDeInteres(200))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Ingrese un nuevo valor válido");
    }

    @Test
    void testActualizarTasaDeInteres_WhenNoExisteTasa_ShouldThrowException() {
        when(tasaDeInteresRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tasaDeInteresService.actualizarTasaDeInteres(8))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No existe una tasa de interes");

        verify(tasaDeInteresRepository).findById(1L);
    }

    @Test
    void testActualizarTasaDeInteres_WhenSuccessful() {
        when(tasaDeInteresRepository.findById(1L)).thenReturn(Optional.of(tasaDeInteres));


        TasaDeInteres tasaActualizada = new TasaDeInteres();
        tasaActualizada.setId(1L);
        tasaActualizada.setValorActual(10.0);
        tasaActualizada.setNuevoValor(0.15);
        tasaActualizada.setCorreoActualizador("admin@example.com");
        tasaActualizada.setNombreActualizador("Laura Gómez");
        tasaActualizada.setFechaAplicacion(OffsetDateTime.now());

        when(actualizacionHelper.aplicarDatosComunes(tasaDeInteres, 15.0, true))
                .thenReturn(tasaActualizada);

        when(tasaDeInteresRepository.save(any(TasaDeInteres.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SuccessResult<TasaDeInteres> result =
                tasaDeInteresService.actualizarTasaDeInteres(15.0);

        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Tasa de interes actualizada correctamente");
        assertThat(result.data().getNuevoValor()).isEqualTo(0.15);

        verify(tasaDeInteresRepository).findById(1L);
        verify(actualizacionHelper).aplicarDatosComunes(tasaDeInteres, 15.0, true);
        verify(tasaDeInteresRepository).save(any(TasaDeInteres.class));
    }

    @Test
    void testSave_ShouldCallRepositorySave() {
        tasaDeInteresService.save(tasaDeInteres);
        verify(tasaDeInteresRepository).save(tasaDeInteres);
    }
}

