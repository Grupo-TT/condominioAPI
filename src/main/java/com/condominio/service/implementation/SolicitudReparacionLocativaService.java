package com.condominio.service.implementation;

import com.condominio.dto.request.SolicitudReparacionUpdateDTO;
import com.condominio.dto.response.SolicitudReparacionPropiDTO;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.PersonaRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final PersonaRepository personaRepository;

    @Override
    public SuccessResult<List<SolicitudReparacionLocativaDTO>> findByEstado(EstadoSolicitud estado) {
        List<SolicitudReparacionLocativa> solicitudes =
                solicitudReparacionLocativaRepository.findByEstadoSolicitud(estado);

        if (solicitudes.isEmpty()) {
            throw new ApiException("No hay solicitudes con estado: " + estado, HttpStatus.OK);
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
    public SuccessResult<SolicitudReparacionUpdateDTO> update(Long id, SolicitudReparacionUpdateDTO solicitud) {
        SolicitudReparacionLocativa oldSolicitud = solicitudReparacionLocativaRepository.findById(id)
                .orElseThrow(() -> new ApiException(SOLICITUD_NOT_FOUND, HttpStatus.NOT_FOUND));

        if(solicitud.getFechaRealizacion().isAfter(LocalDate.now())) {
            throw new ApiException("Por favor, ingresa una fecha y hora validas", HttpStatus.BAD_REQUEST);
        }

        if(solicitud.getInicioObra().isBefore(LocalDate.now())) {
            throw new ApiException("La fecha de inicio de la obra debe ser posterior a la fecha actual", HttpStatus.BAD_REQUEST);
        }

        if(solicitud.getFinObra().isBefore(solicitud.getInicioObra())) {
            throw new ApiException("La fecha de fin de la obra debe ser posterior a la fecha de inicio", HttpStatus.BAD_REQUEST);
        }

        oldSolicitud.setFechaRealizacion(LocalDate.now());
        oldSolicitud.setMotivo(solicitud.getMotivo());
        oldSolicitud.setResponsable(solicitud.getResponsable());
        oldSolicitud.setInicioObra(solicitud.getInicioObra());
        oldSolicitud.setFinObra(solicitud.getFinObra());
        oldSolicitud.setTipoObra(solicitud.getTipoObra());
        oldSolicitud.setTipoObraDetalle(solicitud.getTipoObraDetalle());
        oldSolicitud.setEstadoSolicitud(solicitud.getEstadoSolicitud());

        SolicitudReparacionLocativa actualizada = solicitudReparacionLocativaRepository.save(oldSolicitud);
        SolicitudReparacionUpdateDTO dto = modelMapper.map(actualizada, SolicitudReparacionUpdateDTO.class);

        return new SuccessResult<>("Solicitud de Reparacion modificada exitosamente", dto);
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
        solicitud.setComentarios(comentarios);
        soliDTO.setComentarios(comentarios);
        return new SuccessResult<>("Solicitud de Reparacion desaprobada satisfactoriamente", soliDTO);
    }

    @Override
    public SuccessResult<SolicitudReparacionLocativaDTO> eliminar(Long id) {

        SolicitudReparacionLocativa solicitud = verificarUsuarioAndSoli(id);
        Long casaId = solicitud.getCasa().getId();
        Persona solicitante = personaHelper.obtenerSolicitantePorCasa(casaId);
        SolicitudReparacionLocativaDTO dto = modelMapper.map(solicitud, SolicitudReparacionLocativaDTO.class);
        dto.setSolicitante(personaHelper.toPersonaSimpleDTO(solicitante));

        if(solicitud.getEstadoSolicitud() != EstadoSolicitud.PENDIENTE) {
            throw new ApiException("No se puede eliminar una solicitud que ya ha sido aprobada o rechazada", HttpStatus.BAD_REQUEST);
        }

        solicitudReparacionLocativaRepository.delete(solicitud);
        return new SuccessResult<>("Solicitud eliminada exitosamente", dto);
    }

    @Override
    public SuccessResult<SolicitudReparacionPropiDTO> crearSolicitud(SolicitudReparacionPropiDTO soliDTO) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Persona solicitante = personaRepository.findByUserEmail(username)
                .orElseThrow(() -> new ApiException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        if(soliDTO.getInicioObra().isBefore(LocalDate.now())) {
            throw new ApiException("La fecha de inicio de la obra debe ser posterior a la fecha actual", HttpStatus.BAD_REQUEST);
        }

        if(soliDTO.getFinObra().isBefore(soliDTO.getInicioObra())) {
            throw new ApiException("La fecha de fin de la obra debe ser posterior a la fecha de inicio", HttpStatus.BAD_REQUEST);
        }

        SolicitudReparacionLocativa soliReparacion = SolicitudReparacionLocativa.builder()
                .motivo(soliDTO.getMotivo())
                .responsable(soliDTO.getResponsable())
                .estadoSolicitud(EstadoSolicitud.PENDIENTE)
                .fechaRealizacion(LocalDate.now())
                .inicioObra(soliDTO.getInicioObra())
                .finObra(soliDTO.getFinObra())
                .tipoObra(soliDTO.getTipoObra())
                .tipoObraDetalle(soliDTO.getTipoObraDetalle())
                .casa(solicitante.getCasa())
                .build();
        solicitudReparacionLocativaRepository.save(soliReparacion);

        return new SuccessResult<>("Solicitud registrada exitosamente, Pendiente de aprobación por el administrador", soliDTO);
    }

    @Override
    public SuccessResult<SolicitudReparacionPropiDTO> modificarSolicitud(Long id, SolicitudReparacionPropiDTO soliDTO) {

        SolicitudReparacionLocativa oldSolicitud = verificarUsuarioAndSoli(id);

        if(oldSolicitud.getEstadoSolicitud() != EstadoSolicitud.PENDIENTE) {
            throw new ApiException("No se puede modificar una solicitud que ya ha sido aprobada o rechazada", HttpStatus.BAD_REQUEST);
        }

        if(soliDTO.getInicioObra().isBefore(LocalDate.now())) {
            throw new ApiException("La fecha de inicio de la obra debe ser posterior a la fecha actual", HttpStatus.BAD_REQUEST);
        }

        if(soliDTO.getFinObra().isBefore(soliDTO.getInicioObra())) {
            throw new ApiException("La fecha de fin de la obra debe ser posterior a la fecha de inicio", HttpStatus.BAD_REQUEST);
        }

        oldSolicitud.setMotivo(soliDTO.getMotivo());
        oldSolicitud.setResponsable(soliDTO.getResponsable());
        oldSolicitud.setFechaRealizacion(LocalDate.now());
        oldSolicitud.setInicioObra(soliDTO.getInicioObra());
        oldSolicitud.setFinObra(soliDTO.getFinObra());
        oldSolicitud.setTipoObra(soliDTO.getTipoObra());
        oldSolicitud.setTipoObraDetalle(soliDTO.getTipoObraDetalle());
        solicitudReparacionLocativaRepository.save(oldSolicitud);

        return new SuccessResult<>("Solicitud modificada exitosamente", soliDTO);
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

    public SolicitudReparacionLocativa verificarUsuarioAndSoli(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Persona persona = personaRepository.findByUserEmail(username)
                .orElseThrow(() -> new ApiException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        SolicitudReparacionLocativa solicitud = solicitudReparacionLocativaRepository.findById(id)
                .orElseThrow(() -> new ApiException(SOLICITUD_NOT_FOUND, HttpStatus.NOT_FOUND));

        Long casaSolicitudId = solicitud.getCasa() != null ? solicitud.getCasa().getId() : null;
        Long casaUsuarioId = persona.getCasa() != null ? persona.getCasa().getId() : null;

        if (casaUsuarioId == null || !casaUsuarioId.equals(casaSolicitudId)) {
            throw new ApiException("No autorizado para modificar esta solicitud", HttpStatus.FORBIDDEN);
        }
        return solicitud;
    }
}
