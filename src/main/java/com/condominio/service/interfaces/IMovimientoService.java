package com.condominio.service.interfaces;

import com.condominio.dto.request.MovimientoRequestDTO;
import com.condominio.dto.response.MovimientoDTO;
import com.condominio.dto.response.MovimientosMesDTO;
import com.condominio.dto.response.SuccessResult;

public interface IMovimientoService {

    SuccessResult<MovimientosMesDTO> getMovimientosPorMes(int mes, int anio);
    SuccessResult<MovimientoDTO> crearMovimiento(MovimientoRequestDTO movimientoDTO);
    SuccessResult<MovimientoDTO> actualizarMovimiento(Long id, MovimientoRequestDTO req);
    SuccessResult<Void> eliminarMovimiento(Long id);

}
