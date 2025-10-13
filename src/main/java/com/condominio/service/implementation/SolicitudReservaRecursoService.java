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
}
