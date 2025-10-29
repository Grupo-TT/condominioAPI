package com.condominio;

import com.condominio.dto.request.AsambleaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Asamblea;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.AsambleaRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.implementation.AsambleaService;
import com.condominio.service.implementation.EmailService;
import com.condominio.util.constants.AppConstants;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsambleaServiceTest {

    @Mock
    private AsambleaRepository asambleaRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private EmailService emailService;

    private AsambleaService asambleaService;

    @BeforeEach
    void setUp() {
        asambleaService = new AsambleaService(asambleaRepository, modelMapper, personaRepository, emailService);
    }

    @Test
    void create_shouldThrowException_whenFechaAnterior() {
        AsambleaDTO dto = new AsambleaDTO();
        // Fecha de ayer
        dto.setFecha(Date.from(LocalDate.now(AppConstants.ZONE).minusDays(1).atStartOfDay(AppConstants.ZONE).toInstant()));

        ApiException exception = assertThrows(ApiException.class, () -> asambleaService.create(dto));
        assertEquals("Por favor ingresar una fecha valida", exception.getMessage());
    }

    @Test
    void create_shouldSaveAsamblea_andSendEmails() {
        // Fecha válida
        Date fecha = Date.from(LocalDate.now(AppConstants.ZONE).plusDays(1).atStartOfDay(AppConstants.ZONE).toInstant());
        AsambleaDTO dto = new AsambleaDTO();
        dto.setTitulo("Reunión");
        dto.setFecha(fecha);
        dto.setHoraInicio(LocalTime.of(10, 0));

        Asamblea entidad = new Asamblea();
        entidad.setTitulo(dto.getTitulo());
        entidad.setFecha(dto.getFecha());
        entidad.setHoraInicio(dto.getHoraInicio());

        // Mock del ModelMapper
        when(modelMapper.map(dto, Asamblea.class)).thenReturn(entidad);
        // Mock del repo
        when(asambleaRepository.save(entidad)).thenReturn(entidad);

        // Mock personas
        Persona persona = new Persona();
        UserEntity user = new UserEntity ();
        user.setEmail("user@correo.com");
        persona.setUser(user);
        when(personaRepository.findAll()).thenReturn(Collections.singletonList(persona));

        SuccessResult<AsambleaDTO> result = asambleaService.create(dto);


        verify(asambleaRepository).save(entidad);
        verify(emailService).enviarInvitacionesAsambleaMasivas(Collections.singletonList(persona), entidad);
        assertEquals("Asamblea programada correctamente", result.message());
        assertEquals(dto, result.data());
    }

    @Test
    void testCreate_FechaPasada_LanzaApiException() {
        AsambleaDTO dto = new AsambleaDTO();
        dto.setTitulo("Asamblea Pasada");
        dto.setFecha(Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        ApiException thrown = assertThrows(ApiException.class, () -> {
            asambleaService.create(dto);
        });

        assertEquals("Por favor ingresar una fecha valida", thrown.getMessage());
        verifyNoInteractions(asambleaRepository, emailService);
    }
}
