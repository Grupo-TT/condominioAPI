package com.condominio.service.interfaces;

import com.condominio.dto.response.SolicitudReparacionLocativaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.EstadoSolicitud;

import java.util.List;

public interface ISolicitudReparacionLocativaService {
    SuccessResult<List<SolicitudReparacionLocativaDTO>> findByEstado(EstadoSolicitud estado);
}
