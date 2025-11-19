package com.condominio.service.implementation;

import com.condominio.dto.request.PasswordUpdateDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.RoleEntity;
import com.condominio.persistence.model.RoleEnum;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.RoleRepository;
import com.condominio.persistence.repository.UserRepository;
import com.condominio.service.interfaces.IUserService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Set;


@RequiredArgsConstructor
@Service
public class UserService implements IUserService, UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PersonaRepository personaRepository;
    private final EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findUserEntityByEmail(username);

        if (user == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }

        String[] roles = user.getRoles()
                .stream()
                .map(roleEntity -> roleEntity.getRoleEnum().name())
                .toArray(String[]::new);

        return User.builder()
                .username(user.getEmail())
                .password(user.getContrasenia())
                .roles(roles)
                .disabled(!user.isEnabled())
                .accountExpired(!user.isAccountNoExpired())
                .accountLocked(!user.isAccountNoLocked())
                .credentialsExpired(!user.isCredentialNoExpired())
                .build();
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

    public UserEntity findByEmail(String email) {
        return userRepository.findUserEntityByEmail(email);
    }

    public Persona findPersonaByUser(UserEntity user) {
        return personaRepository.findPersonaByUser(user);
    }

    public SuccessResult<Void> changePassword(UserDetails userDetails, PasswordUpdateDTO dto) {

        UserEntity userEntity = userRepository.findUserEntityByEmail(userDetails.getUsername());

        if (!passwordEncoder.matches(dto.getCurrentPassword(), userEntity.getContrasenia())) {
            throw new ApiException("La contraseña actual no es correcta", HttpStatus.OK);
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new ApiException("Las contraseñas nuevas no coinciden",HttpStatus.OK);
        }

        userEntity.setContrasenia(passwordEncoder.encode(dto.getNewPassword()));

        userRepository.save(userEntity);
        return new SuccessResult<>("Password actualizada correctamente",null);
    }


    public void recuperarPassword(String email) {
        UserEntity usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("El usuario no existe", HttpStatus.OK));

        SecureRandom random = new SecureRandom();
        int numero = 100000 + random.nextInt(900000);
        String nuevaPassword = String.valueOf(numero);
        usuario.setContrasenia(passwordEncoder.encode(nuevaPassword));
        userRepository.save(usuario);

        Persona persona = personaRepository.findByUser_Id(usuario.getId());
        String nombreUsuario = persona.getNombreCompleto();

        emailService.enviarPasswordOlvidada(email, nuevaPassword, nombreUsuario);
    }
}
