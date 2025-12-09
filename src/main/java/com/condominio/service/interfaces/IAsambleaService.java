package com.condominio.service.interfaces;

import com.condominio.dto.request.AsambleaDTO;
import com.condominio.dto.response.AsambleaConAsistenciaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Asamblea;

import java.util.List;

public interface IAsambleaService {

    SuccessResult<AsambleaDTO> create(AsambleaDTO asamblea);
    SuccessResult<List<Asamblea>> findAllAsambleas();
    SuccessResult<AsambleaDTO> edit(AsambleaDTO asambleaEdit, Long id);
    SuccessResult<Void> delete(Long id);
    SuccessResult<AsambleaConAsistenciaDTO> getAsambleaById(Long id);
    SuccessResult<Void> cambiarEstado(Long id, String estado);
}
