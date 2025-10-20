package com.condominio.service.implementation;

import com.condominio.dto.response.PersonaSimpleDTO;
import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.SolicitudReservaRecurso;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.SolicitudReservaRecursoRepository;
import com.condominio.service.interfaces.ISolicitudReservaRecursoService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudReservaRecursoService implements ISolicitudReservaRecursoService {

    private final SolicitudReservaRecursoRepository solicitudReservaRecursoRepository;
    private final ModelMapper modelMapper;
    private final PersonaRepository personaRepository;


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
                .orElseThrow(() -> new ApiException("No se ha encontrado la solicitud", HttpStatus.NOT_FOUND));

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
                .orElseThrow(() -> new ApiException("No se ha encontrado la solicitud", HttpStatus.NOT_FOUND));

        if(!solicitud.getRecursoComun().isEstadoRecurso()) {
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

    private SolicitudReservaRecurso validarSolicitudPendiente(Long id) {
        SolicitudReservaRecurso solicitud = solicitudReservaRecursoRepository.findById(id)
                .orElseThrow(() -> new ApiException("No se ha encontrado la solicitud", HttpStatus.NOT_FOUND));

        if (solicitud.getEstadoSolicitud() != EstadoSolicitud.PENDIENTE) {
            throw new ApiException("Solo se pueden gestionar reservas pendientes", HttpStatus.BAD_REQUEST);
        }

        if(!solicitud.getRecursoComun().isEstadoRecurso()){
            throw new ApiException("No se puede aprobar una reserva de un recurso deshabilitado.", HttpStatus.BAD_REQUEST);
        }
        return solicitud;
    }
}
