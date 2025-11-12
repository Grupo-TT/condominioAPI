package com.condominio.service.interfaces;

import com.condominio.dto.response.*;
import com.condominio.persistence.model.EstadoSolicitud;

import java.util.List;

public interface ISolicitudReservaRecursoService {

    SuccessResult<List<SolicitudReservaRecursoDTO>> findByEstado(EstadoSolicitud estado);
    SuccessResult<SolicitudReservaRecursoDTO> aprobar(Long id);
    SuccessResult<SolicitudReservaRecursoDTO> rechazar(Long id);
    SuccessResult<SolicitudReservaRecursoDTO> cancelar(Long id);
    SuccessResult<SolicitudReservaRecursoDTO> update(Long id, SolicitudReservaRecursoDTO solicitud);
    SuccessResult<SolicitudRecursoPropiDTO> crearSolicitud(SolicitudRecursoPropiDTO solicitud);
    SuccessResult<SolicitudRecursoPropiDTO> modificarCantidadInvitados(InvitadoDTO invitadoDTO);
    SuccessResult<List<SolicitudReservaDTO>> findReservasByCasa(Long idCasa);
    SuccessResult<Void> deleteSolicitud(Long id);

}
