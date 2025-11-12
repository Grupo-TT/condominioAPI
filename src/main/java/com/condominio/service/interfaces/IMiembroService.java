package com.condominio.service.interfaces;

import com.condominio.dto.request.MiembroActualizacionDTO;
import com.condominio.dto.request.MiembroRegistroDTO;
import com.condominio.dto.response.MiembrosDTO;
import com.condominio.dto.response.SuccessResult;
import java.util.List;

public interface IMiembroService {

    int countByCasaId(Long idCasa);
    SuccessResult<List<MiembrosDTO>> obtenerMiembrosPorCasa(Long idCasa);
    SuccessResult<Void> crearMiembro(MiembroRegistroDTO miembroRegistroDTO);
    SuccessResult<Void> actualizarEstadoMiembro(Long idMiembro,Long casaId);
    SuccessResult<Void> actualizarMiembro(Long idMiembro, MiembroActualizacionDTO dto, Long casaUsuarioId);

}
