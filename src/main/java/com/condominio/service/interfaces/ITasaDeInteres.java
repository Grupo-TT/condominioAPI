package com.condominio.service.interfaces;

import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.TasaDeInteres;

public interface ITasaDeInteres {

    SuccessResult<TasaDeInteres> actualizarTasaDeInteres(double nuevoValor);
}
