package com.condominio.service.interfaces;

import com.condominio.persistence.model.RoleEntity;
import com.condominio.persistence.model.RoleEnum;
import java.util.Optional;

public interface IRolService {
    Optional<RoleEntity> findByRoleEnum(RoleEnum roleEnum);
}
