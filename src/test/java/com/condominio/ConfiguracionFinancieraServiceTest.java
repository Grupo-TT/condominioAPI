package com.condominio;

import com.condominio.dto.response.ConfiguracionListaDTO;
import com.condominio.dto.response.ConfigItemDTO;
import com.condominio.persistence.model.CargoAdministracion;
import com.condominio.persistence.model.PagoAdicional;
import com.condominio.persistence.model.TasaDeInteres;
import com.condominio.persistence.repository.CargoAdministracionRepository;
import com.condominio.persistence.repository.PagoAdicionalRepository;
import com.condominio.persistence.repository.TasaDeInteresRepository;
import com.condominio.service.implementation.ConfiguracionFinancieraService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ConfiguracionFinancieraServiceTest {

    @Mock
    private PagoAdicionalRepository pagoAdicionalRepository;

    @Mock
    private TasaDeInteresRepository tasaDeInteresRepository;

    @Mock
    private CargoAdministracionRepository cargoAdministracionRepository;

    @InjectMocks
    private ConfiguracionFinancieraService configuracionFinancieraService;

    private PagoAdicional pagoAdicional;
    private TasaDeInteres tasaDeInteres;
    private CargoAdministracion cargoAdministracion;

    @BeforeEach
    void setUp() {
        pagoAdicional = new PagoAdicional();
        pagoAdicional.setId(1L);
        pagoAdicional.setNuevoValor(2000.0);

        tasaDeInteres = new TasaDeInteres();
        tasaDeInteres.setId(1L);
        tasaDeInteres.setNuevoValor(0.08);

        cargoAdministracion = new CargoAdministracion();
        cargoAdministracion.setId(1L);
        cargoAdministracion.setNuevoValor(100.0);
    }

    @Test
    void testObtenerConfiguracion_WhenAllExist_ShouldReturnDTO() {
        when(pagoAdicionalRepository.findById(1L)).thenReturn(Optional.of(pagoAdicional));
        when(tasaDeInteresRepository.findById(1L)).thenReturn(Optional.of(tasaDeInteres));
        when(cargoAdministracionRepository.findById(1L)).thenReturn(Optional.of(cargoAdministracion));

        ConfiguracionListaDTO result = configuracionFinancieraService.obtenerConfiguracion();

        assertThat(result).isNotNull();
        assertThat(result.getConfiguraciones()).hasSize(3);

        assertThat(result.getConfiguraciones())
                .extracting(ConfigItemDTO::getTipo)
                .containsExactly("Pago adicional", "Tasa de interés", "Cargo de administración");

        assertThat(result.getConfiguraciones())
                .extracting(ConfigItemDTO::getValor)
                .containsExactly(2000.0, 8.0, 100.0);

        verify(pagoAdicionalRepository).findById(1L);
        verify(tasaDeInteresRepository).findById(1L);
        verify(cargoAdministracionRepository).findById(1L);
    }

    @Test
    void testObtenerConfiguracion_WhenPagoAdicionalNotFound_ShouldThrowException() {
        when(pagoAdicionalRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configuracionFinancieraService.obtenerConfiguracion())
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No existe pago adicional");
    }

    @Test
    void testObtenerConfiguracion_WhenTasaNotFound_ShouldThrowException() {
        when(pagoAdicionalRepository.findById(1L)).thenReturn(Optional.of(pagoAdicional));
        when(tasaDeInteresRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configuracionFinancieraService.obtenerConfiguracion())
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No existe tasa de interes");
    }

    @Test
    void testObtenerConfiguracion_WhenCargoNotFound_ShouldThrowException() {
        when(pagoAdicionalRepository.findById(1L)).thenReturn(Optional.of(pagoAdicional));
        when(tasaDeInteresRepository.findById(1L)).thenReturn(Optional.of(tasaDeInteres));
        when(cargoAdministracionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configuracionFinancieraService.obtenerConfiguracion())
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No existe cargo de administración");
    }
}

