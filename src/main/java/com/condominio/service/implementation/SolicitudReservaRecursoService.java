package com.condominio.service.implementation;

import com.condominio.dto.response.*;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.*;
import com.condominio.service.interfaces.ISolicitudReservaRecursoService;
import com.condominio.util.events.RepliedSolicitudEvent;
import com.condominio.util.exception.ApiException;
import com.condominio.util.helper.PersonaHelper;
import lombok.RequiredArgsConstructor;
import org.aspectj.bridge.Message;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SolicitudReservaRecursoService implements ISolicitudReservaRecursoService {

    private static final String SOLICITUD_NOT_FOUND = "No se ha encontrado la solicitud";
    private final SolicitudReservaRecursoRepository solicitudReservaRecursoRepository;
    private final ModelMapper modelMapper;
    private final PersonaRepository personaRepository;
    private final RecursoComunRepository recursoComunRepository;
    private final ReservaRepository reservaRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PersonaHelper personaHelper;
    private final CasaRepository casaRepository;


    public  SuccessResult<List<SolicitudReservaRecursoDTO>> findByEstado(EstadoSolicitud estado){

        List<SolicitudReservaRecurso> solicitudes =
                solicitudReservaRecursoRepository.findByEstadoSolicitud(estado);

        if (solicitudes.isEmpty()) {
            throw new ApiException("No hay solicitudes con estado: " + estado, HttpStatus.OK);
        }

        List<SolicitudReservaRecursoDTO> dtos = solicitudes.stream().map(solicitud -> {
            SolicitudReservaRecursoDTO dto = modelMapper.map(solicitud, SolicitudReservaRecursoDTO.class);

            Long casaId = solicitud.getCasa().getId();
            Persona solicitante = personaRepository.findArrendatarioByCasaId(casaId)
                    .orElseGet(() -> personaRepository.findPropietarioByCasaId(casaId)
                            .orElseThrow(() -> new ApiException(
                                    "No se encontro un solicitante (arrendatario o propietario) para la casa con ID " + casaId,
                                    HttpStatus.BAD_REQUEST
                            )));

            dto.setSolicitante(PersonaSimpleDTO.builder()
                    .nombreCompleto(solicitante.getNombreCompleto())
                    .telefono(solicitante.getTelefono())
                    .correo(solicitante.getUser().getEmail())
                    .build());

            return dto;
        }).toList();

        return new SuccessResult<>("Solicitudes " + estado.name().toLowerCase() + " obtenidas correctamente", dtos);
    }

    @Override
    public SuccessResult<SolicitudReservaRecursoDTO> aprobar(Long id) {
        SolicitudReservaRecurso solicitud = validarSolicitudPendiente(id);
        Long casaId = solicitud.getCasa().getId();
        Persona solicitante = personaHelper.obtenerSolicitantePorCasa(casaId);

        solicitud.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        SolicitudReservaRecurso aprobada = solicitudReservaRecursoRepository.save(solicitud);
        SolicitudReservaRecursoDTO soliDTO = modelMapper.map(aprobada, SolicitudReservaRecursoDTO.class);
        soliDTO.setSolicitante(personaHelper.toPersonaSimpleDTO(solicitante));
        Reserva reserva = Reserva.builder()
                .estado(true)
                .solicitudReservaRecurso(aprobada)
                .build();
        reservaRepository.save(reserva);

        eventPublisher.publishEvent(new RepliedSolicitudEvent(solicitante.getUser().getEmail(),soliDTO));
        return new SuccessResult<>("Reserva aprobada correctamente", soliDTO);
    }

    @Override
    public SuccessResult<SolicitudReservaRecursoDTO> rechazar(Long id) {
        SolicitudReservaRecurso solicitud = validarSolicitudPendiente(id);
        Long casaId = solicitud.getCasa().getId();
        Persona solicitante = personaHelper.obtenerSolicitantePorCasa(casaId);

        solicitud.setEstadoSolicitud(EstadoSolicitud.RECHAZADA);
        SolicitudReservaRecurso rechazada = solicitudReservaRecursoRepository.save(solicitud);
        SolicitudReservaRecursoDTO soliDTO = modelMapper.map(rechazada, SolicitudReservaRecursoDTO.class);
        soliDTO.setSolicitante(personaHelper.toPersonaSimpleDTO(solicitante));

        eventPublisher.publishEvent(new RepliedSolicitudEvent(solicitante.getUser().getEmail(),soliDTO));
        return new SuccessResult<>("Reserva rechazada correctamente", soliDTO);
    }

    @Override
    public SuccessResult<SolicitudReservaRecursoDTO> cancelar(Long id) {
        SolicitudReservaRecurso solicitud = solicitudReservaRecursoRepository.findById(id)
                .orElseThrow(() -> new ApiException(SOLICITUD_NOT_FOUND, HttpStatus.NOT_FOUND));

        if(solicitud.getEstadoSolicitud() != EstadoSolicitud.APROBADA) {
            throw new ApiException("Solo se pueden cancelar reservas aprobadas", HttpStatus.BAD_REQUEST);
        }

        if(!solicitud.getFechaSolicitud().isBefore(LocalDate.now().minusDays(1))) {
            throw new ApiException("Solo se permiten cancelar reservas posteriores a la fecha de ayer", HttpStatus.BAD_REQUEST);
        }

        solicitudReservaRecursoRepository.delete(solicitud);
        return new SuccessResult<>("Reserva cancelada exitosamente", modelMapper.map(solicitud, SolicitudReservaRecursoDTO.class));
    }

    @Override
    public SuccessResult<SolicitudReservaRecursoDTO> update(Long id, SolicitudReservaRecursoDTO solicitud) {
        SolicitudReservaRecurso oldSolicitud = solicitudReservaRecursoRepository.findById(id)
                .orElseThrow(() -> new ApiException(SOLICITUD_NOT_FOUND, HttpStatus.NOT_FOUND));

        if(solicitud.getRecursoComun().getDisponibilidadRecurso()== DisponibilidadRecurso.NO_DISPONIBLE) {
            throw new ApiException("No se puede modificar una reserva de un recurso deshabilitado.", HttpStatus.BAD_REQUEST);
        }

        if(solicitud.getFechaSolicitud().isBefore(LocalDate.now())) {
            throw new ApiException("Por favor, ingresa una fecha y hora validas", HttpStatus.BAD_REQUEST);
        }

        oldSolicitud.setFechaSolicitud(solicitud.getFechaSolicitud());
        oldSolicitud.setHoraInicio(solicitud.getHoraInicio());
        oldSolicitud.setHoraFin(solicitud.getHoraFin());
        oldSolicitud.setNumeroInvitados(solicitud.getNumeroInvitados());

        SolicitudReservaRecurso actualizada = solicitudReservaRecursoRepository.save(oldSolicitud);

        return new SuccessResult<>("Reserva modificada exitosamente", modelMapper.map(actualizada, SolicitudReservaRecursoDTO.class));
    }

    @Override
    public SuccessResult<SolicitudRecursoPropiDTO> crearSolicitud(SolicitudRecursoPropiDTO solicitudDTO) {

        Optional<RecursoComun> optionalRecursoComun = recursoComunRepository.findById(solicitudDTO.getIdRecurso());
        Optional<Persona> optionalPersona = personaRepository.findById(solicitudDTO.getIdSolicitante());
        RecursoComun recursoComun = null;
        Persona persona;
        if(optionalRecursoComun.isPresent()) {
            recursoComun = optionalRecursoComun.get();
            if(recursoComun.getDisponibilidadRecurso() != DisponibilidadRecurso.DISPONIBLE) {
                throw new ApiException("Recurso no disponible.", HttpStatus.BAD_REQUEST);
            }
        }
        if(optionalPersona.isPresent()) {
            persona = optionalPersona.get();
        }else {
            throw new ApiException("Solicitante no encontrado.", HttpStatus.BAD_REQUEST);
        }

        boolean hayConflicto = validarFechaSolicitud(recursoComun, solicitudDTO.getFechaSolicitud(),solicitudDTO.getHoraInicio(), solicitudDTO.getHoraFin());

        if (hayConflicto) {
            throw new ApiException("El recurso ya tiene una solicitud en el horario solicitado.", HttpStatus.BAD_REQUEST);
        }
        SolicitudReservaRecurso reservaRecurso = SolicitudReservaRecurso.builder()
                .fechaSolicitud(LocalDate.now())
                .recursoComun(recursoComun)
                .casa(persona.getCasa())
                .horaInicio(solicitudDTO.getHoraInicio())
                .horaFin(solicitudDTO.getHoraFin())
                .estadoSolicitud(EstadoSolicitud.PENDIENTE)
                .numeroInvitados(solicitudDTO.getNumeroInvitados())
                .fechaCreacion(LocalDate.now())
                .build();
        solicitudReservaRecursoRepository.save(reservaRecurso);

        return new SuccessResult<>("Reserva creada exitosamente, Pendiente de aprobación por el administrador.", solicitudDTO);
    }

    @Override
    public SuccessResult<SolicitudRecursoPropiDTO> modificarCantidadInvitados(InvitadoDTO invitadoDTO) {
        if (invitadoDTO.getCantidadInvitados() < 0) {
            throw new ApiException("No puede ingresar una cantidad de invitados negativa.", HttpStatus.BAD_REQUEST);
        }
        Optional<SolicitudReservaRecurso> optionalSolicitudReservaRecurso = solicitudReservaRecursoRepository.findById(invitadoDTO.getIdSolicitud());
        SolicitudReservaRecurso solicitudReservaRecurso = null;
        if(optionalSolicitudReservaRecurso.isPresent()) {
            solicitudReservaRecurso = optionalSolicitudReservaRecurso.get();
            solicitudReservaRecurso.setNumeroInvitados(invitadoDTO.getCantidadInvitados());
            solicitudReservaRecursoRepository.save(solicitudReservaRecurso);
        }else{
            throw new ApiException("No se encontró la solicitud de reserva.", HttpStatus.BAD_REQUEST);
        }
        SolicitudRecursoPropiDTO solicitudDTO = SolicitudRecursoPropiDTO.builder()
                .idRecurso(solicitudReservaRecurso.getRecursoComun().getId())
                .fechaSolicitud(solicitudReservaRecurso.getFechaSolicitud())
                .horaFin(solicitudReservaRecurso.getHoraFin())
                .horaInicio(solicitudReservaRecurso.getHoraInicio())
                .numeroInvitados(invitadoDTO.getCantidadInvitados())
                .build();
        return new SuccessResult<>("Cantidad de invitados modificado correctamente.", solicitudDTO);
    }

    @Override
    public SuccessResult<List<SolicitudReservaDTO>> findReservasByCasa(Long idCasa) {
        Casa casa = casaRepository.findById(idCasa).get();

        List<SolicitudReservaRecurso> reservasCasa = solicitudReservaRecursoRepository.findAllByCasa(casa);
        List<SolicitudReservaDTO> reservasDTO = new ArrayList<>();
        if(reservasCasa.isEmpty()) {
            throw new ApiException("No se encontró ninguna reserva.", HttpStatus.NOT_FOUND);
        }else {
            for (SolicitudReservaRecurso reserva : reservasCasa) {
                SolicitudReservaDTO solicitudReservaDTO = SolicitudReservaDTO.builder()
                        .id(reserva.getId())
                        .horaFin(reserva.getHoraFin())
                        .horaInicio(reserva.getHoraInicio())
                        .numeroInvitados(reserva.getNumeroInvitados())
                        .fechaReserva(reserva.getFechaSolicitud())
                        .fechaCreacion(reserva.getFechaCreacion())
                        .estadoSolicitud(reserva.getEstadoSolicitud())
                        .nombre(reserva.getRecursoComun().getNombre())
                        .descripcion(reserva.getRecursoComun().getDescripcion())
                        .tipoRecursoComun(reserva.getRecursoComun().getTipoRecursoComun())
                        .build();
                reservasDTO.add(solicitudReservaDTO);
            }
        }
        return new SuccessResult<>("Solicitudes obtenidas correctamente", reservasDTO);
    }

    @Override
    public SuccessResult<Void> deleteSolicitud(Long id) {
        if (!solicitudReservaRecursoRepository.existsById(id)) {
            throw new ApiException("No se encontró la solicitud con el ID especificado.", HttpStatus.NOT_FOUND);
        }

        solicitudReservaRecursoRepository.deleteById(id);

        return new SuccessResult<>("Solicitud eliminada correctamente", null);
    }

    private SolicitudReservaRecurso validarSolicitudPendiente(Long id) {
        SolicitudReservaRecurso solicitud = solicitudReservaRecursoRepository.findById(id)
                .orElseThrow(() -> new ApiException(SOLICITUD_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (solicitud.getEstadoSolicitud() != EstadoSolicitud.PENDIENTE) {
            throw new ApiException("Solo se pueden gestionar reservas pendientes", HttpStatus.BAD_REQUEST);
        }

        if(solicitud.getRecursoComun().getDisponibilidadRecurso()== DisponibilidadRecurso.NO_DISPONIBLE){
            throw new ApiException("No se puede aprobar una reserva de un recurso deshabilitado.", HttpStatus.BAD_REQUEST);
        }
        return solicitud;
    }

    private Boolean validarFechaSolicitud(RecursoComun recursoComun, LocalDate fechaSolicitud, LocalTime horaInicio, LocalTime horaFin ) {
        List<SolicitudReservaRecurso> solicitudesReservas = solicitudReservaRecursoRepository.findByRecursoComunAndFechaSolicitud(recursoComun, fechaSolicitud);

        LocalTime nuevaHoraInicio = horaInicio;
        LocalTime nuevaHoraFin = horaFin;

        boolean hayConflicto = solicitudesReservas.stream().anyMatch(reserva -> {
            LocalTime horaInicioExistente = reserva.getHoraInicio();
            LocalTime horaFinExistente = reserva.getHoraFin();

            // Condición de traslape:
            // (inicioNueva < finExistente) && (finNueva > inicioExistente)
            return nuevaHoraInicio.isBefore(horaFinExistente) && nuevaHoraFin.isAfter(horaInicioExistente);
        });
        return hayConflicto;
    }
}
