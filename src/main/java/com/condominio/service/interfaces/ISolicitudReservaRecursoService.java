package com.condominio.service.interfaces;

import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import com.condominio.dto.response.SuccessResult;

import java.util.List;

public interface ISolicitudReservaRecursoService {

    SuccessResult<List<SolicitudReservaRecursoDTO>> findPendientes();
}
