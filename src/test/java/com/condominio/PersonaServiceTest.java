package com.condominio;

import com.condominio.dto.request.PersonaRegistroDTO;
import com.condominio.dto.request.PersonaUpdateDTO;
import com.condominio.dto.response.PersonaSimpleRolDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.implementation.PersonaService;
import com.condominio.service.interfaces.ICasaService;
import com.condominio.service.interfaces.IUserService;
import com.condominio.util.events.CreatedPersonaEvent;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PersonaServiceTest {

    @Mock
    private IUserService userService;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private ICasaService casaService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PersonaService personaService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private com.condominio.persistence.repository.UserRepository userRepository;


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
    void save_ShouldReturnSuccessResult_WhenDataIsValid() {

        PersonaRegistroDTO dto = new PersonaRegistroDTO();
        dto.setEmail("test@example.com");
        dto.setNumeroDocumento(123L);
        dto.setRolEnCasa(RoleEnum.PROPIETARIO);
        dto.setIdCasa(1L);

        UserEntity mockUser = new UserEntity();
        mockUser.setEmail(dto.getEmail());

        Casa mockCasa = new Casa();
        mockCasa.setId(dto.getIdCasa());
        mockCasa.setNumeroCasa(10);

        Persona mappedPersona = new Persona();
        Persona savedPersona = new Persona();

        when(userService.createUser(dto.getEmail(), dto.getNumeroDocumento(), dto.getRolEnCasa()))
                .thenReturn(mockUser);
        when(personaRepository.findByNumeroDocumento(dto.getNumeroDocumento()))
                .thenReturn(Optional.empty());
        when(personaRepository.existsRoleInCasa(dto.getIdCasa(), dto.getRolEnCasa()))
                .thenReturn(false);
        when(casaService.findById(dto.getIdCasa()))
                .thenReturn(Optional.of(mockCasa));
        when(modelMapper.map(dto, Persona.class))
                .thenReturn(mappedPersona);
        when(personaRepository.save(any(Persona.class)))
                .thenReturn(savedPersona);


        SuccessResult<Persona> result = personaService.save(dto);


        assertNotNull(result);
        assertEquals("Persona registrada correctamente", result.message());
        assertEquals(savedPersona, result.data());

        verify(userService).createUser(dto.getEmail(), dto.getNumeroDocumento(), dto.getRolEnCasa());
        verify(personaRepository).findByNumeroDocumento(dto.getNumeroDocumento());
        verify(personaRepository).existsRoleInCasa(dto.getIdCasa(), dto.getRolEnCasa());
        verify(casaService).findById(dto.getIdCasa());
        verify(personaRepository).save(mappedPersona);
    }

    @Test
    void save_ShouldThrowException_WhenDocumentoAlreadyExists() {
        PersonaRegistroDTO dto = new PersonaRegistroDTO();
        dto.setEmail("test@example.com");
        dto.setNumeroDocumento(123L);
        dto.setRolEnCasa(RoleEnum.ADMIN);
        dto.setIdCasa(1L);

        UserEntity mockUser = new UserEntity();

        when(userService.createUser(dto.getEmail(), dto.getNumeroDocumento(), dto.getRolEnCasa()))
                .thenReturn(mockUser);
        when(personaRepository.findByNumeroDocumento(dto.getNumeroDocumento()))
                .thenReturn(Optional.of(new Persona()));

        ApiException ex = assertThrows(ApiException.class, () -> personaService.save(dto));

        assertEquals("El numero  de documento ya se  encuentra registrado", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(userService).createUser(dto.getEmail(), dto.getNumeroDocumento(), dto.getRolEnCasa());
        verify(personaRepository).findByNumeroDocumento(dto.getNumeroDocumento());
        verifyNoMoreInteractions(personaRepository);
    }

    @Test
    void save_ShouldThrowException_WhenRoleAlreadyExistsInCasa() {
        PersonaRegistroDTO dto = new PersonaRegistroDTO();
        dto.setEmail("test@example.com");
        dto.setNumeroDocumento(123L);
        dto.setRolEnCasa(RoleEnum.ARRENDATARIO);
        dto.setIdCasa(2L);

        UserEntity mockUser = new UserEntity();

        when(userService.createUser(dto.getEmail(), dto.getNumeroDocumento(), dto.getRolEnCasa()))
                .thenReturn(mockUser);
        when(personaRepository.findByNumeroDocumento(dto.getNumeroDocumento()))
                .thenReturn(Optional.empty());
        when(personaRepository.existsRoleInCasa(dto.getIdCasa(), dto.getRolEnCasa()))
                .thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> personaService.save(dto));

        assertTrue(ex.getMessage().contains("Ya existe un " + dto.getRolEnCasa()));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(userService).createUser(dto.getEmail(), dto.getNumeroDocumento(), dto.getRolEnCasa());
        verify(personaRepository).findByNumeroDocumento(dto.getNumeroDocumento());
        verify(personaRepository).existsRoleInCasa(dto.getIdCasa(), dto.getRolEnCasa());
        verifyNoMoreInteractions(casaService);
    }

    @Test
    void save_ShouldThrowException_WhenCasaDoesNotExist() {
        PersonaRegistroDTO dto = new PersonaRegistroDTO();
        dto.setEmail("test@example.com");
        dto.setNumeroDocumento(123L);
        dto.setRolEnCasa(RoleEnum.PROPIETARIO);
        dto.setIdCasa(99L);

        UserEntity mockUser = new UserEntity();

        when(userService.createUser(dto.getEmail(), dto.getNumeroDocumento(), dto.getRolEnCasa()))
                .thenReturn(mockUser);
        when(personaRepository.findByNumeroDocumento(dto.getNumeroDocumento()))
                .thenReturn(Optional.empty());
        when(personaRepository.existsRoleInCasa(dto.getIdCasa(), dto.getRolEnCasa()))
                .thenReturn(false);
        when(casaService.findById(dto.getIdCasa()))
                .thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> personaService.save(dto));

        assertTrue(ex.getMessage().contains("La casa con id " + dto.getIdCasa() + " no existe"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

        verify(userService).createUser(dto.getEmail(), dto.getNumeroDocumento(), dto.getRolEnCasa());
        verify(personaRepository).findByNumeroDocumento(dto.getNumeroDocumento());
        verify(personaRepository).existsRoleInCasa(dto.getIdCasa(), dto.getRolEnCasa());
        verify(casaService).findById(dto.getIdCasa());
    }

    @Test
    void save_ShouldPublishCreatedPersonaEvent_WhenPersonaIsSaved() {

        PersonaRegistroDTO dto = new PersonaRegistroDTO();
        dto.setEmail("event@example.com");
        dto.setNumeroDocumento(456L);
        dto.setRolEnCasa(RoleEnum.PROPIETARIO);
        dto.setIdCasa(10L);

        UserEntity mockUser = new UserEntity();
        mockUser.setEmail(dto.getEmail());

        Casa mockCasa = new Casa();
        mockCasa.setId(dto.getIdCasa());
        mockCasa.setNumeroCasa(20);

        Persona mappedPersona = new Persona();
        Persona savedPersona = new Persona();

        when(userService.createUser(dto.getEmail(), dto.getNumeroDocumento(), dto.getRolEnCasa()))
                .thenReturn(mockUser);
        when(personaRepository.findByNumeroDocumento(dto.getNumeroDocumento()))
                .thenReturn(Optional.empty());
        when(personaRepository.existsRoleInCasa(dto.getIdCasa(), dto.getRolEnCasa()))
                .thenReturn(false);
        when(casaService.findById(dto.getIdCasa()))
                .thenReturn(Optional.of(mockCasa));
        when(modelMapper.map(dto, Persona.class))
                .thenReturn(mappedPersona);
        when(personaRepository.save(any(Persona.class)))
                .thenReturn(savedPersona);


        personaService.save(dto);


        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        verify(applicationEventPublisher).publishEvent(captor.capture());

        Object captured = captor.getValue();
        assertInstanceOf(CreatedPersonaEvent.class, captured);
        assertEquals(savedPersona, ((CreatedPersonaEvent) captured).getPersona());

    }
    @Test
    void obtenerSolicitantePorCasa_debeRetornarArrendatarioSiExiste() {

        Persona arrendatario = new Persona();
        when(personaRepository.findArrendatarioByCasaId(1L))
                .thenReturn(Optional.of(arrendatario));


        Persona result = personaService.obtenerSolicitantePorCasa(1L);


        assertNotNull(result);
        assertEquals(arrendatario, result);
        verify(personaRepository).findArrendatarioByCasaId(1L);
        verify(personaRepository, never()).findPropietarioByCasaId(anyLong());
    }
    @Test
    void obtenerSolicitantePorCasa_debeRetornarPropietarioSiNoHayArrendatario() {

        Persona propietario = new Persona();
        when(personaRepository.findArrendatarioByCasaId(2L))
                .thenReturn(Optional.empty());
        when(personaRepository.findPropietarioByCasaId(2L))
                .thenReturn(Optional.of(propietario));


        Persona result = personaService.obtenerSolicitantePorCasa(2L);


        assertNotNull(result);
        assertEquals(propietario, result);
        verify(personaRepository).findArrendatarioByCasaId(2L);
        verify(personaRepository).findPropietarioByCasaId(2L);
    }
    @Test
    void obtenerSolicitantePorCasa_debeLanzarExcepcionSiNoExisteSolicitante() {

        Long idCasa = 3L;
        when(personaRepository.findArrendatarioByCasaId(idCasa))
                .thenReturn(Optional.empty());
        when(personaRepository.findPropietarioByCasaId(idCasa))
                .thenReturn(Optional.empty());


        ApiException ex = assertThrows(ApiException.class,
                () -> personaService.obtenerSolicitantePorCasa(idCasa));


        assertTrue(ex.getMessage().contains("No se encontró un solicitante"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(personaRepository).findArrendatarioByCasaId(idCasa);
        verify(personaRepository).findPropietarioByCasaId(idCasa);
    }



    @Test
    void getPersonaFromUserDetails_ShouldReturnPersona_WhenUserAndPersonaExist() {

        String email = "test@example.com";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(email);

        UserEntity mockUserEntity = new UserEntity();
        mockUserEntity.setEmail(email);

        Persona mockPersona = new Persona();

        when(userRepository.findUserEntityByEmail(email)).thenReturn(mockUserEntity);
        when(personaRepository.findPersonaByUser(mockUserEntity)).thenReturn(mockPersona);


        Persona result = personaService.getPersonaFromUserDetails(userDetails);


        assertNotNull(result);
        assertEquals(mockPersona, result);
        verify(userRepository).findUserEntityByEmail(email);
        verify(personaRepository).findPersonaByUser(mockUserEntity);
    }

    @Test
    void getPersonaFromUserDetails_ShouldThrowException_WhenUserNotFound() {

        String email = "notfound@example.com";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(email);

        when(userRepository.findUserEntityByEmail(email)).thenReturn(null);


        ApiException ex = assertThrows(ApiException.class, () -> personaService.getPersonaFromUserDetails(userDetails));


        assertEquals("Usuario no encontrado con el email: " + email, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(userRepository).findUserEntityByEmail(email);
        verifyNoInteractions(personaRepository);
    }

    @Test
    void getPersonaFromUserDetails_ShouldThrowException_WhenPersonaNotFound() {

        String email = "user@example.com";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(email);

        UserEntity mockUserEntity = new UserEntity();
        mockUserEntity.setEmail(email);

        when(userRepository.findUserEntityByEmail(email)).thenReturn(mockUserEntity);
        when(personaRepository.findPersonaByUser(mockUserEntity)).thenReturn(null);


        ApiException ex = assertThrows(ApiException.class, () -> personaService.getPersonaFromUserDetails(userDetails));


        assertEquals("Persona no encontrada para el usuario: " + email, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(userRepository).findUserEntityByEmail(email);
        verify(personaRepository).findPersonaByUser(mockUserEntity);
    }

    @Test
    void getPersonaPerfil_ShouldReturnPersonaPerfilDTO_WhenDataIsValid() {

        UserDetails userDetails = mock(UserDetails.class);
        Persona mockPersona = new Persona();
        Casa mockCasa = new Casa();
        UserEntity mockUser = new UserEntity();

        mockCasa.setNumeroCasa(10);
        mockUser.setEmail("test@example.com");

        mockPersona.setCasa(mockCasa);
        mockPersona.setJunta(true);
        mockPersona.setComiteConvivencia(false);
        mockPersona.setNumeroDocumento(123L);
        mockPersona.setPrimerNombre("Juan");
        mockPersona.setSegundoNombre("Carlos");
        mockPersona.setPrimerApellido("Pérez");
        mockPersona.setSegundoApellido("Gómez");
        mockPersona.setTelefono(3216549870L);
        mockPersona.setTipoDocumento(com.condominio.persistence.model.TipoDocumento.CEDULA_DE_CIUDADANIA);
        mockPersona.setUser(mockUser);


        PersonaService spyService = spy(personaService);
        doReturn(mockPersona).when(spyService).getPersonaFromUserDetails(userDetails);


        var result = spyService.getPersonaPerfil(userDetails);


        assertNotNull(result);
        assertEquals(mockPersona.getCasa().getNumeroCasa(), result.getNumeroCasa());
        assertEquals(mockPersona.getJunta(), result.getJunta());
        assertEquals(mockPersona.getComiteConvivencia(), result.getComiteConvivencia());
        assertEquals(mockPersona.getNumeroDocumento(), result.getNumeroDocumento());
        assertEquals(mockPersona.getPrimerNombre(), result.getPrimerNombre());
        assertEquals(mockPersona.getSegundoNombre(), result.getSegundoNombre());
        assertEquals(mockPersona.getPrimerApellido(), result.getPrimerApellido());
        assertEquals(mockPersona.getSegundoApellido(), result.getSegundoApellido());
        assertEquals(mockPersona.getUser().getEmail(), result.getEmail());
        assertEquals(mockPersona.getTelefono(), result.getTelefono());
        assertEquals(mockPersona.getTipoDocumento(), result.getTipoDocumento());

        verify(spyService).getPersonaFromUserDetails(userDetails);
    }

    @Test
    void getPersonaPerfil_ShouldThrowException_WhenPersonaNotFound() {

        UserDetails userDetails = mock(UserDetails.class);

        PersonaService spyService = spy(personaService);
        doThrow(new ApiException("Persona no encontrada", HttpStatus.NOT_FOUND))
                .when(spyService).getPersonaFromUserDetails(userDetails);


        ApiException ex = assertThrows(ApiException.class, () -> spyService.getPersonaPerfil(userDetails));


        assertEquals("Persona no encontrada", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(spyService).getPersonaFromUserDetails(userDetails);
    }
    @Test
    void updatePersona_ShouldUpdate_WhenDataIsValid() {
        String email = "test@example.com";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(email);


        Persona persona = new Persona();
        persona.setId(1L);


        PersonaService spyService = spy(personaService);
        doReturn(persona).when(spyService).getPersonaFromUserDetails(userDetails);

        PersonaUpdateDTO dto = PersonaUpdateDTO.builder()
                .primerNombre("Juan")
                .segundoNombre("Carlos")
                .primerApellido("Pérez")
                .segundoApellido("Gómez")
                .telefono(3001234567L)
                .tipoDocumento(TipoDocumento.CEDULA_DE_CIUDADANIA)
                .numeroDocumento(123456L)
                .build();

        when(personaRepository.existsByNumeroDocumentoAndIdNot(dto.getNumeroDocumento(), persona.getId()))
                .thenReturn(false);

        SuccessResult<?> result = spyService.updatePersona(dto, userDetails);

        assertNotNull(result);
        assertEquals("Persona actualizada correctamente", result.message());
        assertNull(result.data());
        verify(personaRepository).save(persona);
    }

    @Test
    void updatePersona_ShouldThrow_WhenNumeroDocumentoAlreadyExists() {
        String email = "test@example.com";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(email);

        Persona persona = new Persona();
        persona.setId(1L);

        PersonaService spyService = spy(personaService);
        doReturn(persona).when(spyService).getPersonaFromUserDetails(userDetails);

        PersonaUpdateDTO dto = PersonaUpdateDTO.builder()
                .primerNombre("Juan")
                .segundoNombre("Carlos")
                .primerApellido("Pérez")
                .segundoApellido("Gómez")
                .telefono(3001234567L)
                .tipoDocumento(TipoDocumento.CEDULA_DE_CIUDADANIA)
                .numeroDocumento(123456L)
                .build();

        when(personaRepository.existsByNumeroDocumentoAndIdNot(dto.getNumeroDocumento(), persona.getId()))
                .thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> spyService.updatePersona(dto, userDetails));

        assertEquals("El número de documento ya está registrado", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(personaRepository, never()).save(any());
    }
    @Test
    void obtenerTodasPersonas_ShouldReturnListOfPersonaSimpleRolDTO() {

        UserEntity user1 = new UserEntity();
        RoleEntity role1 = new RoleEntity();
        role1.setRoleEnum(RoleEnum.ADMIN);
        user1.setRoles(Set.of(role1));
        user1.setEmail("user1@example.com");

        Persona persona1 = new Persona();
        persona1.setPrimerNombre("Juan");
        persona1.setSegundoNombre("Carlos");
        persona1.setPrimerApellido("Pérez");
        persona1.setSegundoApellido("Gómez");
        persona1.setTelefono(123456789L);
        persona1.setUser(user1);

        UserEntity user2 = new UserEntity();
        RoleEntity role2 = new RoleEntity();
        role2.setRoleEnum(RoleEnum.PROPIETARIO);
        user2.setRoles(Set.of(role2));
        user2.setEmail("user2@example.com");

        Persona persona2 = new Persona();
        persona2.setPrimerNombre("Ana");
        persona2.setPrimerApellido("López");
        persona2.setTelefono(987654321L);
        persona2.setUser(user2);

        when(personaRepository.findAll()).thenReturn(List.of(persona1, persona2));


        List<PersonaSimpleRolDTO> result = personaService.obtenerTodasPersonas();


        assertNotNull(result);
        assertEquals(2, result.size());

        PersonaSimpleRolDTO dto1 = result.getFirst();
        assertEquals("Juan Carlos Pérez Gómez", dto1.getNombreCompleto());
        assertEquals(123456789L, dto1.getTelefono());
        assertEquals("user1@example.com", dto1.getCorreo());
        assertEquals(List.of("ADMIN"), dto1.getRoles());

        PersonaSimpleRolDTO dto2 = result.get(1);
        assertEquals("Ana López", dto2.getNombreCompleto());
        assertEquals(987654321L, dto2.getTelefono());
        assertEquals("user2@example.com", dto2.getCorreo());
        assertEquals(List.of("PROPIETARIO"), dto2.getRoles());
    }

    @Test
    void obtenerTodasPersonas_ShouldReturnEmptyList_WhenNoPersonas() {

        when(personaRepository.findAll()).thenReturn(Collections.emptyList());


        List<PersonaSimpleRolDTO> result = personaService.obtenerTodasPersonas();


        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}