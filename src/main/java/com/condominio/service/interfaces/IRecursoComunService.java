package com.condominio.service.interfaces;

import com.condominio.dto.request.RecursoComunDTO;
import com.condominio.dto.response.RecursoComunPropiDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.RecursoComun;
import com.condominio.persistence.model.TipoRecursoComun;


import java.util.List;

public interface IRecursoComunService {

    List<RecursoComun> findAll();
    SuccessResult<RecursoComun> save(RecursoComunDTO recurso);
    SuccessResult<RecursoComun> update(Long id,RecursoComunDTO recurso);
    SuccessResult<RecursoComun> habilitar(Long id);
    SuccessResult<RecursoComun> deshabilitar(Long id);
    SuccessResult<RecursoComun> enMantenimiento(Long id);
    List<RecursoComun> findByTipoRecurso(TipoRecursoComun tipoRecursoComun);
    List<RecursoComunPropiDTO> findByDisponibilidad();
}
