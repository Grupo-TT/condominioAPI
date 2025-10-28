package com.condominio.service.implementation;

import com.condominio.dto.response.PersonaSimpleDTO;
import com.condominio.dto.response.SolicitudRecursoPropiDTO;
import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.RecursoComunRepository;
import com.condominio.persistence.repository.SolicitudReservaRecursoRepository;
import com.condominio.service.interfaces.ISolicitudReservaRecursoService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
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


    public  SuccessResult<List<SolicitudReservaRecursoDTO>> findByEstado(EstadoSolicitud estado){

        List<SolicitudReservaRecurso> solicitudes =
                solicitudReservaRecursoRepository.findByEstadoSolicitud(estado);

        if (solicitudes.isEmpty()) {
            throw new ApiException("No hay solicitudes con estado: " + estado, HttpStatus.NOT_FOUND);
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

        solicitud.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        SolicitudReservaRecurso aprobada = solicitudReservaRecursoRepository.save(solicitud);
        return new SuccessResult<>("Reserva aprobada correctamente", modelMapper.map(aprobada, SolicitudReservaRecursoDTO.class));
    }

    @Override
    public SuccessResult<SolicitudReservaRecursoDTO> rechazar(Long id) {
        SolicitudReservaRecurso solicitud = validarSolicitudPendiente(id);

        solicitud.setEstadoSolicitud(EstadoSolicitud.RECHAZADA);
        SolicitudReservaRecurso rechazada = solicitudReservaRecursoRepository.save(solicitud);
        return new SuccessResult<>("Reserva rechazada correctamente", modelMapper.map(rechazada, SolicitudReservaRecursoDTO.class));
    }

    @Override
    public SuccessResult<SolicitudReservaRecursoDTO> eliminar(Long id) {
        SolicitudReservaRecurso solicitud = solicitudReservaRecursoRepository.findById(id)
                .orElseThrow(() -> new ApiException(SOLICITUD_NOT_FOUND, HttpStatus.NOT_FOUND));

        if(solicitud.getEstadoSolicitud() != EstadoSolicitud.APROBADA) {
            throw new ApiException("Solo se pueden eliminar reservas aprobadas", HttpStatus.BAD_REQUEST);
        }

        if(!solicitud.getFechaSolicitud().isBefore(LocalDate.now().minusDays(1))) {
            throw new ApiException("Solo se permiten borrar reservas posteriores a la fecha de ayer", HttpStatus.BAD_REQUEST);
        }

        solicitudReservaRecursoRepository.delete(solicitud);
        return new SuccessResult<>("Reserva eliminada exitosamente", modelMapper.map(solicitud, SolicitudReservaRecursoDTO.class));
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

        List<SolicitudReservaRecurso> solicitudesReservas = solicitudReservaRecursoRepository.findByRecursoComunAndFechaSolicitud(recursoComun, solicitudDTO.getFechaSolicitud());

        LocalTime nuevaHoraInicio = solicitudDTO.getHoraInicio();
        LocalTime nuevaHoraFin = solicitudDTO.getHoraFin();

        boolean hayConflicto = solicitudesReservas.stream().anyMatch(reserva -> {
            LocalTime horaInicioExistente = reserva.getHoraInicio();
            LocalTime horaFinExistente = reserva.getHoraFin();

            // Condición de traslape:
            // (inicioNueva < finExistente) && (finNueva > inicioExistente)
            return nuevaHoraInicio.isBefore(horaFinExistente) && nuevaHoraFin.isAfter(horaInicioExistente);
        });

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
                .build();
        solicitudReservaRecursoRepository.save(reservaRecurso);

        return new SuccessResult<>("Reserva creada exitosamente, Pendiente de aprobación por el administrador.", solicitudDTO);
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
}
