package com.condominio;


import com.condominio.dto.response.*;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.*;
import com.condominio.service.implementation.SolicitudReservaRecursoService;
import com.condominio.util.events.RepliedSolicitudEvent;
import com.condominio.util.exception.ApiException;
import com.condominio.util.helper.PersonaHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudReservaRecursoServiceTest {

    @Mock
    private SolicitudReservaRecursoRepository solicitudReservaRecursoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private RecursoComunRepository recursoComunRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PersonaHelper personaHelper;

    @Mock
    private CasaRepository casaRepository;

    @InjectMocks
    private SolicitudReservaRecursoService solicitudReservaRecursoService;

    @Test
    void testFindByEstado_ShouldReturnPendientes() {

        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        Casa newCasa = new Casa();
        newCasa.setId(1L);
        solicitud.setCasa(newCasa);

        Persona persona = new Persona();
        persona.setPrimerNombre("Carlos");
        persona.setPrimerApellido("Pérez");
        persona.setTelefono(123456789L);

        UserEntity user = new UserEntity();
        user.setEmail("carlos@example.com");
        persona.setUser(user);

        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(1L)).thenReturn(Optional.of(persona));
        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        SuccessResult<List<SolicitudReservaRecursoDTO>> result =
                solicitudReservaRecursoService.findByEstado(EstadoSolicitud.PENDIENTE);

        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;
        assertThat(result.message())
                .contains("Solicitudes " + estado.name().toLowerCase() + " obtenidas correctamente");
    }

    @Test
    void testFindByEstado_ShouldThrow_WhenEmpty() {
        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.APROBADA))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> solicitudReservaRecursoService.findByEstado(EstadoSolicitud.APROBADA))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No hay solicitudes con estado: APROBADA");
    }

    @Test
    void testFindByEstado_ShouldThrow_WhenNoSolicitanteFound() {
        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        Casa newCasa = new Casa();
        newCasa.setId(2L);
        solicitud.setCasa(newCasa);


        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(2L)).thenReturn(Optional.empty());
        when(personaRepository.findPropietarioByCasaId(2L)).thenReturn(Optional.empty());
        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        assertThatThrownBy(() -> solicitudReservaRecursoService.findByEstado(EstadoSolicitud.PENDIENTE))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No se encontro un solicitante");
    }

    @Test
    void testFindByEstado_ShouldReturnAprobadas() {

        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        Casa newCasa = new Casa();
        newCasa.setId(1L);
        solicitud.setCasa(newCasa);

        Persona persona = new Persona();
        persona.setPrimerNombre("María");
        persona.setPrimerApellido("Gómez");
        persona.setTelefono(987654321L);

        UserEntity user = new UserEntity();
        user.setEmail("maria@example.com");
        persona.setUser(user);

        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.APROBADA))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(1L)).thenReturn(Optional.of(persona));
        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        SuccessResult<List<SolicitudReservaRecursoDTO>> result =
                solicitudReservaRecursoService.findByEstado(EstadoSolicitud.APROBADA);

        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        EstadoSolicitud estado = EstadoSolicitud.APROBADA;
        assertThat(result.message())
                .contains("Solicitudes " + estado.name().toLowerCase() + " obtenidas correctamente");
    }

    @Test
    void testFindByEstado_ShouldReturnRechazadas() {

        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setEstadoSolicitud(EstadoSolicitud.RECHAZADA);
        Casa newCasa = new Casa();
        newCasa.setId(1L);
        solicitud.setCasa(newCasa);

        Persona persona = new Persona();
        persona.setPrimerNombre("Laura");
        persona.setPrimerApellido("Torres");
        persona.setTelefono(321654987L);

        UserEntity user = new UserEntity();
        user.setEmail("laura@example.com");
        persona.setUser(user);

        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.RECHAZADA))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(1L)).thenReturn(Optional.of(persona));
        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        SuccessResult<List<SolicitudReservaRecursoDTO>> result =
                solicitudReservaRecursoService.findByEstado(EstadoSolicitud.RECHAZADA);

        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        EstadoSolicitud estado = EstadoSolicitud.RECHAZADA;
        assertThat(result.message())
                .contains("Solicitudes " + estado.name().toLowerCase() + " obtenidas correctamente");
    }

    @Test
    void testFindByEstado_ShouldThrowException_WhenNoSolicitudes() {
        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of());

        assertThatThrownBy(() -> solicitudReservaRecursoService.findByEstado(EstadoSolicitud.PENDIENTE))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No hay solicitudes con estado: PENDIENTE");
    }
    @Test
    void testFindByEstado_ShouldThrowException_WhenNoSolicitanteFound() {
        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        Casa casa = new Casa();
        casa.setId(1L);
        solicitud.setCasa(casa);

        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(1L)).thenReturn(Optional.empty());
        when(personaRepository.findPropietarioByCasaId(1L)).thenReturn(Optional.empty());
        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        assertThatThrownBy(() -> solicitudReservaRecursoService.findByEstado(EstadoSolicitud.PENDIENTE))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No se encontro un solicitante (arrendatario o propietario)");
    }

    @Test
    void testFindByEstado_ShouldUsePropietario_WhenNoArrendatario() {
        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        Casa casa = new Casa();
        casa.setId(1L);
        solicitud.setCasa(casa);

        Persona propietario = new Persona();
        propietario.setPrimerNombre("Ana");
        propietario.setPrimerApellido("Lopez");
        propietario.setTelefono(123456789L);
        UserEntity user = new UserEntity();
        user.setEmail("ana@example.com");
        propietario.setUser(user);

        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(1L)).thenReturn(Optional.empty());
        when(personaRepository.findPropietarioByCasaId(1L)).thenReturn(Optional.of(propietario));
        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        SuccessResult<List<SolicitudReservaRecursoDTO>> result =
                solicitudReservaRecursoService.findByEstado(EstadoSolicitud.PENDIENTE);

        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        assertThat(result.message()).contains("Solicitudes pendiente");
    }

    @Test
    void aprobar_shouldApprove_whenPending_andResourceEnabled() {
        // Arrange
        Long id = 5L;
        Casa casa = new Casa();
        casa.setId(5L);

        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setId(id);
        solicitud.setCasa(casa); // <- evita NPE
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaSolicitud(LocalDate.now().plusDays(1));
        solicitud.setHoraInicio(LocalTime.of(10, 0));
        solicitud.setHoraFin(LocalTime.of(12, 0));
        solicitud.setNumeroInvitados(4);

        RecursoComun recurso = new RecursoComun();
        recurso.setDisponibilidadRecurso(DisponibilidadRecurso.DISPONIBLE);
        solicitud.setRecursoComun(recurso);

        Persona persona = new Persona();
        UserEntity user = new UserEntity(); user.setEmail("juan@ejemplo.com");
        persona.setUser(user);

        SolicitudReservaRecurso saved = new SolicitudReservaRecurso();
        saved.setId(id);
        saved.setEstadoSolicitud(EstadoSolicitud.APROBADA);

        SolicitudReservaRecursoDTO dto = new SolicitudReservaRecursoDTO();
        when(solicitudReservaRecursoRepository.findById(id)).thenReturn(Optional.of(solicitud));
        when(personaHelper.obtenerSolicitantePorCasa(casa.getId())).thenReturn(persona);
        when(solicitudReservaRecursoRepository.save(any(SolicitudReservaRecurso.class))).thenReturn(saved);
        when(modelMapper.map(saved, SolicitudReservaRecursoDTO.class)).thenReturn(dto);
        when(personaHelper.toPersonaSimpleDTO(persona)).thenReturn(
                PersonaSimpleDTO.builder()
                        .nombreCompleto("Juan Ejemplo")
                        .telefono(null)
                        .correo("juan@ejemplo.com")
                        .build()
        );
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SuccessResult<SolicitudReservaRecursoDTO> result = solicitudReservaRecursoService.aprobar(id);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Reserva aprobada correctamente");
        verify(solicitudReservaRecursoRepository).findById(id);

        ArgumentCaptor<SolicitudReservaRecurso> captor = ArgumentCaptor.forClass(SolicitudReservaRecurso.class);
        verify(solicitudReservaRecursoRepository).save(captor.capture());
        assertThat(captor.getValue().getEstadoSolicitud()).isEqualTo(EstadoSolicitud.APROBADA);

        // Verifica que se creo una Reserva
        verify(reservaRepository).save(any(Reserva.class));

        // Verifica que se publico el evento
        ArgumentCaptor<RepliedSolicitudEvent> evtCap = ArgumentCaptor.forClass(RepliedSolicitudEvent.class);
        verify(eventPublisher).publishEvent(evtCap.capture());
        assertThat(evtCap.getValue().getEmailPropietario()).isEqualTo("juan@ejemplo.com");
    }

    @Test
    void aprobar_shouldThrowNotFound_whenMissing() {
        Long id = 6L;
        when(solicitudReservaRecursoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudReservaRecursoService.aprobar(id))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No se ha encontrado la solicitud");
        verify(solicitudReservaRecursoRepository).findById(id);
    }

    @Test
    void aprobar_shouldThrowBadRequest_whenNotPending() {
        Long id = 7L;
        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setId(id);
        solicitud.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        solicitud.setRecursoComun(new RecursoComun(){{
            setDisponibilidadRecurso(DisponibilidadRecurso.DISPONIBLE);
        }});

        when(solicitudReservaRecursoRepository.findById(id)).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() -> solicitudReservaRecursoService.aprobar(id))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Solo se pueden gestionar reservas pendientes");
    }

    @Test
    void aprobar_shouldThrowBadRequest_whenResourceDisabled() {
        Long id = 8L;
        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setId(id);
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        RecursoComun recurso = new RecursoComun();
        recurso.setDisponibilidadRecurso(DisponibilidadRecurso.NO_DISPONIBLE);
        solicitud.setRecursoComun(recurso);

        when(solicitudReservaRecursoRepository.findById(id)).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() -> solicitudReservaRecursoService.aprobar(id))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No se puede aprobar una reserva de un recurso deshabilitado");
    }

    @Test
    void rechazar_shouldReject_whenPending_andResourceEnabled() {
        // Arrange
        Long id = 9L;

        Casa casa = new Casa();
        casa.setId(99L);

        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setId(id);
        solicitud.setCasa(casa); // <- importante: evita NPE al obtener casaId
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaSolicitud(LocalDate.now().plusDays(1));
        solicitud.setHoraInicio(LocalTime.of(10, 0));
        solicitud.setHoraFin(LocalTime.of(12, 0));

        RecursoComun recurso = new RecursoComun();
        recurso.setDisponibilidadRecurso(DisponibilidadRecurso.DISPONIBLE);
        solicitud.setRecursoComun(recurso);

        // solicitante (para el evento)
        Persona persona = new Persona();
        UserEntity user = new UserEntity();
        user.setEmail("usuario@example.com");
        persona.setUser(user);

        SolicitudReservaRecurso saved = new SolicitudReservaRecurso();
        saved.setId(id);
        saved.setEstadoSolicitud(EstadoSolicitud.RECHAZADA);

        when(solicitudReservaRecursoRepository.findById(id)).thenReturn(Optional.of(solicitud));
        when(personaHelper.obtenerSolicitantePorCasa(casa.getId())).thenReturn(persona);
        when(solicitudReservaRecursoRepository.save(any(SolicitudReservaRecurso.class))).thenReturn(saved);
        when(modelMapper.map(saved, SolicitudReservaRecursoDTO.class)).thenReturn(new SolicitudReservaRecursoDTO());

        // Act
        SuccessResult<SolicitudReservaRecursoDTO> result = solicitudReservaRecursoService.rechazar(id);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Reserva rechazada correctamente");

        ArgumentCaptor<SolicitudReservaRecurso> captor = ArgumentCaptor.forClass(SolicitudReservaRecurso.class);
        verify(solicitudReservaRecursoRepository).save(captor.capture());
        assertThat(captor.getValue().getEstadoSolicitud()).isEqualTo(EstadoSolicitud.RECHAZADA);

        // Verifica que se publicó el evento con el email del solicitante
        ArgumentCaptor<RepliedSolicitudEvent> evtCap = ArgumentCaptor.forClass(RepliedSolicitudEvent.class);
        verify(eventPublisher).publishEvent(evtCap.capture());
        assertThat(evtCap.getValue().getEmailPropietario()).isEqualTo("usuario@example.com");
    }

    @Test
    void rechazar_shouldThrowNotFound_whenMissing() {
        Long id = 10L;
        when(solicitudReservaRecursoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudReservaRecursoService.rechazar(id))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No se ha encontrado la solicitud");
    }

    @Test
    void rechazar_shouldThrowBadRequest_whenNotPending() {
        Long id = 11L;
        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setId(id);
        solicitud.setEstadoSolicitud(EstadoSolicitud.RECHAZADA);
        solicitud.setRecursoComun(new RecursoComun(){{
            setDisponibilidadRecurso(DisponibilidadRecurso.DISPONIBLE);
        }});

        when(solicitudReservaRecursoRepository.findById(id)).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() -> solicitudReservaRecursoService.rechazar(id))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Solo se pueden gestionar reservas pendientes");
    }

    @Test
    void rechazar_shouldThrowBadRequest_whenResourceDisabled() {
        Long id = 12L;
        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setId(id);
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        RecursoComun recurso = new RecursoComun();
        recurso.setDisponibilidadRecurso(DisponibilidadRecurso.NO_DISPONIBLE);
        solicitud.setRecursoComun(recurso);

        when(solicitudReservaRecursoRepository.findById(id)).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() -> solicitudReservaRecursoService.rechazar(id))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No se puede aprobar una reserva de un recurso deshabilitado");
    }

    @Test
    void cancelar_shouldDeleteWhenApproved_andDateBeforeYesterday() {

        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setId(1L);
        solicitud.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        solicitud.setFechaSolicitud(LocalDate.now().minusDays(2));

        when(solicitudReservaRecursoRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(modelMapper.map(solicitud, SolicitudReservaRecursoDTO.class))
                .thenReturn(new SolicitudReservaRecursoDTO());

        SuccessResult<SolicitudReservaRecursoDTO> result = solicitudReservaRecursoService.cancelar(1L);

        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Reserva cancelada exitosamente");
        verify(solicitudReservaRecursoRepository).findById(1L);
        verify(solicitudReservaRecursoRepository).delete(solicitud);
    }

    @Test
    void cancelar_shouldThrowNotFound_whenMissing() {

        when(solicitudReservaRecursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudReservaRecursoService.cancelar(99L))
                .isInstanceOf(ApiException.class)
                .hasMessage("No se ha encontrado la solicitud")
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });

        verify(solicitudReservaRecursoRepository).findById(99L);
        verify(solicitudReservaRecursoRepository, never()).delete(any());
    }

    @Test
    void cancelar_shouldThrowBadRequest_ifNotApproved() {

        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setId(2L);
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaSolicitud(LocalDate.now().minusDays(2));

        when(solicitudReservaRecursoRepository.findById(2L)).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() -> solicitudReservaRecursoService.cancelar(2L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Solo se pueden cancelar reservas aprobadas")
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        verify(solicitudReservaRecursoRepository).findById(2L);
        verify(solicitudReservaRecursoRepository, never()).delete(any());
    }

    @Test
    void cancelar_shouldThrowBadRequest_ifDateNotBeforeYesterday() {

        SolicitudReservaRecurso solicitudAyer = new SolicitudReservaRecurso();
        solicitudAyer.setId(3L);
        solicitudAyer.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        solicitudAyer.setFechaSolicitud(LocalDate.now().minusDays(1));

        when(solicitudReservaRecursoRepository.findById(3L)).thenReturn(Optional.of(solicitudAyer));

        assertThatThrownBy(() -> solicitudReservaRecursoService.cancelar(3L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Solo se permiten cancelar reservas posteriores a la fecha de ayer")
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        verify(solicitudReservaRecursoRepository).findById(3L);
        verify(solicitudReservaRecursoRepository, never()).delete(any());

        SolicitudReservaRecurso solicitudHoy = new SolicitudReservaRecurso();
        solicitudHoy.setId(4L);
        solicitudHoy.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        solicitudHoy.setFechaSolicitud(LocalDate.now());

        when(solicitudReservaRecursoRepository.findById(4L)).thenReturn(Optional.of(solicitudHoy));

        assertThatThrownBy(() -> solicitudReservaRecursoService.cancelar(4L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Solo se permiten cancelar reservas posteriores a la fecha de ayer")
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        verify(solicitudReservaRecursoRepository).findById(4L);
        verify(solicitudReservaRecursoRepository, never()).delete(any());
    }

    @Test
    void update_shouldModifyAndSave_whenValid() {

        Long id = 1L;

        SolicitudReservaRecurso existing = new SolicitudReservaRecurso();
        existing.setId(id);
        existing.setFechaSolicitud(LocalDate.now().plusDays(2));
        existing.setHoraInicio(LocalTime.of(10,0));
        existing.setHoraFin(LocalTime.of(12,0));
        existing.setNumeroInvitados(5);

        when(solicitudReservaRecursoRepository.findById(id)).thenReturn(Optional.of(existing));

        SolicitudReservaRecursoDTO dto = new SolicitudReservaRecursoDTO();
        dto.setFechaSolicitud(LocalDate.now().plusDays(3));
        dto.setHoraInicio(LocalTime.of(14, 0));
        dto.setHoraFin(LocalTime.of(16, 0));
        dto.setNumeroInvitados(8);

        RecursoComun recurso = new RecursoComun();
        recurso.setDisponibilidadRecurso(DisponibilidadRecurso.DISPONIBLE);
        dto.setRecursoComun(recurso);

        SolicitudReservaRecurso saved = new SolicitudReservaRecurso();
        saved.setId(id);
        saved.setFechaSolicitud(dto.getFechaSolicitud());
        saved.setHoraInicio(dto.getHoraInicio());
        saved.setHoraFin(dto.getHoraFin());
        saved.setNumeroInvitados(dto.getNumeroInvitados());

        when(solicitudReservaRecursoRepository.save(any(SolicitudReservaRecurso.class))).thenReturn(saved);
        when(modelMapper.map(saved, SolicitudReservaRecursoDTO.class)).thenReturn(dto);

        SuccessResult<SolicitudReservaRecursoDTO> result = solicitudReservaRecursoService.update(id, dto);

        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Reserva modificada exitosamente");
        assertThat(result.data()).isNotNull();
        assertThat(result.data()).isEqualTo(dto);

        ArgumentCaptor<SolicitudReservaRecurso> captor = ArgumentCaptor.forClass(SolicitudReservaRecurso.class);
        verify(solicitudReservaRecursoRepository).save(captor.capture());
        SolicitudReservaRecurso arg = captor.getValue();

        assertThat(arg.getFechaSolicitud()).isEqualTo(dto.getFechaSolicitud());
        assertThat(arg.getHoraInicio()).isEqualTo(dto.getHoraInicio());
        assertThat(arg.getHoraFin()).isEqualTo(dto.getHoraFin());
        assertThat(arg.getNumeroInvitados()).isEqualTo(dto.getNumeroInvitados());

        verify(solicitudReservaRecursoRepository).findById(id);
        verify(modelMapper).map(saved, SolicitudReservaRecursoDTO.class);
    }

    @Test
    void update_shouldThrowNotFound_whenSolicitudMissing() {

        Long id = 99L;
        when(solicitudReservaRecursoRepository.findById(id)).thenReturn(Optional.empty());

        SolicitudReservaRecursoDTO dto = new SolicitudReservaRecursoDTO();

        assertThatThrownBy(() -> solicitudReservaRecursoService.update(id, dto))
                .isInstanceOf(ApiException.class)
                .hasMessage("No se ha encontrado la solicitud")
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });

        verify(solicitudReservaRecursoRepository).findById(id);
        verifyNoMoreInteractions(solicitudReservaRecursoRepository);
    }

    @Test
    void update_shouldThrowBadRequest_whenRecursoDisabled() {

        Long id = 2L;
        SolicitudReservaRecurso existing = new SolicitudReservaRecurso();
        existing.setId(id);

        when(solicitudReservaRecursoRepository.findById(id)).thenReturn(Optional.of(existing));

        SolicitudReservaRecursoDTO dto = new SolicitudReservaRecursoDTO();
        RecursoComun recurso = new RecursoComun();
        recurso.setDisponibilidadRecurso(DisponibilidadRecurso.NO_DISPONIBLE);
        dto.setRecursoComun(recurso);
        dto.setFechaSolicitud(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> solicitudReservaRecursoService.update(id, dto))
                .isInstanceOf(ApiException.class)
                .hasMessage("No se puede modificar una reserva de un recurso deshabilitado.")
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        verify(solicitudReservaRecursoRepository).findById(id);
        verify(solicitudReservaRecursoRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowBadRequest_whenFechaAntesDeHoy() {

        Long id = 3L;
        SolicitudReservaRecurso existing = new SolicitudReservaRecurso();
        existing.setId(id);

        when(solicitudReservaRecursoRepository.findById(id)).thenReturn(Optional.of(existing));

        SolicitudReservaRecursoDTO dto = new SolicitudReservaRecursoDTO();
        RecursoComun recurso = new RecursoComun();
        recurso.setDisponibilidadRecurso(DisponibilidadRecurso.DISPONIBLE); // enabled
        dto.setRecursoComun(recurso);

        dto.setFechaSolicitud(LocalDate.now().minusDays(1)); // fecha anterior a hoy -> invalid
        dto.setHoraInicio(LocalTime.of(10, 0));
        dto.setHoraFin(LocalTime.of(11, 0));
        dto.setNumeroInvitados(2);

        assertThatThrownBy(() -> solicitudReservaRecursoService.update(id, dto))
                .isInstanceOf(ApiException.class)
                .hasMessage("Por favor, ingresa una fecha y hora validas")
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        verify(solicitudReservaRecursoRepository).findById(id);
        verify(solicitudReservaRecursoRepository, never()).save(any());
    }

    @Test
    void crearSolicitud_sinConflicto_guardaExitosamente() {
        RecursoComun recurso;
        Persona persona;
        SolicitudRecursoPropiDTO solicitudDTO;

        recurso = new RecursoComun();
        recurso.setId(1L);
        recurso.setDisponibilidadRecurso(DisponibilidadRecurso.DISPONIBLE);

        persona = new Persona();
        persona.setId(2L);
        persona.setCasa(new Casa(1L, 101));

        solicitudDTO = SolicitudRecursoPropiDTO.builder()
                .idRecurso(1L)
                .idSolicitante(2L)
                .fechaSolicitud(LocalDate.of(2025, 10, 28))
                .horaInicio(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(15, 0))
                .numeroInvitados(3)
                .build();

        when(recursoComunRepository.findById(1L)).thenReturn(Optional.of(recurso));
        when(personaRepository.findById(2L)).thenReturn(Optional.of(persona));
        when(solicitudReservaRecursoRepository.findByRecursoComunAndFechaSolicitud(recurso, solicitudDTO.getFechaSolicitud()))
                .thenReturn(Collections.emptyList());

        SuccessResult<SolicitudRecursoPropiDTO> resultado = solicitudReservaRecursoService.crearSolicitud(solicitudDTO);

        assertNotNull(resultado);
        assertEquals("Reserva creada exitosamente, Pendiente de aprobación por el administrador.", resultado.message());
        verify(solicitudReservaRecursoRepository, times(1)).save(any(SolicitudReservaRecurso.class));
    }

    @Test
    void crearSolicitud_conConflicto_lanzaExcepcion() {

        RecursoComun recurso;
        Persona persona;
        SolicitudRecursoPropiDTO solicitudDTO;

        recurso = new RecursoComun();
        recurso.setId(1L);
        recurso.setDisponibilidadRecurso(DisponibilidadRecurso.DISPONIBLE);

        persona = new Persona();
        persona.setId(2L);
        persona.setCasa(new Casa(1L, 101));

        solicitudDTO = SolicitudRecursoPropiDTO.builder()
                .idRecurso(1L)
                .idSolicitante(2L)
                .fechaSolicitud(LocalDate.of(2025, 10, 28))
                .horaInicio(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(15, 0))
                .numeroInvitados(3)
                .build();

        when(recursoComunRepository.findById(1L)).thenReturn(Optional.of(recurso));
        when(personaRepository.findById(2L)).thenReturn(Optional.of(persona));

        SolicitudReservaRecurso reservaExistente = SolicitudReservaRecurso.builder()
                .horaInicio(LocalTime.of(14, 30))
                .horaFin(LocalTime.of(15, 30))
                .build();

        when(solicitudReservaRecursoRepository.findByRecursoComunAndFechaSolicitud(recurso, solicitudDTO.getFechaSolicitud()))
                .thenReturn(List.of(reservaExistente));

        ApiException ex = assertThrows(ApiException.class, () -> solicitudReservaRecursoService.crearSolicitud(solicitudDTO));

        assertEquals("El recurso ya tiene una solicitud en el horario solicitado.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(solicitudReservaRecursoRepository, never()).save(any());
    }

    @Test
    void modificarCantidadInvitados_conSolicitudExistente_actualizaExitosamente() {

        RecursoComun recurso = new RecursoComun();
        recurso.setId(10L);
        recurso.setNombre("Piscina");

        SolicitudReservaRecurso solicitudReservaRecurso = SolicitudReservaRecurso.builder()
                .id(1L)
                .recursoComun(recurso)
                .fechaSolicitud(LocalDate.of(2025, 10, 28))
                .horaInicio(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(16, 0))
                .numeroInvitados(3)
                .build();

        InvitadoDTO invitadoDTO = InvitadoDTO.builder()
                .idSolicitud(1L)
                .cantidadInvitados(8)
                .build();

        when(solicitudReservaRecursoRepository.findById(1L))
                .thenReturn(Optional.of(solicitudReservaRecurso));

        SuccessResult<SolicitudRecursoPropiDTO> resultado =
                solicitudReservaRecursoService.modificarCantidadInvitados(invitadoDTO);

        assertNotNull(resultado);
        assertEquals("Cantidad de invitados modificado correctamente.", resultado.message());
        assertEquals(8, resultado.data().getNumeroInvitados());
        assertEquals(10L, resultado.data().getIdRecurso());
        verify(solicitudReservaRecursoRepository, times(1)).save(solicitudReservaRecurso);
    }

    @Test
    void modificarCantidadInvitados_conSolicitudInexistente_lanzaError() {
        RecursoComun recurso = new RecursoComun();
        recurso.setId(10L);
        recurso.setNombre("Piscina");

        InvitadoDTO invitadoDTO = InvitadoDTO.builder()
                .idSolicitud(1L)
                .cantidadInvitados(8)
                .build();

        when(solicitudReservaRecursoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> solicitudReservaRecursoService.modificarCantidadInvitados(invitadoDTO));

        verify(solicitudReservaRecursoRepository, never()).save(any());
    }

    @Test
    void testFindReservasByCasa_Exito() {

        Casa casa = new Casa();
        casa.setId(1L);

        RecursoComun recurso = new RecursoComun();
        recurso.setNombre("Piscina");
        recurso.setDescripcion("Piscina comunal");
        recurso.setTipoRecursoComun(TipoRecursoComun.ZONA);

        SolicitudReservaRecurso reserva = new SolicitudReservaRecurso();
        reserva.setId(10L);
        reserva.setCasa(casa);
        reserva.setHoraInicio(LocalTime.from(LocalDateTime.of(2025, 11, 12, 10, 0)));
        reserva.setHoraFin(LocalTime.from(LocalDateTime.of(2025, 11, 12, 12, 0)));
        reserva.setNumeroInvitados(5);
        reserva.setFechaSolicitud(LocalDate.from(LocalDateTime.of(2025, 11, 10, 15, 0)));
        reserva.setFechaCreacion(LocalDate.from(LocalDateTime.of(2025, 11, 10, 15, 5)));
        reserva.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        reserva.setRecursoComun(recurso);

        // Given
        when(casaRepository.findById(1L)).thenReturn(Optional.of(casa));
        when(solicitudReservaRecursoRepository.findAllByCasa(casa))
                .thenReturn(Collections.singletonList(reserva));

        // When
        SuccessResult<List<SolicitudReservaDTO>> result = solicitudReservaRecursoService.findReservasByCasa(1L);

        // Then
        assertNotNull(result);
        assertEquals("Solicitudes obtenidas correctamente", result.message());
        assertEquals(1, result.data().size());

        SolicitudReservaDTO dto = result.data().getFirst();
        assertEquals(reserva.getId(), dto.getId());
        assertEquals("Piscina", dto.getNombre());
        assertEquals(TipoRecursoComun.ZONA, dto.getTipoRecursoComun());

        verify(casaRepository, times(1)).findById(1L);
        verify(solicitudReservaRecursoRepository, times(1)).findAllByCasa(casa);
    }

    @Test
    void testFindReservasByCasa_SinResultados() {

        Casa casa = new Casa();
        casa.setId(1L);

        // Given
        when(casaRepository.findById(1L)).thenReturn(Optional.of(casa));
        when(solicitudReservaRecursoRepository.findAllByCasa(casa))
                .thenReturn(Collections.emptyList());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> {
            solicitudReservaRecursoService.findReservasByCasa(1L);
        });

        assertEquals("No se encontró ninguna reserva.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testDeleteSolicitud_Exito() {
        when(solicitudReservaRecursoRepository.existsById(1L)).thenReturn(true);

        SuccessResult<Void> result = solicitudReservaRecursoService.deleteSolicitud(1L);

        verify(solicitudReservaRecursoRepository, times(1)).deleteById(1L);
        assertEquals("Solicitud eliminada correctamente", result.message());
    }

    @Test
    void testDeleteSolicitud_NoExiste() {
        when(solicitudReservaRecursoRepository.existsById(1L)).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () -> {
            solicitudReservaRecursoService.deleteSolicitud(1L);
        });

        assertEquals("No se encontró la solicitud con el ID especificado.", exception.getMessage());
    }

}

