package com.condominio;

import com.condominio.dto.request.PersonaRegistroDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.RoleEnum;
import com.condominio.persistence.model.UserEntity;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@SpringBootTest
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
        // Arrange
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

}