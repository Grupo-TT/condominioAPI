package com.condominio.service.interfaces;

import com.condominio.dto.request.PersonaRegistroDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.RoleEnum;

public interface IPersonaService {
    Boolean existsByNumeroDeDocumento(Long numeroDeDocumento);
    SuccessResult<Persona> save(PersonaRegistroDTO persona);
    boolean existsRoleInCasa(Long casaId, RoleEnum roleEnum);


}
