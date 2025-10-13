package com.condominio.service.interfaces;

import com.condominio.dto.response.ObligacionDTO;
import com.condominio.dto.response.PagoDTO;
import com.condominio.dto.response.SuccessResult;

public interface IPagoService {
    SuccessResult<ObligacionDTO> registrarPago(PagoDTO pagoDTO);
}
