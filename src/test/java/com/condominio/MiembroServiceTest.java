package com.condominio;

import com.condominio.dto.response.MiembrosDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Miembro;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.MiembroRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.implementation.MiembroService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MiembroServiceTest {

    @Mock
    private MiembroRepository miembroRepository;

    @InjectMocks
    private MiembroService miembroService;

    @Mock
    private PersonaRepository personaRepository;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testCountByCasaId_ShouldReturnCorrectValue() {

        Long casaId = 1L;
        when(miembroRepository.countByCasaId(casaId)).thenReturn(3);


        int result = miembroService.countByCasaId(casaId);

        assertThat(result).isEqualTo(3);
        verify(miembroRepository).countByCasaId(casaId);
    }

    @Test
    void testObtenerMiembrosPorCasa_WhenMembersExist() {
        Long casaId = 1L;


        Persona propietario = new Persona();
        propietario.setPrimerNombre("Juan");
        propietario.setPrimerApellido("Pérez");
        propietario.setNumeroDocumento(12345L);
        propietario.setTelefono(3001234567L);
        UserEntity propietarioUser = new UserEntity();
        propietarioUser.setEmail("juan@example.com");
        propietario.setUser(propietarioUser);
        when(personaRepository.findPropietarioByCasaId(casaId)).thenReturn(Optional.of(propietario));


        Persona arrendatario = new Persona();
        arrendatario.setPrimerNombre("Ana");
        arrendatario.setPrimerApellido("Gómez");
        arrendatario.setNumeroDocumento(67890L);
        arrendatario.setTelefono(3011234567L);
        UserEntity arrendatarioUser = new UserEntity();
        arrendatarioUser.setEmail("ana@example.com");
        arrendatario.setUser(arrendatarioUser);
        when(personaRepository.findArrendatarioByCasaId(casaId)).thenReturn(Optional.of(arrendatario));


        Miembro miembro = new Miembro();
        miembro.setNombre("Pedro");
        miembro.setParentesco("Hijo");
        miembro.setNumeroDocumento(11111L);
        miembro.setTelefono(3021234567L);
        when(miembroRepository.findByCasaIdAndEstadoTrue(casaId)).thenReturn(List.of(miembro));


        SuccessResult<List<MiembrosDTO>> result = miembroService.obtenerMiembrosPorCasa(casaId);


        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Miembros encontrados");

        List<MiembrosDTO> miembros = result.data();


        assertThat(miembros.size()).isEqualTo(3);

        verify(personaRepository).findPropietarioByCasaId(casaId);
        verify(personaRepository).findArrendatarioByCasaId(casaId);
        verify(miembroRepository).findByCasaIdAndEstadoTrue(casaId);
    }

    @Test
    void testObtenerMiembrosPorCasa_WhenNoMembers_ShouldThrowException() {
        Long casaId = 1L;


        when(personaRepository.findPropietarioByCasaId(casaId)).thenReturn(Optional.empty());
        when(personaRepository.findArrendatarioByCasaId(casaId)).thenReturn(Optional.empty());
        when(miembroRepository.findByCasaIdAndEstadoTrue(casaId)).thenReturn(List.of());


        assertThatThrownBy(() -> miembroService.obtenerMiembrosPorCasa(casaId))
                .isInstanceOf(ApiException.class)
                .hasMessage("No hay miembros registrados para esta casa")
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(personaRepository).findPropietarioByCasaId(casaId);
        verify(personaRepository).findArrendatarioByCasaId(casaId);
        verify(miembroRepository).findByCasaIdAndEstadoTrue(casaId);
    }
}