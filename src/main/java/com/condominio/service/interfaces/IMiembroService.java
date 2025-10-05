package com.condominio.service.interfaces;

import com.condominio.dto.response.MiembrosDTO;
import com.condominio.dto.response.SuccessResult;
import java.util.List;

public interface IMiembroService {

    int countByCasaId(Long IdCasa);
    SuccessResult<List<MiembrosDTO>> obtenerMiembrosPorCasa(Long idCasa);
}
