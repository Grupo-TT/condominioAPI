package com.condominio;

import com.condominio.dto.request.MiembroActualizacionDTO;
import com.condominio.dto.request.MiembroRegistroDTO;
import com.condominio.dto.response.MiembrosDTO;
import com.condominio.dto.response.MiembrosDatosDTO;
import com.condominio.dto.response.MiembrosResponseDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.Miembro;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.TipoDocumento;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.persistence.repository.MiembroRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.implementation.MiembroService;
import com.condominio.util.exception.ApiException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static com.condominio.util.constants.AppConstants.DOCUMENTO_REPETIDO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        // Given
        Long casaId = 1L;
        when(miembroRepository.countByCasaId(casaId)).thenReturn(3);

        // When
        int result = miembroService.countByCasaId(casaId);

        // Then
        assertThat(result).isEqualTo(3);
        verify(miembroRepository).countByCasaId(casaId);
    }

    @Test
    void testObtenerMiembrosPorCasa_WhenMembersExist() {
        // Given
        Long casaId = 1L;

        Persona propietario = new Persona();
        propietario.setPrimerNombre("Juan");
        propietario.setPrimerApellido("Pérez");
        propietario.setNumeroDocumento(12345L);
        propietario.setTipoDocumento(TipoDocumento.CEDULA_DE_CIUDADANIA);
        propietario.setTelefono(3001234567L);
        UserEntity propietarioUser = new UserEntity();
        propietarioUser.setEmail("juan@example.com");
        propietario.setUser(propietarioUser);
        when(personaRepository.findPropietarioByCasaId(casaId)).thenReturn(Optional.of(propietario));

        Persona arrendatario = new Persona();
        arrendatario.setPrimerNombre("Ana");
        arrendatario.setPrimerApellido("Gómez");
        arrendatario.setNumeroDocumento(67890L);
        arrendatario.setTipoDocumento(TipoDocumento.CEDULA_DE_EXTRANJERIA);
        arrendatario.setTelefono(3011234567L);
        UserEntity arrendatarioUser = new UserEntity();
        arrendatarioUser.setEmail("ana@example.com");
        arrendatario.setUser(arrendatarioUser);
        when(personaRepository.findArrendatarioByCasaId(casaId)).thenReturn(Optional.of(arrendatario));

        Miembro miembro = new Miembro();
        miembro.setNombre("Pedro");
        miembro.setParentesco("Hijo");
        miembro.setNumeroDocumento(11111L);
        miembro.setTipoDocumento(TipoDocumento.TARJETA_DE_IDENTIDAD);
        miembro.setTelefono(3021234567L);
        when(miembroRepository.findByCasaIdAndEstadoTrue(casaId)).thenReturn(List.of(miembro));

        // When
        SuccessResult<List<MiembrosDTO>> result = miembroService.obtenerMiembrosPorCasa(casaId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Miembros encontrados");

        List<MiembrosDTO> miembros = result.data();
        assertThat(miembros.size()).isEqualTo(3);

        assertThat(miembros).extracting(MiembrosDTO::getTipoDocumento)
                .contains(TipoDocumento.CEDULA_DE_CIUDADANIA, TipoDocumento.CEDULA_DE_EXTRANJERIA, TipoDocumento.TARJETA_DE_IDENTIDAD);

        verify(personaRepository).findPropietarioByCasaId(casaId);
        verify(personaRepository).findArrendatarioByCasaId(casaId);
        verify(miembroRepository).findByCasaIdAndEstadoTrue(casaId);
    }

    @Test
    void testObtenerMiembrosPorCasa_WhenNoMembers_ShouldThrowException() {
        // Given
        Long casaId = 1L;
        when(personaRepository.findPropietarioByCasaId(casaId)).thenReturn(Optional.empty());
        when(personaRepository.findArrendatarioByCasaId(casaId)).thenReturn(Optional.empty());
        when(miembroRepository.findByCasaIdAndEstadoTrue(casaId)).thenReturn(List.of());

        // When & Then
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
        // Given
        MiembroRegistroDTO dto = new MiembroRegistroDTO();
        dto.setIdCasa(1L);
        dto.setNombre("Carlos");
        dto.setNumeroDocumento(12345L);
        dto.setTipoDocumento(TipoDocumento.CEDULA_DE_CIUDADANIA);
        dto.setTelefono(3001112233L);
        dto.setParentesco("Hijo");

        Casa newCasa = new Casa();
        newCasa.setId(1L);

        when(casaRepository.findById(1L)).thenReturn(Optional.of(newCasa));
        when(miembroRepository.existsByNumeroDocumento(12345L)).thenReturn(false);
        when(personaRepository.existsByNumeroDocumento(12345L)).thenReturn(false);

        // When
        SuccessResult<Void> result = miembroService.crearMiembro(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Miembro registrado correctamente");

        ArgumentCaptor<Miembro> miembroCaptor = ArgumentCaptor.forClass(Miembro.class);
        verify(miembroRepository).save(miembroCaptor.capture());
        Miembro savedMiembro = miembroCaptor.getValue();

        assertThat(savedMiembro.getNombre()).isEqualTo("Carlos");
        assertThat(savedMiembro.getNumeroDocumento()).isEqualTo(12345L);
        assertThat(savedMiembro.getTipoDocumento()).isEqualTo(TipoDocumento.CEDULA_DE_CIUDADANIA);
        assertThat(savedMiembro.getCasa()).isEqualTo(newCasa);

        verify(miembroRepository).existsByNumeroDocumento(12345L);
        verify(personaRepository).existsByNumeroDocumento(12345L);
    }

    @Test
    void testCrearMiembro_WhenMiembroDocumentoExists_ShouldThrowException() {
        // Given
        MiembroRegistroDTO dto = new MiembroRegistroDTO();
        dto.setIdCasa(1L);
        dto.setNumeroDocumento(12345L);

        Casa newCasa = new Casa();
        newCasa.setId(1L);

        when(casaRepository.findById(1L)).thenReturn(Optional.of(newCasa));
        when(miembroRepository.existsByNumeroDocumento(12345L)).thenReturn(true);

        // When & Then
        ApiException thrown = assertThrows(ApiException.class, () -> miembroService.crearMiembro(dto));
        assertThat(thrown.getMessage()).isEqualTo("El numero  de documento ya se  encuentra registrado");

        verify(miembroRepository).existsByNumeroDocumento(12345L);
        verify(personaRepository, never()).existsByNumeroDocumento(any());
        verify(miembroRepository, never()).save(any(Miembro.class));
    }

    @Test
    void testCrearMiembro_WhenPersonaDocumentoExists_ShouldThrowException() {
        // Given
        MiembroRegistroDTO dto = new MiembroRegistroDTO();
        dto.setIdCasa(1L);
        dto.setNumeroDocumento(12345L);

        Casa newCasa = new Casa();
        newCasa.setId(1L);

        when(casaRepository.findById(1L)).thenReturn(Optional.of(newCasa));
        when(miembroRepository.existsByNumeroDocumento(12345L)).thenReturn(false);
        when(personaRepository.existsByNumeroDocumento(12345L)).thenReturn(true);

        // When & Then
        ApiException thrown = assertThrows(ApiException.class, () -> miembroService.crearMiembro(dto));
        assertThat(thrown.getMessage()).isEqualTo("El numero  de documento ya se  encuentra registrado");

        verify(miembroRepository).existsByNumeroDocumento(12345L);
        verify(personaRepository).existsByNumeroDocumento(12345L);
        verify(miembroRepository, never()).save(any(Miembro.class));
    }

    @Test
    void testCrearMiembro_WhenCasaDoesNotExist_ShouldThrowApiException() {
        // Given
        MiembroRegistroDTO dto = new MiembroRegistroDTO();
        dto.setIdCasa(99L);
        when(casaRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        ApiException thrown = assertThrows(ApiException.class, () -> miembroService.crearMiembro(dto));
        assertThat(thrown.getMessage()).isEqualTo("La casa con id 99 no existe");
        assertThat(thrown.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(miembroRepository, never()).save(any(Miembro.class));
    }

    @Test
    void testListarMiembrosPorCasa_ShouldReturnMappedDtos() {
        // Given
        Long casaId = 1L;
        Casa newCasa = new Casa();
        newCasa.setId(casaId);

        Miembro miembro1 = new Miembro();
        miembro1.setId(10L);
        miembro1.setCasa(newCasa);
        miembro1.setNombre("Juan Pérez");
        miembro1.setNumeroDocumento(12345L);
        miembro1.setTipoDocumento(TipoDocumento.CEDULA_DE_CIUDADANIA);
        miembro1.setTelefono(3001112222L);
        miembro1.setParentesco("Hijo");
        miembro1.setEstado(true);

        Miembro miembro2 = new Miembro();
        miembro2.setId(11L);
        miembro2.setCasa(newCasa);
        miembro2.setNombre("Ana Gómez");
        miembro2.setNumeroDocumento(67890L);
        miembro2.setTipoDocumento(TipoDocumento.TARJETA_DE_IDENTIDAD);
        miembro2.setTelefono(3003334444L);
        miembro2.setParentesco("Esposa");
        miembro2.setEstado(false);

        when(miembroRepository.findByCasaId(casaId)).thenReturn(List.of(miembro1, miembro2));

        // When
        List<MiembrosDatosDTO> result = miembroService.listarMiembrosPorCasa(casaId);

        // Then
        assertThat(result.size()).isEqualTo(2);

        MiembrosDatosDTO dto1 = result.getFirst();
        assertThat(dto1.getId()).isEqualTo(10L);
        assertThat(dto1.getNombre()).isEqualTo("Juan Pérez");
        assertThat(dto1.getTipoDocumento()).isEqualTo(TipoDocumento.CEDULA_DE_CIUDADANIA);
        assertThat(dto1.getEstado()).isTrue();

        MiembrosDatosDTO dto2 = result.get(1);
        assertThat(dto2.getId()).isEqualTo(11L);
        assertThat(dto2.getNombre()).isEqualTo("Ana Gómez");
        assertThat(dto2.getTipoDocumento()).isEqualTo(TipoDocumento.TARJETA_DE_IDENTIDAD);
        assertThat(dto2.getEstado()).isFalse();

        verify(miembroRepository).findByCasaId(casaId);
    }

    @Test
    void testActualizarMiembro_ShouldUpdateSuccessfully() {
        // Given
        Long idMiembro = 1L;
        Long casaUsuarioId = 10L;

        Miembro miembroExistente = new Miembro();
        miembroExistente.setId(idMiembro);
        miembroExistente.setEstado(true);
        Casa casa = new Casa();
        casa.setId(casaUsuarioId);
        miembroExistente.setCasa(casa);

        MiembroActualizacionDTO dto = new MiembroActualizacionDTO();
        dto.setNombre("Carlos López");
        dto.setNumeroDocumento(99999L);
        dto.setTipoDocumento(TipoDocumento.CEDULA_DE_EXTRANJERIA);
        dto.setTelefono(3012223333L);
        dto.setParentesco("Padre");

        when(miembroRepository.findById(idMiembro)).thenReturn(Optional.of(miembroExistente));
        when(miembroRepository.existsByNumeroDocumentoAndIdNot(99999L, idMiembro)).thenReturn(false);
        when(personaRepository.existsByNumeroDocumento(99999L)).thenReturn(false);

        // When
        SuccessResult<Void> result = miembroService.actualizarMiembro(idMiembro, dto, casaUsuarioId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Miembro actualizado correctamente");

        verify(miembroRepository).findById(idMiembro);
        verify(miembroRepository).existsByNumeroDocumentoAndIdNot(99999L, idMiembro);
        verify(personaRepository).existsByNumeroDocumento(99999L);
        verify(miembroRepository).save(miembroExistente);

        assertThat(miembroExistente.getNombre()).isEqualTo("Carlos López");
        assertThat(miembroExistente.getNumeroDocumento()).isEqualTo(99999L);
        assertThat(miembroExistente.getTipoDocumento()).isEqualTo(TipoDocumento.CEDULA_DE_EXTRANJERIA);
        assertThat(miembroExistente.getTelefono()).isEqualTo(3012223333L);
        assertThat(miembroExistente.getParentesco()).isEqualTo("Padre");
    }

    @Test
    void testActualizarMiembro_WhenMiembroIsInactive_ShouldThrowException() {
        // Given
        Long idMiembro = 1L;
        Long casaUsuarioId = 10L;
        MiembroActualizacionDTO dto = new MiembroActualizacionDTO();

        Miembro miembroInactivo = new Miembro();
        miembroInactivo.setId(idMiembro);
        miembroInactivo.setEstado(false); // Inactive member
        Casa casa = new Casa();
        casa.setId(casaUsuarioId);
        miembroInactivo.setCasa(casa);

        when(miembroRepository.findById(idMiembro)).thenReturn(Optional.of(miembroInactivo));

        // When & Then
        ApiException thrown = assertThrows(ApiException.class,
                () -> miembroService.actualizarMiembro(idMiembro, dto, casaUsuarioId));

        assertThat(thrown.getMessage()).isEqualTo("Este miembro está inactivo");
        assertThat(thrown.getStatus()).isEqualTo(HttpStatus.OK);

        verify(miembroRepository).findById(idMiembro);
        verify(miembroRepository, never()).save(any());
    }

    @Test
    void testActualizarMiembro_WhenMiembroNotFound_ShouldThrowException() {
        // Given
        Long idMiembro = 99L;
        Long casaUsuarioId = 10L;
        MiembroActualizacionDTO dto = new MiembroActualizacionDTO();
        dto.setNumeroDocumento(123L);

        when(miembroRepository.findById(idMiembro)).thenReturn(Optional.empty());

        // When & Then
        ApiException thrown = assertThrows(ApiException.class,
                () -> miembroService.actualizarMiembro(idMiembro, dto, casaUsuarioId));

        assertThat(thrown.getMessage()).isEqualTo("El miembro con id 99 no existe");
        assertThat(thrown.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(miembroRepository).findById(idMiembro);
        verify(miembroRepository, never()).save(any());
    }

    @Test
    void testActualizarMiembro_WhenMiembroDocumentoDuplicado_ShouldThrowException() {
        // Given
        Long idMiembro = 1L;
        Long casaUsuarioId = 10L;

        Miembro miembroExistente = new Miembro();
        miembroExistente.setId(idMiembro);
        miembroExistente.setEstado(true);
        Casa casa = new Casa();
        casa.setId(casaUsuarioId);
        miembroExistente.setCasa(casa);

        MiembroActualizacionDTO dto = new MiembroActualizacionDTO();
        dto.setNumeroDocumento(123L);

        when(miembroRepository.findById(idMiembro)).thenReturn(Optional.of(miembroExistente));
        when(miembroRepository.existsByNumeroDocumentoAndIdNot(123L, idMiembro)).thenReturn(true);

        // When & Then
        ApiException thrown = assertThrows(ApiException.class,
                () -> miembroService.actualizarMiembro(idMiembro, dto, casaUsuarioId));

        assertThat(thrown.getMessage()).isEqualTo(DOCUMENTO_REPETIDO);
        assertThat(thrown.getStatus()).isEqualTo(HttpStatus.OK);

        verify(miembroRepository).findById(idMiembro);
        verify(miembroRepository).existsByNumeroDocumentoAndIdNot(123L, idMiembro);
        verify(miembroRepository, never()).save(any());
    }

    @Test
    void testActualizarMiembro_WhenPersonaDocumentoDuplicado_ShouldThrowException() {
        // Given
        Long idMiembro = 1L;
        Long casaUsuarioId = 10L;

        Miembro miembroExistente = new Miembro();
        miembroExistente.setId(idMiembro);
        miembroExistente.setEstado(true);
        Casa casa = new Casa();
        casa.setId(casaUsuarioId);
        miembroExistente.setCasa(casa);

        MiembroActualizacionDTO dto = new MiembroActualizacionDTO();
        dto.setNumeroDocumento(123L);

        when(miembroRepository.findById(idMiembro)).thenReturn(Optional.of(miembroExistente));
        when(miembroRepository.existsByNumeroDocumentoAndIdNot(123L, idMiembro)).thenReturn(false);
        when(personaRepository.existsByNumeroDocumento(123L)).thenReturn(true);

        // When & Then
        ApiException thrown = assertThrows(ApiException.class,
                () -> miembroService.actualizarMiembro(idMiembro, dto, casaUsuarioId));

        assertThat(thrown.getMessage()).isEqualTo(DOCUMENTO_REPETIDO);
        assertThat(thrown.getStatus()).isEqualTo(HttpStatus.OK);

        verify(miembroRepository).findById(idMiembro);
        verify(miembroRepository).existsByNumeroDocumentoAndIdNot(123L, idMiembro);
        verify(personaRepository).existsByNumeroDocumento(123L);
        verify(miembroRepository, never()).save(any());
    }

    @Test
    void testActualizarEstadoMiembro_ShouldHabilitarSiEstabaDeshabilitado() {
        // Given
        Long idMiembro = 1L;
        Long casaUsuarioId = 10L;

        Miembro miembro = new Miembro();
        miembro.setId(idMiembro);
        miembro.setEstado(false);
        Casa casa = new Casa();
        casa.setId(casaUsuarioId);
        miembro.setCasa(casa);

        when(miembroRepository.findById(idMiembro)).thenReturn(Optional.of(miembro));

        // When
        SuccessResult<Void> result = miembroService.actualizarEstadoMiembro(idMiembro, casaUsuarioId);

        // Then
        assertThat(miembro.getEstado()).isTrue();
        assertThat(result.message()).isEqualTo("Miembro habilitado correctamente");

        verify(miembroRepository).findById(idMiembro);
        verify(miembroRepository).save(miembro);
    }

    @Test
    void testActualizarEstadoMiembro_ShouldDeshabilitarSiEstabaHabilitado() {
        // Given
        Long idMiembro = 2L;
        Long casaUsuarioId = 10L;

        Miembro miembro = new Miembro();
        miembro.setId(idMiembro);
        miembro.setEstado(true);
        Casa casa = new Casa();
        casa.setId(casaUsuarioId);
        miembro.setCasa(casa);

        when(miembroRepository.findById(idMiembro)).thenReturn(Optional.of(miembro));

        // When
        SuccessResult<Void> result = miembroService.actualizarEstadoMiembro(idMiembro, casaUsuarioId);

        // Then
        assertThat(miembro.getEstado()).isFalse();
        assertThat(result.message()).isEqualTo("Miembro deshabilitado correctamente");

        verify(miembroRepository).findById(idMiembro);
        verify(miembroRepository).save(miembro);
    }

    @Test
    void testActualizarEstadoMiembro_WhenMiembroNotFound_ShouldThrowException() {
        // Given
        Long idMiembro = 99L;
        Long casaUsuarioId = 10L;
        when(miembroRepository.findById(idMiembro)).thenReturn(Optional.empty());

        // When & Then
        ApiException thrown = assertThrows(ApiException.class,
                () -> miembroService.actualizarEstadoMiembro(idMiembro, casaUsuarioId));

        assertThat(thrown.getMessage()).isEqualTo("El miembro con id 99 no existe");
        assertThat(thrown.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(miembroRepository).findById(idMiembro);
        verify(miembroRepository, never()).save(any());
    }

    @Test
    void testActualizarMiembro_CuandoNoPerteneceALaCasa_ShouldThrowForbiddenException() {
        // Given
        Long idMiembro = 1L;
        Long casaUsuarioId = 10L;
        Long casaMiembroId = 20L;

        Casa casaMiembro = new Casa();
        casaMiembro.setId(casaMiembroId);

        Miembro miembro = new Miembro();
        miembro.setId(idMiembro);
        miembro.setEstado(true);
        miembro.setCasa(casaMiembro);

        MiembroActualizacionDTO dto = new MiembroActualizacionDTO();

        when(miembroRepository.findById(idMiembro)).thenReturn(Optional.of(miembro));

        // When & Then
        ApiException thrown = assertThrows(ApiException.class, () ->
                miembroService.actualizarMiembro(idMiembro, dto, casaUsuarioId)
        );

        assertThat(thrown.getMessage()).isEqualTo("No puedes modificar miembros que no pertenezcan a tu casa");
        assertThat(thrown.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);

        verify(miembroRepository).findById(idMiembro);
        verify(miembroRepository, never()).save(any());
    }

    @Test
    void testActualizarEstadoMiembro_CuandoNoPerteneceALaCasa_ShouldThrowForbiddenException() {
        // Given
        Long idMiembro = 1L;
        Long casaUsuarioId = 10L;
        Long casaMiembroId = 20L;

        Casa casaMiembro = new Casa();
        casaMiembro.setId(casaMiembroId);

        Miembro miembro = new Miembro();
        miembro.setId(idMiembro);
        miembro.setCasa(casaMiembro);
        miembro.setEstado(true);

        when(miembroRepository.findById(idMiembro)).thenReturn(Optional.of(miembro));

        // When & Then
        ApiException thrown = assertThrows(ApiException.class, () ->
                miembroService.actualizarEstadoMiembro(idMiembro, casaUsuarioId)
        );

        assertThat(thrown.getMessage()).isEqualTo("No puedes modificar miembros que no pertenezcan a tu casa");
        assertThat(thrown.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);

        verify(miembroRepository).findById(idMiembro);
        verify(miembroRepository, never()).save(any());
    }

    @Test
    void testObtenerMiembrosPorCasaConEstado_WhenAllExist() {
        // Given
        Long casaId = 1L;

        Persona propietario = new Persona();
        propietario.setPrimerNombre("Juan");
        propietario.setPrimerApellido("Pérez");
        propietario.setTipoDocumento(TipoDocumento.CEDULA_DE_CIUDADANIA);
        UserEntity propietarioUser = new UserEntity();
        propietarioUser.setEmail("juan@example.com");
        propietario.setUser(propietarioUser);

        when(personaRepository.findPropietarioByCasaId(casaId))
                .thenReturn(Optional.of(propietario));

        Persona arrendatario = new Persona();
        arrendatario.setPrimerNombre("Ana");
        arrendatario.setPrimerApellido("Gómez");
        arrendatario.setTipoDocumento(TipoDocumento.CEDULA_DE_EXTRANJERIA);
        UserEntity arrUser = new UserEntity();
        arrUser.setEmail("ana@example.com");
        arrendatario.setUser(arrUser);

        when(personaRepository.findArrendatarioByCasaId(casaId))
                .thenReturn(Optional.of(arrendatario));

        Miembro hijo = new Miembro();
        hijo.setNombre("Pedro");
        hijo.setParentesco("Hijo");
        hijo.setTipoDocumento(TipoDocumento.TARJETA_DE_IDENTIDAD);
        when(miembroRepository.findByCasaIdAndEstadoTrue(casaId))
                .thenReturn(List.of(hijo));

        // When
        SuccessResult<MiembrosResponseDTO> result =
                miembroService.obtenerMiembrosPorCasaConEstado(casaId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Miembros encontrados");

        MiembrosResponseDTO data = result.data();

        assertThat(data.getMiembros().size()).isEqualTo(3);
        assertThat(data.isArrendatarioExiste()).isTrue();
        assertThat(data.isMiembrosExisten()).isTrue();

        verify(personaRepository).findPropietarioByCasaId(casaId);
        verify(personaRepository).findArrendatarioByCasaId(casaId);
        verify(miembroRepository).findByCasaIdAndEstadoTrue(casaId);
    }

    @Test
    void testObtenerMiembrosPorCasaConEstado_WhenNoArrendatario() {
        // Given
        Long casaId = 1L;

        Persona propietario = new Persona();
        propietario.setPrimerNombre("Juan");
        propietario.setPrimerApellido("Pérez");
        UserEntity user = new UserEntity();
        user.setEmail("juan@example.com");
        propietario.setUser(user);

        when(personaRepository.findPropietarioByCasaId(casaId))
                .thenReturn(Optional.of(propietario));

        when(personaRepository.findArrendatarioByCasaId(casaId))
                .thenReturn(Optional.empty());

        Miembro miembro = new Miembro();
        miembro.setNombre("Carlos");
        miembro.setParentesco("Hermano");
        when(miembroRepository.findByCasaIdAndEstadoTrue(casaId))
                .thenReturn(List.of(miembro));

        // When
        SuccessResult<MiembrosResponseDTO> result =
                miembroService.obtenerMiembrosPorCasaConEstado(casaId);

        // Then
        MiembrosResponseDTO data = result.data();

        assertThat(data.getMiembros().size()).isEqualTo(2);
        assertThat(data.isArrendatarioExiste()).isFalse();
        assertThat(data.isMiembrosExisten()).isTrue();
    }

    @Test
    void testObtenerMiembrosPorCasaConEstado_WhenNoneExist() {
        // Given
        Long casaId = 1L;
        when(personaRepository.findPropietarioByCasaId(casaId))
                .thenReturn(Optional.empty());
        when(personaRepository.findArrendatarioByCasaId(casaId))
                .thenReturn(Optional.empty());
        when(miembroRepository.findByCasaIdAndEstadoTrue(casaId))
                .thenReturn(List.of());

        // When
        SuccessResult<MiembrosResponseDTO> result =
                miembroService.obtenerMiembrosPorCasaConEstado(casaId);

        // Then
        MiembrosResponseDTO data = result.data();

        assertThat(data.getMiembros())
                .asInstanceOf(InstanceOfAssertFactories.list(MiembrosDTO.class))
                .isEmpty();

        assertThat(data.isArrendatarioExiste()).isFalse();
        assertThat(data.isMiembrosExisten()).isFalse();
        assertThat(result.message()).isEqualTo("Miembros encontrados");
    }
}