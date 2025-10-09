package com.condominio.service.interfaces;

import com.condominio.dto.response.EstadoCuentaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Obligacion;

import java.util.List;

public interface IObligacionService {

    SuccessResult<EstadoCuentaDTO> estadoDeCuentaCasa(Long idCasa);
    List<Obligacion> findAllObligaciones();
    List<Obligacion> findByCasaId(Long idCasa);

}
