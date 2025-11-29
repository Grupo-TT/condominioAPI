package com.condominio;

import com.condominio.dto.response.DestinatarioInfoDTO;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.CorreoDestinatario;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.CorreoDestinatarioRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.UserRepository;
import com.condominio.service.implementation.CorreoDestinatarioService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CorreoDestinatarioServiceTest {

    private CorreoDestinatarioRepository correoDestinatarioRepository;
    private UserRepository userRepository;
    private PersonaRepository personaRepository;
    private CorreoDestinatarioService service;

    @BeforeEach
    void setUp() {
        correoDestinatarioRepository = mock(CorreoDestinatarioRepository.class);
        userRepository = mock(UserRepository.class);
        personaRepository = mock(PersonaRepository.class);

        service = new CorreoDestinatarioService(correoDestinatarioRepository, userRepository, personaRepository);
    }

    @Test
    void getDestinatariosInfo_validData_returnsList() {
        Long correoId = 1L;

        // Mock CorreoDestinatario
        CorreoDestinatario dest = new CorreoDestinatario();
        dest.setEmailDestinatario("test@correo.com");
        List<CorreoDestinatario> lista = new ArrayList<>();
        lista.add(dest);

        when(correoDestinatarioRepository.findByCorreoEnviadoId(correoId)).thenReturn(lista);

        // Mock UserEntity
        UserEntity user = new UserEntity();
        user.setId(10L);
        when(userRepository.findByEmail("test@correo.com")).thenReturn(Optional.of(user));

        // Mock Persona
        Persona persona = new Persona();
        persona.setPrimerNombre("Juan Pérez");
        Casa casa = new Casa();
        casa.setId(5L);
        persona.setCasa(casa);
        when(personaRepository.findByUser_Id(10L)).thenReturn(persona);

        // Ejecutar
        List<DestinatarioInfoDTO> result = service.getDestinatariosInfo(correoId);

        // Verificar
        assertEquals(1, result.size());
        DestinatarioInfoDTO dto = result.get(0);
        assertEquals("Juan Pérez", dto.getNombreCompleto());
        assertEquals(5L, dto.getIdCasa());
        assertEquals("test@correo.com", dto.getEmail());
    }

    @Test
    void getDestinatariosInfo_noDestinatarios_throwsException() {
        Long correoId = 1L;
        when(correoDestinatarioRepository.findByCorreoEnviadoId(correoId)).thenReturn(List.of());

        ApiException ex = assertThrows(ApiException.class, () -> service.getDestinatariosInfo(correoId));
        assertEquals("No se encontraron destinatarios para este correo", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void getDestinatariosInfo_userOrPersonaNull_skipsEntry() {
        Long correoId = 1L;

        CorreoDestinatario dest = new CorreoDestinatario();
        dest.setEmailDestinatario("noexist@correo.com");
        List<CorreoDestinatario> lista = new ArrayList<>();
        lista.add(dest);

        when(correoDestinatarioRepository.findByCorreoEnviadoId(correoId)).thenReturn(lista);
        when(userRepository.findByEmail("noexist@correo.com")).thenReturn(Optional.empty());

        List<DestinatarioInfoDTO> result = service.getDestinatariosInfo(correoId);

        assertTrue(result.isEmpty(), "Debe omitir entradas sin user o persona");
    }
}
