package com.condominio.service.interfaces;

import com.condominio.dto.request.PasswordUpdateDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.RoleEnum;
import com.condominio.persistence.model.UserEntity;
import org.springframework.security.core.userdetails.UserDetails;


public interface IUserService {

    Boolean existsByEmail(String email);
    UserEntity createUser(String email, Long numeroDeDocumento, RoleEnum rolEnum);
    UserEntity findByEmail(String email);
    SuccessResult<Void> changePassword(UserDetails userDetails, PasswordUpdateDTO dto);
    void recuperarPassword(String email);
}

