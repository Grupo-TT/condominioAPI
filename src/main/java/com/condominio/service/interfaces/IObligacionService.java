package com.condominio.service.interfaces;

import com.condominio.dto.response.EstadoCuentaDTO;
import com.condominio.dto.response.SuccessResult;
import org.springframework.http.ResponseEntity;

public interface IObligacionService {

    SuccessResult<EstadoCuentaDTO> estadoDeCuentaCasa(Long idCasa);
    ResponseEntity<?> generarPazYSalvo(Long idCasa);

}