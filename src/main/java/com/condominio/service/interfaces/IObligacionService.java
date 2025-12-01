package com.condominio.service.interfaces;

import com.condominio.dto.request.MultaActualizacionDTO;
import com.condominio.dto.request.MultaRegistroDTO;
import com.condominio.dto.response.EstadoCuentaDTO;
import com.condominio.dto.response.MostrarObligacionDTO;
import com.condominio.dto.response.MultasPorCasaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Obligacion;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IObligacionService {

    SuccessResult<EstadoCuentaDTO> estadoDeCuentaCasa(Long idCasa);
    SuccessResult<Obligacion> save(MultaRegistroDTO multa);
    SuccessResult<Obligacion> update(Long id, MultaActualizacionDTO multa);
    ResponseEntity<?> generarPazYSalvo(Long idCasa);
    SuccessResult<List<MultasPorCasaDTO>> obtenerCasasConMultas();
    void generarObligacionesMensuales();
}