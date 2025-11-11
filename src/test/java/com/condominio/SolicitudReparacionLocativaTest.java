package com.condominio;

import com.condominio.dto.response.SolicitudReparacionLocativaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.ReparacionLocativaRepository;
import com.condominio.persistence.repository.SolicitudReparacionLocativaRepository;
import com.condominio.service.implementation.SolicitudReparacionLocativaService;
import com.condominio.util.exception.ApiException;
import com.condominio.util.helper.PersonaHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    @Mock
    private ReparacionLocativaRepository reparacionLocativaRepository;

    @InjectMocks
    private SolicitudReparacionLocativaService service;


    @Test
    void findByEstado_shouldReturnDtosWithSolicitante_whenThereAreRequests() {

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

        when(solicitudRepo.findByEstadoSolicitud(estado)).thenReturn(List.of(entity));
        when(modelMapper.map(entity, SolicitudReparacionLocativaDTO.class)).thenReturn(dto);
        when(personaHelper.obtenerSolicitantePorCasa(casaId)).thenReturn(persona);

        var personaSimple = com.condominio.dto.response.PersonaSimpleDTO.builder()
                .nombreCompleto("Ana Lopez")
                .telefono(null)
                .correo("ana@example.com")
                .build();
        when(personaHelper.toPersonaSimpleDTO(persona)).thenReturn(personaSimple);

        SuccessResult<List<SolicitudReparacionLocativaDTO>> result = service.findByEstado(estado);

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

    @Test
    void update_shouldUpdateAndReturnDto_whenValidDate() {

        Long id = 1L;
        SolicitudReparacionLocativa existing = new SolicitudReparacionLocativa();
        existing.setId(id);
        existing.setMotivo("Motivo viejo");
        existing.setResponsable("Responsable viejo");
        existing.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        existing.setFechaRealizacion(LocalDate.now().plusDays(5));

        SolicitudReparacionLocativaDTO dto = new SolicitudReparacionLocativaDTO();
        dto.setFechaRealizacion(LocalDate.now().plusDays(10)); // fecha válida (futura)
        dto.setMotivo("Motivo nuevo");
        dto.setResponsable("Responsable nuevo");
        dto.setEstadoSolicitud(EstadoSolicitud.APROBADA);

        SolicitudReparacionLocativa saved = new SolicitudReparacionLocativa();
        saved.setId(id);
        saved.setFechaRealizacion(dto.getFechaRealizacion());
        saved.setMotivo(dto.getMotivo());
        saved.setResponsable(dto.getResponsable());
        saved.setEstadoSolicitud(dto.getEstadoSolicitud());

        SolicitudReparacionLocativaDTO mappedDto = new SolicitudReparacionLocativaDTO();

        when(solicitudRepo.findById(id)).thenReturn(Optional.of(existing));
        when(solicitudRepo.save(existing)).thenReturn(saved);
        when(modelMapper.map(saved, SolicitudReparacionLocativaDTO.class)).thenReturn(mappedDto);

        SuccessResult<SolicitudReparacionLocativaDTO> result = service.update(id, dto);

        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Solicitud de Reparacion modificada exitosamente");
        assertThat(result.data()).isEqualTo(mappedDto);

        assertThat(existing.getFechaRealizacion()).isEqualTo(dto.getFechaRealizacion());
        assertThat(existing.getMotivo()).isEqualTo(dto.getMotivo());
        assertThat(existing.getResponsable()).isEqualTo(dto.getResponsable());
        assertThat(existing.getEstadoSolicitud()).isEqualTo(dto.getEstadoSolicitud());

        verify(solicitudRepo).findById(id);
        verify(solicitudRepo).save(existing);
        verify(modelMapper).map(saved, SolicitudReparacionLocativaDTO.class);
    }

    @Test
    void update_shouldThrowBadRequest_whenFechaRealizacionIsBeforeToday() {

        Long id = 2L;
        SolicitudReparacionLocativa existing = new SolicitudReparacionLocativa();
        existing.setId(id);
        existing.setFechaRealizacion(LocalDate.now().plusDays(2));

        SolicitudReparacionLocativaDTO dto = new SolicitudReparacionLocativaDTO();
        dto.setFechaRealizacion(LocalDate.now().minusDays(1)); // fecha inválida (pasada)
        dto.setMotivo("x");

        when(solicitudRepo.findById(id)).thenReturn(Optional.of(existing));

        ApiException ex = assertThrows(ApiException.class, () -> service.update(id, dto));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("Por favor, ingresa una fecha y hora validas");

        verify(solicitudRepo).findById(id);
        verifyNoMoreInteractions(solicitudRepo);
        verifyNoInteractions(modelMapper, personaHelper);
    }

    @Test
    void update_shouldThrowNotFound_whenSolicitudMissing() {
        Long id = 99L;
        SolicitudReparacionLocativaDTO dto = new SolicitudReparacionLocativaDTO();
        when(solicitudRepo.findById(id)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.update(id, dto));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(solicitudRepo).findById(id);
        verifyNoInteractions(modelMapper, personaHelper);
    }

    @Test
    void saveNewSoliAndReparacion_shouldSaveEntitiesAndReturnDto() {

        SolicitudReparacionLocativa input = new SolicitudReparacionLocativa();
        input.setId(100L);

        Persona solicitante = new Persona();
        solicitante.setId(200L);

        SolicitudReparacionLocativa nuevaSoli = new SolicitudReparacionLocativa();
        nuevaSoli.setId(100L);

        SolicitudReparacionLocativaDTO mappedDto = new SolicitudReparacionLocativaDTO();

        when(solicitudRepo.save(input)).thenReturn(nuevaSoli);
        when(modelMapper.map(nuevaSoli, SolicitudReparacionLocativaDTO.class)).thenReturn(mappedDto);

        com.condominio.dto.response.PersonaSimpleDTO personaSimple = com.condominio.dto.response.PersonaSimpleDTO.builder()
                .nombreCompleto("Pedro").telefono(null).correo("p@e.com").build();
        when(personaHelper.toPersonaSimpleDTO(solicitante)).thenReturn(personaSimple);

        when(reparacionLocativaRepository.save(any(ReparacionLocativa.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SolicitudReparacionLocativaDTO result = service.saveNewSoliAndReparacion(input, solicitante);

        assertThat(result).isEqualTo(mappedDto);
        assertThat(result.getSolicitante()).isEqualTo(personaSimple);

        verify(solicitudRepo).save(input);
        verify(modelMapper).map(nuevaSoli, SolicitudReparacionLocativaDTO.class);
        verify(personaHelper).toPersonaSimpleDTO(solicitante);
        ArgumentCaptor<ReparacionLocativa> captor = ArgumentCaptor.forClass(ReparacionLocativa.class);
        verify(reparacionLocativaRepository).save(captor.capture());
        ReparacionLocativa saved = captor.getValue();
        assertThat(saved.getSolicitudReparacionLocativa()).isEqualTo(nuevaSoli);
        assertThat(saved.isEstado()).isTrue();
    }

    @Test
    void aprobar_shouldApproveAndCreateReparacion_whenSolicitudExists() {

        Long id = 3L;
        Casa casa = new Casa();
        casa.setId(7L);

        SolicitudReparacionLocativa solicitud = new SolicitudReparacionLocativa();
        solicitud.setId(id);
        solicitud.setCasa(casa);
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);

        Persona solicitante = new Persona();
        UserEntity u = new UserEntity(); u.setEmail("solicitante@example.com");
        solicitante.setUser(u);

        SolicitudReparacionLocativa nuevaSoli = new SolicitudReparacionLocativa();
        nuevaSoli.setId(id);

        SolicitudReparacionLocativaDTO mappedDto = new SolicitudReparacionLocativaDTO();

        when(solicitudRepo.findById(id)).thenReturn(Optional.of(solicitud));
        when(personaHelper.obtenerSolicitantePorCasa(casa.getId())).thenReturn(solicitante);

        when(solicitudRepo.save(solicitud)).thenReturn(nuevaSoli);
        when(modelMapper.map(nuevaSoli, SolicitudReparacionLocativaDTO.class)).thenReturn(mappedDto);
        when(personaHelper.toPersonaSimpleDTO(solicitante)).thenReturn(
                com.condominio.dto.response.PersonaSimpleDTO.builder()
                        .nombreCompleto("Solicitante").correo("sol@example.com").build()
        );
        when(reparacionLocativaRepository.save(any(ReparacionLocativa.class))).thenAnswer(inv -> inv.getArgument(0));

        SuccessResult<SolicitudReparacionLocativaDTO> result = service.aprobar(id);

        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Solicitud de Reparacion aprobada satisfactoriamente");
        assertThat(result.data()).isEqualTo(mappedDto);

        verify(solicitudRepo).findById(id);
        verify(personaHelper).obtenerSolicitantePorCasa(casa.getId());
        verify(solicitudRepo).save(solicitud);
        verify(modelMapper).map(nuevaSoli, SolicitudReparacionLocativaDTO.class);
        verify(reparacionLocativaRepository).save(any(ReparacionLocativa.class));
    }

    @Test
    void aprobar_shouldThrowNotFound_whenSolicitudMissing() {
        Long id = 44L;
        when(solicitudRepo.findById(id)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.aprobar(id));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(solicitudRepo).findById(id);
        verifyNoInteractions(personaHelper);
    }

    @Test
    void rechazar_shouldRejectAndReturnDto_withComentarios() {
        // Arrange
        Long id = 5L;
        Casa casa = new Casa();
        casa.setId(11L);

        SolicitudReparacionLocativa solicitud = new SolicitudReparacionLocativa();
        solicitud.setId(id);
        solicitud.setCasa(casa);
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);

        Persona solicitante = new Persona();
        UserEntity u = new UserEntity(); u.setEmail("sol@example.com");
        solicitante.setUser(u);

        SolicitudReparacionLocativa nuevaSoli = new SolicitudReparacionLocativa();
        nuevaSoli.setId(id);

        SolicitudReparacionLocativaDTO mappedDto = new SolicitudReparacionLocativaDTO();

        when(solicitudRepo.findById(id)).thenReturn(Optional.of(solicitud));
        when(personaHelper.obtenerSolicitantePorCasa(casa.getId())).thenReturn(solicitante);

        when(solicitudRepo.save(solicitud)).thenReturn(nuevaSoli);
        when(modelMapper.map(nuevaSoli, SolicitudReparacionLocativaDTO.class)).thenReturn(mappedDto);
        when(personaHelper.toPersonaSimpleDTO(solicitante)).thenReturn(
                com.condominio.dto.response.PersonaSimpleDTO.builder()
                        .nombreCompleto("Solic").correo("sol@example.com").build()
        );
        when(reparacionLocativaRepository.save(any(ReparacionLocativa.class))).thenAnswer(inv -> inv.getArgument(0));

        String comentarios = "Comentario de prueba";

        SuccessResult<SolicitudReparacionLocativaDTO> result = service.rechazar(id, comentarios);

        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Solicitud de Reparacion desaprobada");
        assertThat(result.data()).isEqualTo(mappedDto);
        assertThat(result.data().getComentarios()).isEqualTo(comentarios);

        verify(solicitudRepo).findById(id);
        verify(personaHelper).obtenerSolicitantePorCasa(casa.getId());
        verify(solicitudRepo).save(solicitud);
        verify(reparacionLocativaRepository).save(any(ReparacionLocativa.class));
        verify(modelMapper).map(nuevaSoli, SolicitudReparacionLocativaDTO.class);
    }

    @Test
    void rechazar_shouldThrowNotFound_whenSolicitudMissing() {
        Long id = 66L;
        when(solicitudRepo.findById(id)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.rechazar(id, "x"));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(solicitudRepo).findById(id);
        verifyNoInteractions(personaHelper);
    }
}

