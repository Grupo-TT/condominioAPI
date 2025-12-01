package com.condominio.service.interfaces;

import com.condominio.dto.request.MascotaDTO;
import com.condominio.dto.response.SuccessResult;

import java.util.List;

public interface IMascotaService {

    int countByCasaId(Long idCasa);
    SuccessResult<Void> addMascota(MascotaDTO mascotaDTO);
    SuccessResult<Void> subtractMascota(MascotaDTO mascotaDTO);
    SuccessResult<List<MascotaDTO>> findMascotasByCasa(Long idCasa);

}
