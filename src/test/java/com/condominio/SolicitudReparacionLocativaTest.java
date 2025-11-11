package com.condominio;

import com.condominio.dto.response.SolicitudReparacionLocativaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.SolicitudReparacionLocativa;
import com.condominio.persistence.repository.SolicitudReparacionLocativaRepository;
import com.condominio.service.implementation.SolicitudReparacionLocativaService;
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
public class SolicitudReparacionLocativaTest {
    @Mock
    private SolicitudReparacionLocativaRepository solicitudRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PersonaHelper personaHelper;

    @InjectMocks
    private SolicitudReparacionLocativaService service;

    @Test
    void findByEstado_shouldReturnDtosWithSolicitante_whenThereAreRequests() {
        // Arrange
        EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

        Long casaId = 7L;
        Casa casa = new Casa();
        casa.setId(casaId);

        SolicitudReparacionLocativa entity = new SolicitudReparacionLocativa();
        entity.setId(1L);
        entity.setCasa(casa);

        SolicitudReparacionLocativaDTO dto = new SolicitudReparacionLocativaDTO();

        Persona persona = new Persona();
        persona.setPrimerNombre("Ana");
        persona.setPrimerApellido("Lopez");
        // personaHelper.toPersonaSimpleDTO -> we stub a PersonaSimpleDTO, but we only need equality

        // Prepare stubs
        when(solicitudRepo.findByEstadoSolicitud(estado)).thenReturn(List.of(entity));
        when(modelMapper.map(entity, SolicitudReparacionLocativaDTO.class)).thenReturn(dto);
        when(personaHelper.obtenerSolicitantePorCasa(casaId)).thenReturn(persona);

        var personaSimple = com.condominio.dto.response.PersonaSimpleDTO.builder()
                .nombreCompleto("Ana Lopez")
                .telefono(null)
                .correo("ana@example.com")
                .build();
        when(personaHelper.toPersonaSimpleDTO(persona)).thenReturn(personaSimple);

        // Act
        SuccessResult<List<SolicitudReparacionLocativaDTO>> result = service.findByEstado(estado);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.message()).contains("Solicitudes " + estado.name().toLowerCase() + " obtenidas correctamente");
        assertThat(result.data()).isNotNull().hasSize(1);
        SolicitudReparacionLocativaDTO returned = result.data().getFirst();
        assertThat(returned.getSolicitante()).isEqualTo(personaSimple);

        verify(solicitudRepo, times(1)).findByEstadoSolicitud(estado);
        verify(modelMapper, times(1)).map(entity, SolicitudReparacionLocativaDTO.class);
        verify(personaHelper, times(1)).obtenerSolicitantePorCasa(casaId);
        verify(personaHelper, times(1)).toPersonaSimpleDTO(persona);
    }

    @Test
    void findByEstado_shouldThrowApiException_whenNoRequestsFound() {
        EstadoSolicitud estado = EstadoSolicitud.APROBADA;
        when(solicitudRepo.findByEstadoSolicitud(estado)).thenReturn(Collections.emptyList());

        ApiException ex = assertThrows(ApiException.class, () -> service.findByEstado(estado));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("No hay solicitudes con estado: " + estado);

        verify(solicitudRepo, times(1)).findByEstadoSolicitud(estado);
        verifyNoInteractions(modelMapper, personaHelper);
    }

    @Test
    void findByEstado_shouldPropagateIfPersonaHelperFails() {
        EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

        Long casaId = 3L;
        Casa casa = new Casa();
        casa.setId(casaId);

        SolicitudReparacionLocativa entity = new SolicitudReparacionLocativa();
        entity.setId(2L);
        entity.setCasa(casa);

        SolicitudReparacionLocativaDTO dto = new SolicitudReparacionLocativaDTO();

        when(solicitudRepo.findByEstadoSolicitud(estado)).thenReturn(List.of(entity));
        when(modelMapper.map(entity, SolicitudReparacionLocativaDTO.class)).thenReturn(dto);
        when(personaHelper.obtenerSolicitantePorCasa(casaId))
                .thenThrow(new ApiException("No se encontró solicitante", HttpStatus.BAD_REQUEST));

        ApiException ex = assertThrows(ApiException.class, () -> service.findByEstado(estado));
        assertThat(ex.getMessage()).contains("No se encontró solicitante");

        verify(solicitudRepo).findByEstadoSolicitud(estado);
        verify(modelMapper).map(entity, SolicitudReparacionLocativaDTO.class);
        verify(personaHelper).obtenerSolicitantePorCasa(casaId);
        verify(personaHelper, never()).toPersonaSimpleDTO(any());
    }
}

