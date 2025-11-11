package com.condominio.service.implementation;

import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.ReparacionLocativaRepository;
import com.condominio.util.helper.PersonaHelper;
import com.condominio.dto.response.SolicitudReparacionLocativaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.repository.SolicitudReparacionLocativaRepository;
import com.condominio.service.interfaces.ISolicitudReparacionLocativaService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudReparacionLocativaService implements ISolicitudReparacionLocativaService {

    private static final String SOLICITUD_NOT_FOUND = "No se ha encontrado la solicitud";
    private final SolicitudReparacionLocativaRepository solicitudReparacionLocativaRepository;
    private final ModelMapper modelMapper;
    private final PersonaHelper personaHelper;
    private final ReparacionLocativaRepository reparacionLocativaRepository;

    @Override
    public SuccessResult<List<SolicitudReparacionLocativaDTO>> findByEstado(EstadoSolicitud estado) {
        List<SolicitudReparacionLocativa> solicitudes =
                solicitudReparacionLocativaRepository.findByEstadoSolicitud(estado);

        if (solicitudes.isEmpty()) {
            throw new ApiException("No hay solicitudes con estado: " + estado, HttpStatus.NOT_FOUND);
        }

        List<SolicitudReparacionLocativaDTO> dtos = solicitudes.stream().map(solicitud -> {
            SolicitudReparacionLocativaDTO dto = modelMapper.map(solicitud, SolicitudReparacionLocativaDTO.class);

            Long casaId = solicitud.getCasa().getId();
            Persona solicitante = personaHelper.obtenerSolicitantePorCasa(casaId);
            dto.setSolicitante(personaHelper.toPersonaSimpleDTO(solicitante));

            return dto;
        }).toList();

        return new SuccessResult<>("Solicitudes " + estado.name().toLowerCase() + " obtenidas correctamente", dtos);
    }

    @Override
    public SuccessResult<SolicitudReparacionLocativaDTO> update(Long id, SolicitudReparacionLocativaDTO solicitud) {
        SolicitudReparacionLocativa oldSolicitud = solicitudReparacionLocativaRepository.findById(id)
                .orElseThrow(() -> new ApiException(SOLICITUD_NOT_FOUND, HttpStatus.NOT_FOUND));

        if(solicitud.getFechaRealizacion().isBefore(LocalDate.now())) {
            throw new ApiException("Por favor, ingresa una fecha y hora validas", HttpStatus.BAD_REQUEST);
        }

        oldSolicitud.setFechaRealizacion(solicitud.getFechaRealizacion());
        oldSolicitud.setMotivo(solicitud.getMotivo());
        oldSolicitud.setResponsable(solicitud.getResponsable());
        oldSolicitud.setEstadoSolicitud(solicitud.getEstadoSolicitud());

        SolicitudReparacionLocativa actualizada = solicitudReparacionLocativaRepository.save(oldSolicitud);

        return new SuccessResult<>("Solicitud de Reparacion modificada exitosamente", modelMapper.map(actualizada, SolicitudReparacionLocativaDTO.class));
    }

    @Override
    public SuccessResult<SolicitudReparacionLocativaDTO> aprobar(Long id) {
        SolicitudReparacionLocativa solicitud = solicitudReparacionLocativaRepository.findById(id)
                .orElseThrow(() -> new ApiException(SOLICITUD_NOT_FOUND, HttpStatus.NOT_FOUND));

        Long casaId = solicitud.getCasa().getId();
        Persona solicitante = personaHelper.obtenerSolicitantePorCasa(casaId);

        solicitud.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        SolicitudReparacionLocativaDTO soliDTO = saveNewSoliAndReparacion(solicitud, solicitante);
        return new SuccessResult<>("Solicitud de Reparacion aprobada satisfactoriamente", soliDTO);
    }

    @Override
    public SuccessResult<SolicitudReparacionLocativaDTO> rechazar(Long id, String comentarios) {
        SolicitudReparacionLocativa solicitud = solicitudReparacionLocativaRepository.findById(id)
                .orElseThrow(() -> new ApiException(SOLICITUD_NOT_FOUND, HttpStatus.NOT_FOUND));

        Long casaId = solicitud.getCasa().getId();
        Persona solicitante = personaHelper.obtenerSolicitantePorCasa(casaId);

        solicitud.setEstadoSolicitud(EstadoSolicitud.RECHAZADA);
        SolicitudReparacionLocativaDTO soliDTO = saveNewSoliAndReparacion(solicitud, solicitante);
        soliDTO.setComentarios(comentarios);
        return new SuccessResult<>("Solicitud de Reparacion desaprobada", soliDTO);
    }

    public SolicitudReparacionLocativaDTO saveNewSoliAndReparacion(SolicitudReparacionLocativa solicitud, Persona solicitante) {
        SolicitudReparacionLocativa nuevaSoli = solicitudReparacionLocativaRepository.save(solicitud);
        SolicitudReparacionLocativaDTO soliDTO = modelMapper.map(nuevaSoli, SolicitudReparacionLocativaDTO.class);
        soliDTO.setSolicitante(personaHelper.toPersonaSimpleDTO(solicitante));
        ReparacionLocativa reparacionLocativa = ReparacionLocativa.builder()
                .estado(true)
                .solicitudReparacionLocativa(nuevaSoli)
                .build();
        reparacionLocativaRepository.save(reparacionLocativa);

        return soliDTO;
    }
}
