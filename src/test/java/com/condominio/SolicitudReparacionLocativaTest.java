package com.condominio;

import com.condominio.dto.response.SolicitudReparacionLocativaDTO;
import com.condominio.dto.response.SolicitudReparacionPropiDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.ReparacionLocativaRepository;
import com.condominio.persistence.repository.SolicitudReparacionLocativaRepository;
import com.condominio.service.implementation.SolicitudReparacionLocativaService;
import com.condominio.util.exception.ApiException;
import com.condominio.util.helper.PersonaHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
    private PersonaRepository personaRepository;

    @Mock
    private ReparacionLocativaRepository reparacionLocativaRepository;

    @InjectMocks
    private SolicitudReparacionLocativaService service;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        Mockito.reset(personaRepository, solicitudRepo, modelMapper, personaHelper, reparacionLocativaRepository);
    }


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
        UserEntity u = new UserEntity(); u.setEmail("ana@example.com");
        persona.setUser(u);

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
    void update_shouldUpdateAndReturnDto_whenValidDates() {

        Long id = 1L;
        SolicitudReparacionLocativa existing = new SolicitudReparacionLocativa();
        existing.setId(id);
        existing.setMotivo("Motivo viejo");
        existing.setResponsable("Responsable viejo");
        existing.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        existing.setFechaRealizacion(LocalDate.now().plusDays(5));

        // DTO: fechaRealizacion must NOT be future (service throws if isAfter(today))
        // inicioObra must be >= today; finObra > inicioObra
        LocalDate now = LocalDate.now();
        SolicitudReparacionLocativaDTO dto = new SolicitudReparacionLocativaDTO();
        dto.setFechaRealizacion(now); // valid (not future)
        dto.setMotivo("Motivo nuevo");
        dto.setResponsable("Responsable nuevo");
        dto.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        dto.setInicioObra(now.plusDays(1));
        dto.setFinObra(now.plusDays(2));
        dto.setTipoObra(TipoObra.ELECTRICA);
        dto.setTipoObraDetalle("Detalle obra");

        SolicitudReparacionLocativa saved = new SolicitudReparacionLocativa();
        saved.setId(id);
        saved.setFechaRealizacion(now);
        saved.setMotivo(dto.getMotivo());
        saved.setResponsable(dto.getResponsable());
        saved.setEstadoSolicitud(dto.getEstadoSolicitud());
        saved.setInicioObra(dto.getInicioObra());
        saved.setFinObra(dto.getFinObra());
        saved.setTipoObra(dto.getTipoObra());
        saved.setTipoObraDetalle(dto.getTipoObraDetalle());

        SolicitudReparacionLocativaDTO mappedDto = new SolicitudReparacionLocativaDTO();

        when(solicitudRepo.findById(id)).thenReturn(Optional.of(existing));
        when(solicitudRepo.save(existing)).thenReturn(saved);
        when(modelMapper.map(saved, SolicitudReparacionLocativaDTO.class)).thenReturn(mappedDto);

        SuccessResult<SolicitudReparacionLocativaDTO> result = service.update(id, dto);

        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Solicitud de Reparacion modificada exitosamente");
        assertThat(result.data()).isEqualTo(mappedDto);

        // service sets fechaRealizacion = LocalDate.now() when updating
        assertThat(existing.getFechaRealizacion()).isEqualTo(now);
        assertThat(existing.getMotivo()).isEqualTo(dto.getMotivo());
        assertThat(existing.getResponsable()).isEqualTo(dto.getResponsable());
        assertThat(existing.getEstadoSolicitud()).isEqualTo(dto.getEstadoSolicitud());
        assertThat(existing.getInicioObra()).isEqualTo(dto.getInicioObra());
        assertThat(existing.getFinObra()).isEqualTo(dto.getFinObra());
        assertThat(existing.getTipoObra()).isEqualTo(dto.getTipoObra());
        assertThat(existing.getTipoObraDetalle()).isEqualTo(dto.getTipoObraDetalle());

        verify(solicitudRepo).findById(id);
        verify(solicitudRepo).save(existing);
        verify(modelMapper).map(saved, SolicitudReparacionLocativaDTO.class);
    }

    @Test
    void update_shouldThrowBadRequest_whenFechaRealizacionIsFuture() {

        Long id = 2L;
        SolicitudReparacionLocativa existing = new SolicitudReparacionLocativa();
        existing.setId(id);
        existing.setFechaRealizacion(LocalDate.now().plusDays(2));

        // DTO with future fechaRealizacion should trigger BAD_REQUEST per service
        SolicitudReparacionLocativaDTO dto = new SolicitudReparacionLocativaDTO();
        dto.setFechaRealizacion(LocalDate.now().plusDays(1)); // future -> invalid

        when(solicitudRepo.findById(id)).thenReturn(Optional.of(existing));

        ApiException ex = assertThrows(ApiException.class, () -> service.update(id, dto));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("Por favor, ingresa una fecha y hora validas");

        verify(solicitudRepo).findById(id);
        verifyNoMoreInteractions(solicitudRepo);
        verifyNoInteractions(modelMapper, personaHelper);
    }

    @Test
    void update_shouldThrowBadRequest_whenInicioObraBeforeToday() {
        Long id = 3L;
        SolicitudReparacionLocativa existing = new SolicitudReparacionLocativa();
        existing.setId(id);

        SolicitudReparacionLocativaDTO dto = new SolicitudReparacionLocativaDTO();
        dto.setFechaRealizacion(LocalDate.now()); // valid
        dto.setInicioObra(LocalDate.now().minusDays(1)); // invalid: before today
        dto.setFinObra(LocalDate.now().plusDays(2));

        when(solicitudRepo.findById(id)).thenReturn(Optional.of(existing));

        ApiException ex = assertThrows(ApiException.class, () -> service.update(id, dto));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("La fecha de inicio de la obra debe ser posterior a la fecha actual");

        verify(solicitudRepo).findById(id);
        verifyNoMoreInteractions(solicitudRepo);
    }

    @Test
    void update_shouldThrowBadRequest_whenFinAntesDeInicio() {
        Long id = 4L;
        SolicitudReparacionLocativa existing = new SolicitudReparacionLocativa();
        existing.setId(id);

        LocalDate now = LocalDate.now();
        SolicitudReparacionLocativaDTO dto = new SolicitudReparacionLocativaDTO();
        dto.setFechaRealizacion(now);
        dto.setInicioObra(now.plusDays(3));
        dto.setFinObra(now.plusDays(2)); // fin before inicio -> invalid

        when(solicitudRepo.findById(id)).thenReturn(Optional.of(existing));

        ApiException ex = assertThrows(ApiException.class, () -> service.update(id, dto));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("La fecha de fin de la obra debe ser posterior a la fecha de inicio");

        verify(solicitudRepo).findById(id);
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
        assertThat(result.message()).isEqualTo("Solicitud de Reparacion desaprobada satisfactoriamente");
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

    @Test
    void crearSolicitud_success_savesWithAuthenticatedUserCasa() {

        String username = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Persona persona = new Persona();
        Casa casa = new Casa(); casa.setId(7L);
        persona.setCasa(casa);

        SolicitudReparacionPropiDTO dto = new SolicitudReparacionPropiDTO();
        dto.setFechaRealizacion(LocalDate.now().plusDays(1));
        dto.setMotivo("Motivo X");
        dto.setResponsable("Responsable Y");
        dto.setInicioObra(LocalDate.now().plusDays(2));
        dto.setFinObra(LocalDate.now().plusDays(3));
        dto.setTipoObra(TipoObra.ELECTRICA);
        dto.setTipoObraDetalle("Detalle");

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(persona));
        when(solicitudRepo.save(any(SolicitudReparacionLocativa.class))).thenAnswer(inv -> {
            SolicitudReparacionLocativa s = inv.getArgument(0);
            s.setId(123L);
            return s;
        });

        SuccessResult<SolicitudReparacionPropiDTO> res = service.crearSolicitud(dto);

        assertThat(res).isNotNull();
        assertThat(res.message()).contains("Solicitud registrada exitosamente");
        assertThat(res.data()).isEqualTo(dto);

        ArgumentCaptor<SolicitudReparacionLocativa> captor = ArgumentCaptor.forClass(SolicitudReparacionLocativa.class);
        verify(solicitudRepo).save(captor.capture());
        SolicitudReparacionLocativa saved = captor.getValue();
        assertThat(saved.getMotivo()).isEqualTo(dto.getMotivo());
        assertThat(saved.getResponsable()).isEqualTo(dto.getResponsable());
        assertThat(saved.getCasa()).isEqualTo(casa);
        assertThat(saved.getEstadoSolicitud()).isEqualTo(EstadoSolicitud.PENDIENTE);
    }

    @Test
    void crearSolicitud_userNotFound_throwsNotFound() {

        String username = "nouser@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        SolicitudReparacionPropiDTO dto = new SolicitudReparacionPropiDTO();
        dto.setFechaRealizacion(LocalDate.now().plusDays(2));
        dto.setMotivo("X");
        dto.setResponsable("Y");
        dto.setInicioObra(LocalDate.now().plusDays(3));
        dto.setFinObra(LocalDate.now().plusDays(4));

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.crearSolicitud(dto));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("Usuario no encontrado");

        verify(solicitudRepo, never()).save(any());
    }

    // eliminar and modificar tests remain same but adjusted where service mutates fechaRealizacion to now
    @Test
    void eliminar_success_deletesWhenOwnerAndPending() {

        String username = "owner@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Long id = 10L;
        Casa casa = new Casa(); casa.setId(50L);

        SolicitudReparacionLocativa solicitud = new SolicitudReparacionLocativa();
        solicitud.setId(id);
        solicitud.setCasa(casa);
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);

        Persona persona = new Persona();
        persona.setCasa(casa);

        SolicitudReparacionLocativaDTO mapped = new SolicitudReparacionLocativaDTO();

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(persona));
        when(solicitudRepo.findById(id)).thenReturn(Optional.of(solicitud));
        when(modelMapper.map(solicitud, SolicitudReparacionLocativaDTO.class)).thenReturn(mapped);

        SuccessResult<SolicitudReparacionLocativaDTO> res = service.eliminar(id);

        assertThat(res).isNotNull();
        assertThat(res.message()).isEqualTo("Solicitud eliminada exitosamente");
        assertThat(res.data()).isEqualTo(mapped);

        verify(solicitudRepo).findById(id);
        verify(solicitudRepo).delete(solicitud);
        verify(modelMapper).map(solicitud, SolicitudReparacionLocativaDTO.class);
    }

    @Test
    void eliminar_notPending_throwsBadRequest() {

        String username = "owner@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Long id = 11L;
        Casa casa = new Casa(); casa.setId(51L);

        SolicitudReparacionLocativa solicitud = new SolicitudReparacionLocativa();
        solicitud.setId(id);
        solicitud.setCasa(casa);
        solicitud.setEstadoSolicitud(EstadoSolicitud.APROBADA);

        Persona persona = new Persona();
        persona.setCasa(casa);

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(persona));
        when(solicitudRepo.findById(id)).thenReturn(Optional.of(solicitud));

        ApiException ex = assertThrows(ApiException.class, () -> service.eliminar(id));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("No se puede eliminar una solicitud que ya ha sido aprobada o rechazada");

        verify(solicitudRepo, never()).delete(any());
    }

    @Test
    void eliminar_forbidden_whenUserNotOwner() {

        String username = "intruder@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Long id = 12L;
        Casa casaSolicitud = new Casa(); casaSolicitud.setId(60L);
        SolicitudReparacionLocativa solicitud = new SolicitudReparacionLocativa();
        solicitud.setId(id);
        solicitud.setCasa(casaSolicitud);
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);

        Persona personaOther = new Persona();
        Casa otraCasa = new Casa(); otraCasa.setId(999L);
        personaOther.setCasa(otraCasa);

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(personaOther));
        when(solicitudRepo.findById(id)).thenReturn(Optional.of(solicitud));

        ApiException ex = assertThrows(ApiException.class, () -> service.eliminar(id));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getMessage()).contains("No autorizado");

        verify(solicitudRepo, never()).delete(any());
    }

    @Test
    void eliminar_notFound_whenSolicitudMissing() {

        String username = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Long id = 99L;
        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(new Persona()));
        when(solicitudRepo.findById(id)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.eliminar(id));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(solicitudRepo).findById(id);
        verify(solicitudRepo, never()).delete(any());
    }

    @Test
    void modificarSolicitud_success_whenOwnerAndPending() {

        String username = "owner2@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Long id = 20L;
        Casa casa = new Casa(); casa.setId(70L);

        SolicitudReparacionLocativa existing = new SolicitudReparacionLocativa();
        existing.setId(id);
        existing.setCasa(casa);
        existing.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);

        Persona persona = new Persona();
        persona.setCasa(casa);

        LocalDate now = LocalDate.now();
        SolicitudReparacionPropiDTO dto = new SolicitudReparacionPropiDTO();
        dto.setFechaRealizacion(now.plusDays(5)); // service will set fechaRealizacion to now when saving
        dto.setMotivo("Reparar ventana");
        dto.setResponsable("Juan");
        dto.setInicioObra(now.plusDays(6));
        dto.setFinObra(now.plusDays(7));
        dto.setTipoObra(TipoObra.OBRA_BLANCA);
        dto.setTipoObraDetalle("Detalle");

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(persona));
        when(solicitudRepo.findById(id)).thenReturn(Optional.of(existing));
        when(solicitudRepo.save(existing)).thenReturn(existing);

        SuccessResult<SolicitudReparacionPropiDTO> res = service.modificarSolicitud(id, dto);

        assertThat(res).isNotNull();
        assertThat(res.message()).isEqualTo("Solicitud modificada exitosamente");
        assertThat(res.data()).isEqualTo(dto);

        // service sets fechaRealizacion = LocalDate.now()
        assertThat(existing.getFechaRealizacion()).isEqualTo(LocalDate.now());
        assertThat(existing.getMotivo()).isEqualTo(dto.getMotivo());
        assertThat(existing.getResponsable()).isEqualTo(dto.getResponsable());
        assertThat(existing.getInicioObra()).isEqualTo(dto.getInicioObra());
        assertThat(existing.getFinObra()).isEqualTo(dto.getFinObra());
        assertThat(existing.getTipoObra()).isEqualTo(dto.getTipoObra());
        assertThat(existing.getTipoObraDetalle()).isEqualTo(dto.getTipoObraDetalle());
        verify(solicitudRepo).save(existing);
    }

    @Test
    void modificarSolicitud_badRequest_whenNotPending() {

        String username = "owner2@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Long id = 21L;
        Casa casa = new Casa(); casa.setId(71L);

        SolicitudReparacionLocativa existing = new SolicitudReparacionLocativa();
        existing.setId(id);
        existing.setCasa(casa);
        existing.setEstadoSolicitud(EstadoSolicitud.APROBADA);

        Persona persona = new Persona();
        persona.setCasa(casa);

        SolicitudReparacionPropiDTO dto = new SolicitudReparacionPropiDTO();
        dto.setFechaRealizacion(LocalDate.now().plusDays(5));
        dto.setMotivo("X");
        dto.setResponsable("Y");
        dto.setInicioObra(LocalDate.now().plusDays(6));
        dto.setFinObra(LocalDate.now().plusDays(7));

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(persona));
        when(solicitudRepo.findById(id)).thenReturn(Optional.of(existing));

        ApiException ex = assertThrows(ApiException.class, () -> service.modificarSolicitud(id, dto));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("No se puede modificar una solicitud que ya ha sido aprobada o rechazada");

        verify(solicitudRepo, never()).save(any());
    }

    @Test
    void modificarSolicitud_forbidden_whenUserNotOwner() {

        String username = "intruder2@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Long id = 22L;
        Casa casaSolicitud = new Casa(); casaSolicitud.setId(80L);

        SolicitudReparacionLocativa existing = new SolicitudReparacionLocativa();
        existing.setId(id);
        existing.setCasa(casaSolicitud);
        existing.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);

        Persona personaOther = new Persona();
        Casa otraCasa = new Casa(); otraCasa.setId(9999L);
        personaOther.setCasa(otraCasa);

        SolicitudReparacionPropiDTO dto = new SolicitudReparacionPropiDTO();
        dto.setFechaRealizacion(LocalDate.now().plusDays(3));
        dto.setMotivo("X");
        dto.setResponsable("Y");
        dto.setInicioObra(LocalDate.now().plusDays(4));
        dto.setFinObra(LocalDate.now().plusDays(5));

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(personaOther));
        when(solicitudRepo.findById(id)).thenReturn(Optional.of(existing));

        ApiException ex = assertThrows(ApiException.class, () -> service.modificarSolicitud(id, dto));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getMessage()).contains("No autorizado");

        verify(solicitudRepo, never()).save(any());
    }

    @Test
    void modificarSolicitud_notFound_whenSolicitudMissing() {

        String username = "owner3@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Long id = 999L;
        SolicitudReparacionPropiDTO dto = new SolicitudReparacionPropiDTO();
        dto.setFechaRealizacion(LocalDate.now().plusDays(2));
        dto.setMotivo("a");
        dto.setResponsable("b");

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(new Persona()));
        when(solicitudRepo.findById(id)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.modificarSolicitud(id, dto));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(solicitudRepo).findById(id);
        verify(solicitudRepo, never()).save(any());
    }
}

