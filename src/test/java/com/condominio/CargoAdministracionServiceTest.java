package com.condominio;

import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.CargoAdministracion;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.CargoAdministracionRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.UserRepository;
import com.condominio.service.implementation.CargoAdministracionService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoAdministracionServiceTest {

    @Mock
    private CargoAdministracionRepository cargoAdministracionRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private UserRepository userRepository;

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

        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(auth.getName()).thenReturn("admin@example.com");


        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("admin@example.com");

        Persona persona = new Persona();
        persona.setPrimerNombre("Juan");
        persona.setPrimerApellido("Pérez");

        when(cargoAdministracionRepository.findById(1L))
                .thenReturn(Optional.of(cargoAdministracion));

        when(userRepository.findUserEntityByEmail("admin@example.com"))
                .thenReturn(userEntity);

        when(personaRepository.findPersonaByUser(userEntity))
                .thenReturn(persona);

        when(cargoAdministracionRepository.save(any(CargoAdministracion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        SuccessResult<CargoAdministracion> result =
                cargoAdministracionService.actualizarCargoAdministracion(1500.0);


        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Cargo de administración actualizado correctamente");
        assertThat(result.data().getValorActual()).isEqualTo(1200.0);
        assertThat(result.data().getNuevoValor()).isEqualTo(1500.0);
        assertThat(result.data().getCorreoActualizador()).isEqualTo("admin@example.com");
        assertThat(result.data().getNombreActualizador()).isEqualTo("Juan Pérez");
        assertThat(result.data().getFechaAplicacion()).isNotNull();

        verify(cargoAdministracionRepository).findById(1L);
        verify(userRepository).findUserEntityByEmail("admin@example.com");
        verify(personaRepository).findPersonaByUser(userEntity);
        verify(cargoAdministracionRepository).save(any(CargoAdministracion.class));
    }

    @Test
    void testSave_ShouldCallRepositorySave() {
        cargoAdministracionService.save(cargoAdministracion);
        verify(cargoAdministracionRepository).save(cargoAdministracion);
    }
}
