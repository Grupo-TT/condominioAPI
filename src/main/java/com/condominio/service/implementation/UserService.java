package com.condominio.service.implementation;

import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findUserEntityByEmail(username);

        return User.builder()
                .username(user.getEmail())
                .password(user.getContrasenia())
                .roles(String.valueOf(user.getRoles()))
                .build();
    }
}
