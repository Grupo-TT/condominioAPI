package com.condominio.service.interfaces;

import com.condominio.dto.response.MovimientosMesDTO;
import com.condominio.dto.response.ResumenFinancieroDTO;
import com.condominio.dto.response.SuccessResult;

public interface IDashboardAdminService {

    SuccessResult<ResumenFinancieroDTO> getResumenFinancieronByYear(int year);
    SuccessResult<MovimientosMesDTO> getResumenFinancieronMesReciente();
}
