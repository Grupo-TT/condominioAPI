package com.condominio;

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
}
