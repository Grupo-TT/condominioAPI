package com.condominio.service.interfaces;

import com.condominio.dto.request.RecursoComunDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.RecursoComun;


import java.util.List;
import java.util.Optional;

public interface IRecursoComunService {

    List<RecursoComun> findAll();
    SuccessResult<RecursoComun> save(RecursoComunDTO recurso);
    SuccessResult<RecursoComun> update(Long id,RecursoComunDTO recurso);
    SuccessResult<RecursoComun> habilitar(Long id);
    SuccessResult<RecursoComun> deshabilitar(Long id);
    Optional<RecursoComun> findById(Long id);
}
