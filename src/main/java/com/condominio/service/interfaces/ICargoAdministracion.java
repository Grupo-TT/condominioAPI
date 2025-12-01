package com.condominio.service.interfaces;

import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.CargoAdministracion;

public interface ICargoAdministracion {
    SuccessResult<CargoAdministracion> actualizarCargoAdministracion(double nuevoValor);
}
