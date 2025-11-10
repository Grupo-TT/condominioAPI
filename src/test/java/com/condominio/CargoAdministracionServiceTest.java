package com.condominio;

import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.ActualizacionHelper;
import com.condominio.persistence.model.CargoAdministracion;
import com.condominio.persistence.repository.CargoAdministracionRepository;
import com.condominio.service.implementation.CargoAdministracionService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoAdministracionServiceTest {

    @Mock
    private CargoAdministracionRepository cargoAdministracionRepository;

    @Mock
    private ActualizacionHelper actualizacionHelper;

    @InjectMocks
    private CargoAdministracionService cargoAdministracionService;

    private CargoAdministracion cargoAdministracion;

    @BeforeEach
    void setUp() {
        cargoAdministracion = new CargoAdministracion();
        cargoAdministracion.setId(1L);
        cargoAdministracion.setValorActual(1000.0);
        cargoAdministracion.setNuevoValor(1200.0);
    }

    @Test
    void testActualizarCargoAdministracion_WhenNuevoValorIsZero_ShouldThrowException() {
        when(cargoAdministracionRepository.findById(1L))
                .thenReturn(Optional.of(cargoAdministracion));

        assertThatThrownBy(() -> cargoAdministracionService.actualizarCargoAdministracion(0))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("El nuevo valor no puede ser tan bajo");
    }

    @Test
    void testActualizarCargoAdministracion_WhenCargoNotFound_ShouldThrowException() {
        when(cargoAdministracionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cargoAdministracionService.actualizarCargoAdministracion(1500))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No existe el cargo de administración");

        verify(cargoAdministracionRepository).findById(1L);
    }

    @Test
    void testActualizarCargoAdministracion_WhenSuccessful() {
        // Arrange
        when(cargoAdministracionRepository.findById(1L))
                .thenReturn(Optional.of(cargoAdministracion));

        CargoAdministracion actualizado = new CargoAdministracion();
        actualizado.setValorActual(1200.0);
        actualizado.setNuevoValor(1500.0);
        actualizado.setCorreoActualizador("admin@example.com");
        actualizado.setNombreActualizador("Juan Pérez");


        when(actualizacionHelper.aplicarDatosComunes(cargoAdministracion, 1500.0))
                .thenReturn(actualizado);

        when(cargoAdministracionRepository.save(any(CargoAdministracion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        SuccessResult<CargoAdministracion> result =
                cargoAdministracionService.actualizarCargoAdministracion(1500.0);


        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Cargo de administración actualizado correctamente");
        assertThat(result.data().getNuevoValor()).isEqualTo(1500.0);
        assertThat(result.data().getCorreoActualizador()).isEqualTo("admin@example.com");

        verify(cargoAdministracionRepository).findById(1L);
        verify(actualizacionHelper).aplicarDatosComunes(cargoAdministracion, 1500.0);
        verify(cargoAdministracionRepository).save(any(CargoAdministracion.class));
    }

    @Test
    void testSave_ShouldCallRepositorySave() {
        cargoAdministracionService.save(cargoAdministracion);
        verify(cargoAdministracionRepository).save(cargoAdministracion);
    }
}
