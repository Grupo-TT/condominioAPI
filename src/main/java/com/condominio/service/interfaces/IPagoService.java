package com.condominio.service.interfaces;

import com.condominio.dto.response.ObligacionDTO;
import com.condominio.dto.response.PagoDTO;
import com.condominio.dto.response.SuccessResult;

import java.time.LocalDate;
import java.util.Optional;

public interface IPagoService {
    SuccessResult<ObligacionDTO> registrarPago(PagoDTO pagoDTO);
    Optional<LocalDate> obtenerFechaUltimoPagoPorCasa(Long idCasa);
}
