package com.condominio.service.interfaces;

import com.condominio.dto.request.SolicitudReparacionUpdateDTO;
import com.condominio.dto.response.SolicitudReparacionLocativaDTO;
import com.condominio.dto.response.SolicitudReparacionPropiDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.EstadoSolicitud;

import java.util.List;

public interface ISolicitudReparacionLocativaService {
    SuccessResult<List<SolicitudReparacionLocativaDTO>> findByEstado(EstadoSolicitud estado);
    SuccessResult<SolicitudReparacionUpdateDTO> update(Long id, SolicitudReparacionUpdateDTO solicitud);
    SuccessResult<SolicitudReparacionLocativaDTO> aprobar(Long id);
    SuccessResult<SolicitudReparacionLocativaDTO> rechazar(Long id, String comentarios);
    SuccessResult<SolicitudReparacionLocativaDTO> eliminar(Long id);
    SuccessResult<SolicitudReparacionPropiDTO> crearSolicitud(SolicitudReparacionPropiDTO solicitud);
    SuccessResult<SolicitudReparacionPropiDTO> modificarSolicitud(Long id, SolicitudReparacionPropiDTO solicitud);

}
