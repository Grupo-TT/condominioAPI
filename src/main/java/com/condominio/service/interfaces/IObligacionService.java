package com.condominio.service.interfaces;

import com.condominio.dto.response.EstadoCuentaDTO;
import com.condominio.dto.response.SuccessResult;

public interface IObligacionService {

    SuccessResult<EstadoCuentaDTO> estadoDeCuentaCasa(Long idCasa);

}