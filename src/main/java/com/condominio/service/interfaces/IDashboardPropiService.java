package com.condominio.service.interfaces;

import com.condominio.dto.response.AccountStatusDTO;
import com.condominio.dto.response.InfoCasaPropiDTO;
import com.condominio.dto.response.SolicitudPropiDTO;
import com.condominio.dto.response.SuccessResult;

import java.util.List;

public interface IDashboardPropiService {
    SuccessResult<InfoCasaPropiDTO> getPropiBasicInfo();
    SuccessResult<AccountStatusDTO> getAccountStatus();
    SuccessResult<List<SolicitudPropiDTO>> getSolicitudesPropietario();
}
