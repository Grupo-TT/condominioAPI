package com.condominio.service.implementation;

import com.condominio.persistence.model.RoleEntity;
import com.condominio.persistence.model.RoleEnum;
import com.condominio.persistence.repository.RoleRepository;
import com.condominio.service.interfaces.IRolService;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class RolService implements IRolService {

    RoleRepository roleRepository;

    public RolService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    @Override
    public Optional<RoleEntity> findByRoleEnum(RoleEnum roleEnum) {
        return roleRepository.findByRoleEnum(roleEnum);
    }
}
