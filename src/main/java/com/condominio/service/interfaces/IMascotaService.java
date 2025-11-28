package com.condominio.service.interfaces;

import com.condominio.dto.request.MascotaDTO;
import com.condominio.dto.response.SuccessResult;

public interface IMascotaService {

    int countByCasaId(Long idCasa);
    SuccessResult<Void> addMascota(MascotaDTO mascotaDTO);
    SuccessResult<Void> subtractMascota(MascotaDTO mascotaDTO);

}
