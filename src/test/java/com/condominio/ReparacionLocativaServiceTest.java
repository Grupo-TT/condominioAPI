package com.condominio;

import com.condominio.dto.response.PersonaSimpleDTO;
import com.condominio.dto.response.ReparacionLocativaDTO;
import com.condominio.dto.response.SolicitudReparacionLocativaDTO;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.ReparacionLocativa;
import com.condominio.persistence.model.SolicitudReparacionLocativa;
import com.condominio.persistence.repository.ReparacionLocativaRepository;
import com.condominio.service.implementation.ReparacionLocativaService;
import com.condominio.util.exception.ApiException;
import com.condominio.util.helper.PersonaHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReparacionLocativaServiceTest {
    @Mock
    private ReparacionLocativaRepository reparacionLocativaRepository;

    @Mock
    private PersonaHelper personaHelper;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ReparacionLocativaService reparacionLocativaService;

    @Test
    void findAll_shouldReturnDtosWithSolicitanteSet() {
        // Arrange
        Long casaId = 42L;

        // Entity setup
        Casa casa = new Casa();
        casa.setId(casaId);

        SolicitudReparacionLocativa solicitudEntity = new SolicitudReparacionLocativa();
        solicitudEntity.setCasa(casa);

        ReparacionLocativa entity = new ReparacionLocativa();
        entity.setSolicitudReparacionLocativa(solicitudEntity);

        // DTOs returned by modelMapper
        SolicitudReparacionLocativaDTO solicitudDto = new SolicitudReparacionLocativaDTO();
        ReparacionLocativaDTO reparacionDto = new ReparacionLocativaDTO();
        reparacionDto.setSolicitudReparacionLocativa(solicitudDto);

        when(reparacionLocativaRepository.findAll()).thenReturn(List.of(entity));
        when(modelMapper.map(entity, ReparacionLocativaDTO.class)).thenReturn(reparacionDto);

        // Persona helper stubs
        Persona persona = new Persona();
        persona.setPrimerNombre("Ana");
        persona.setPrimerApellido("Lopez");

        PersonaSimpleDTO personaSimple = PersonaSimpleDTO.builder()
                .nombreCompleto("Ana Lopez")
                .telefono(null)
                .correo("ana@example.com")
                .build();

        when(personaHelper.obtenerSolicitantePorCasa(casaId)).thenReturn(persona);
        when(personaHelper.toPersonaSimpleDTO(persona)).thenReturn(personaSimple);

        // Act
        List<ReparacionLocativaDTO> results = reparacionLocativaService.findAll();

        // Assert
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        ReparacionLocativaDTO got = results.getFirst();

        // Ajusta estas llamadas si tu DTO anidado usa nombres distintos:
        assertThat(got.getSolicitudReparacionLocativa()).isNotNull();
        assertThat(got.getSolicitudReparacionLocativa().getSolicitante()).isEqualTo(personaSimple);

        verify(reparacionLocativaRepository, times(1)).findAll();
        verify(modelMapper, times(1)).map(entity, ReparacionLocativaDTO.class);
        verify(personaHelper, times(1)).obtenerSolicitantePorCasa(casaId);
        verify(personaHelper, times(1)).toPersonaSimpleDTO(persona);
    }

    @Test
    void findAll_shouldThrowApiException_whenNoReparaciones() {
        // Arrange
        when(reparacionLocativaRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class, () -> reparacionLocativaService.findAll());
        assertThat(ex.getMessage()).isEqualTo("No hay reparaciones registradas");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(reparacionLocativaRepository, times(1)).findAll();
        verifyNoInteractions(modelMapper, personaHelper);
    }
}
