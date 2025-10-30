package com.condominio.service.interfaces;

import com.condominio.dto.response.InvitadoDTO;
import com.condominio.dto.response.SolicitudRecursoPropiDTO;
import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.EstadoSolicitud;

import java.util.List;

public interface ISolicitudReservaRecursoService {

    SuccessResult<List<SolicitudReservaRecursoDTO>> findByEstado(EstadoSolicitud estado);
    SuccessResult<SolicitudReservaRecursoDTO> aprobar(Long id);
    SuccessResult<SolicitudReservaRecursoDTO> rechazar(Long id);
    SuccessResult<SolicitudReservaRecursoDTO> eliminar(Long id);
    SuccessResult<SolicitudReservaRecursoDTO> update(Long id, SolicitudReservaRecursoDTO solicitud);
    SuccessResult<SolicitudRecursoPropiDTO> crearSolicitud(SolicitudRecursoPropiDTO solicitud);
    SuccessResult<SolicitudRecursoPropiDTO> modificarCantidadInvitados(InvitadoDTO invitadoDTO);

}
