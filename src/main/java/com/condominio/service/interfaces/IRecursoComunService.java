package com.condominio.service.interfaces;

import com.condominio.dto.request.RecursoComunDTO;
import com.condominio.dto.response.RecursoComunPropiDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.DisponibilidadRecurso;
import com.condominio.persistence.model.RecursoComun;
import com.condominio.persistence.model.TipoRecursoComun;


import java.util.List;

public interface IRecursoComunService {

    List<RecursoComun> findAll();
    SuccessResult<RecursoComun> save(RecursoComunDTO recurso);
    SuccessResult<RecursoComun> update(Long id,RecursoComunDTO recurso);
    SuccessResult<RecursoComun> cambiarDisponibilidad(Long id, DisponibilidadRecurso disponibilidad);
    List<RecursoComun> findByTipoRecurso(TipoRecursoComun tipoRecursoComun);
    List<RecursoComunPropiDTO> findByDisponibilidad();
}
