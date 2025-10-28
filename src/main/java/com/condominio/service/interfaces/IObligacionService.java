package com.condominio.service.interfaces;

import com.condominio.dto.request.MultaActualizacionDTO;
import com.condominio.dto.request.MultaRegistroDTO;
import com.condominio.dto.response.EstadoCuentaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Obligacion;
import org.springframework.http.ResponseEntity;

public interface IObligacionService {

    SuccessResult<EstadoCuentaDTO> estadoDeCuentaCasa(Long idCasa);
    SuccessResult<Obligacion> save(MultaRegistroDTO multa);
    SuccessResult<Obligacion> update(Long id, MultaActualizacionDTO multa);
    ResponseEntity<?> generarPazYSalvo(Long idCasa);
    void generarObligacionesMensuales();
}