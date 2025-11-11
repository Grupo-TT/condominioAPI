package com.condominio;

import com.condominio.dto.request.MiembroRegistroDTO;
import com.condominio.dto.response.MiembrosDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.Miembro;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.CasaRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MiembroServiceTest {

    @Mock
    private MiembroRepository miembroRepository;

    @InjectMocks
    private MiembroService miembroService;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private CasaRepository casaRepository;

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

    @Test
    void testCrearMiembro_ShouldCreateSuccessfully() {

        MiembroRegistroDTO dto = new MiembroRegistroDTO();
        dto.setIdCasa(1L);
        dto.setNombre("Carlos");
        dto.setNumeroDocumento(12345L);
        dto.setTelefono(3001112233L);
        dto.setParentesco("Hijo");

        Casa newCasa = new Casa();
        newCasa.setId(1L);


        when(casaRepository.findById(1L)).thenReturn(Optional.of(newCasa));
        when(miembroRepository.existsByNumeroDocumento(12345L)).thenReturn(false);


        SuccessResult<Void> result = miembroService.crearMiembro(dto);


        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Miembro registrado correctamente");


        verify(miembroRepository).existsByNumeroDocumento(12345L);
        verify(miembroRepository).save(any(Miembro.class));
    }

    @Test
    void testCrearMiembro_ShouldThrowExceptionWhenDocumentoExists() {
        MiembroRegistroDTO dto = new MiembroRegistroDTO();
        dto.setIdCasa(1L);
        dto.setNombre("Carlos");
        dto.setNumeroDocumento(12345L);
        dto.setTelefono(3001112233L);
        dto.setParentesco("Hijo");

        Casa newCasa = new Casa();
        newCasa.setId(1L);


        when(casaRepository.findById(1L)).thenReturn(Optional.of(newCasa));
        when(miembroRepository.existsByNumeroDocumento(12345L)).thenReturn(true);


        ApiException thrown = assertThrows(ApiException.class, () -> miembroService.crearMiembro(dto));

        assertThat(thrown.getMessage()).isEqualTo("El numero  de documento ya se  encuentra registrado");

        verify(miembroRepository).existsByNumeroDocumento(12345L);
        verify(miembroRepository, never()).save(any(Miembro.class));
    }
    @Test
    void testCrearMiembro_WhenCasaDoesNotExist_ShouldThrowApiException() {
        // given
        MiembroRegistroDTO dto = new MiembroRegistroDTO();
        dto.setIdCasa(99L);
        dto.setNombre("Carlos");
        dto.setNumeroDocumento(123456L);
        dto.setTelefono(3001112222L);
        dto.setParentesco("Hermano");


        when(casaRepository.findById(99L)).thenReturn(Optional.empty());


        ApiException thrown = assertThrows(ApiException.class, () -> miembroService.crearMiembro(dto));

        assertThat(thrown.getMessage()).isEqualTo("La casa con id 99 no existe");
        assertThat(thrown.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);


        verify(miembroRepository, never()).save(any(Miembro.class));
    }
}