package com.condominio.service.implementation;

import com.condominio.util.helper.PersonaHelper;
import com.condominio.dto.response.SolicitudReparacionLocativaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.SolicitudReparacionLocativa;
import com.condominio.persistence.repository.SolicitudReparacionLocativaRepository;
import com.condominio.service.interfaces.ISolicitudReparacionLocativaService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudReparacionLocativaService implements ISolicitudReparacionLocativaService {

    private final SolicitudReparacionLocativaRepository solicitudReparacionLocativaRepository;
    private final ModelMapper modelMapper;
    private final PersonaHelper personaHelper;

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
}
