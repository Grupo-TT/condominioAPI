package com.condominio.service.interfaces;

import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.PagoAdicional;

public interface IPagoAdicional {

    SuccessResult<PagoAdicional> actualizarPagoAdicional(double nuevoValor);
}
