package com.condominio;

import com.condominio.dto.response.DestinatarioInfoDTO;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.CorreoEnviado;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.CorreoEnviadoRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.UserRepository;
import com.condominio.service.implementation.CorreoDestinatarioService;
import com.condominio.util.exception.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorreoDestinatarioServiceTest {

    @Mock
    private CorreoEnviadoRepository correoEnviadoRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PersonaRepository personaRepository;
    @Mock
    private ObjectMapper objectMapper;

    private CorreoDestinatarioService service;

    @BeforeEach
    void setUp() {
        service = new CorreoDestinatarioService(correoEnviadoRepository, userRepository, personaRepository, objectMapper);
    }

    @Test
    void getDestinatariosInfo_validData_returnsList() throws IOException {
        Long correoId = 1L;
        String jsonDestinatarios = "[\"test@correo.com\"]";
        List<String> emailList = List.of("test@correo.com");

        // Mock CorreoEnviado
        CorreoEnviado correo = new CorreoEnviado();
        correo.setId(correoId);
        correo.setDestinatarios(jsonDestinatarios);
        when(correoEnviadoRepository.findById(correoId)).thenReturn(Optional.of(correo));

        // Mock ObjectMapper
        when(objectMapper.readValue(eq(jsonDestinatarios), any(TypeReference.class))).thenReturn(emailList);

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

        // Execute
        List<DestinatarioInfoDTO> result = service.getDestinatariosInfo(correoId);

        // Verify
        assertEquals(1, result.size());
        DestinatarioInfoDTO dto = result.get(0);
        assertEquals("Juan Pérez", dto.getNombreCompleto());
        assertEquals(5L, dto.getIdCasa());
        assertEquals("test@correo.com", dto.getEmail());
    }

    @Test
    void getDestinatariosInfo_correoNotFound_throwsException() {
        when(correoEnviadoRepository.findById(anyLong())).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.getDestinatariosInfo(1L));
        assertEquals("Correo no encontrado", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void getDestinatariosInfo_emptyEmailList_throwsException() throws IOException {
        Long correoId = 1L;
        CorreoEnviado correo = new CorreoEnviado();
        correo.setId(correoId);
        correo.setDestinatarios("[]");
        when(correoEnviadoRepository.findById(correoId)).thenReturn(Optional.of(correo));
        when(objectMapper.readValue(eq("[]"), any(TypeReference.class))).thenReturn(List.of());

        ApiException ex = assertThrows(ApiException.class, () -> service.getDestinatariosInfo(correoId));
        assertEquals("No se encontraron destinatarios para este correo", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void getDestinatariosInfo_nullJsonString_returnsEmptyList() {
        Long correoId = 1L;
        CorreoEnviado correo = new CorreoEnviado();
        correo.setId(correoId);
        correo.setDestinatarios(null);
        when(correoEnviadoRepository.findById(correoId)).thenReturn(Optional.of(correo));

        List<DestinatarioInfoDTO> result = service.getDestinatariosInfo(correoId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getDestinatariosInfo_emptyJsonString_returnsEmptyList() {
        Long correoId = 1L;
        CorreoEnviado correo = new CorreoEnviado();
        correo.setId(correoId);
        correo.setDestinatarios("");
        when(correoEnviadoRepository.findById(correoId)).thenReturn(Optional.of(correo));

        List<DestinatarioInfoDTO> result = service.getDestinatariosInfo(correoId);

        assertTrue(result.isEmpty());
    }


    @Test
    void getDestinatariosInfo_userOrPersonaNull_skipsEntry() throws IOException {
        Long correoId = 1L;
        String jsonDestinatarios = "[\"noexist@correo.com\"]";
        List<String> emailList = List.of("noexist@correo.com");

        CorreoEnviado correo = new CorreoEnviado();
        correo.setId(correoId);
        correo.setDestinatarios(jsonDestinatarios);
        when(correoEnviadoRepository.findById(correoId)).thenReturn(Optional.of(correo));
        when(objectMapper.readValue(eq(jsonDestinatarios), any(TypeReference.class))).thenReturn(emailList);
        when(userRepository.findByEmail("noexist@correo.com")).thenReturn(Optional.empty());

        List<DestinatarioInfoDTO> result = service.getDestinatariosInfo(correoId);

        assertTrue(result.isEmpty(), "Should skip entries without a user or persona");
    }
}
