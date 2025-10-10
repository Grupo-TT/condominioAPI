package com.condominio.service.interfaces;

import com.condominio.dto.request.TipoRecursoComunDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.TipoRecursoComun;

import java.util.List;

public interface ITipoRecursoComun {
    List<TipoRecursoComun> findAll();
    SuccessResult<TipoRecursoComunDTO> save(TipoRecursoComunDTO tipoRecurso);
}
