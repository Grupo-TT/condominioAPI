package com.condominio.service.implementation;

import com.condominio.persistence.model.RoleEntity;
import com.condominio.persistence.model.RoleEnum;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.RoleRepository;
import com.condominio.persistence.repository.UserRepository;
import com.condominio.service.interfaces.IUserService;
import com.condominio.util.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;


    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       RoleRepository roleRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public Boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();

    }

    @Override
    public UserEntity createUser(String email, Long numeroDeDocumento, RoleEnum rolEnum) {
        if (existsByEmail(email)) {
            throw new ApiException("El email ya está registrado", HttpStatus.BAD_REQUEST);
        }
        RoleEntity role = roleRepository.findByRoleEnum(rolEnum)
                .orElseThrow(() -> new ApiException("Rol no encontrado", HttpStatus.BAD_REQUEST));

        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .contrasenia(passwordEncoder.encode(String.valueOf(numeroDeDocumento)))
                .isEnabled(true)
                .accountNoExpired(true)
                .accountNoLocked(true)
                .credentialNoExpired(true)
                .roles(Set.of(role))
                .build();

        userRepository.save(userEntity);


        return userEntity;

    }

}
