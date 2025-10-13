package com.condominio.service.interfaces;

import com.condominio.persistence.model.RoleEnum;
import com.condominio.persistence.model.UserEntity;



public interface IUserService {

    Boolean existsByEmail(String email);
    UserEntity createUser(String email, Long numeroDeDocumento, RoleEnum rolEnum);
    UserEntity findByEmail(String email);
}

