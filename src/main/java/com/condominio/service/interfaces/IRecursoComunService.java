package com.condominio.service.interfaces;

import com.condominio.dto.request.RecursoComunDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.RecursoComun;


import java.util.List;

public interface IRecursoComunService {

    List<RecursoComun> findAll();
    SuccessResult<RecursoComunDTO> save(RecursoComunDTO Recurso);
}
