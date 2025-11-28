package com.condominio.service.interfaces;

import com.condominio.dto.request.PqrsUpdateDTO;
import com.condominio.dto.response.PqrsDTO;
import com.condominio.dto.response.PqrsPropiDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.EstadoPqrs;

import java.util.List;

public interface IPqrsService {
    SuccessResult<List<PqrsDTO>> findByEstado(EstadoPqrs estado);
    SuccessResult<PqrsUpdateDTO> update(Long id, PqrsUpdateDTO pqrs);
    SuccessResult<PqrsDTO> marcarRevisada(Long id);
    SuccessResult<PqrsDTO> eliminar(Long id);
    SuccessResult<PqrsPropiDTO> crearPqrs(PqrsPropiDTO pqrs);
    SuccessResult<PqrsPropiDTO> modificarPqrs(Long id, PqrsPropiDTO pqrs);
}
