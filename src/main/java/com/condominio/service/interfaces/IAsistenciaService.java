package com.condominio.service.interfaces;

import com.condominio.dto.request.AsistenciaDTO;
import com.condominio.dto.response.SuccessResult;

import java.util.List;

public interface IAsistenciaService {
    SuccessResult<Void> registrarAsistencia(Long idAsamblea, List<AsistenciaDTO> asistencias );
}
