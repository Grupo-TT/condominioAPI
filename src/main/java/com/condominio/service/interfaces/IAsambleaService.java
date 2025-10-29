package com.condominio.service.interfaces;

import com.condominio.dto.request.AsambleaDTO;
import com.condominio.dto.response.SuccessResult;

public interface IAsambleaService {

    SuccessResult<AsambleaDTO> create(AsambleaDTO asamblea);
}
