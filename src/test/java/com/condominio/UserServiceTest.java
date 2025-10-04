package com.condominio;

import com.condominio.persistence.model.RoleEntity;
import com.condominio.persistence.model.RoleEnum;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.RoleRepository;
import com.condominio.persistence.repository.UserRepository;
import com.condominio.service.implementation.UserService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // Arrange
        UserEntity userEntity = mock(UserEntity.class);
        when(userEntity.getEmail()).thenReturn("test@example.com");
        when(userEntity.getContrasenia()).thenReturn("encodedPassword");
        when(userEntity.getRoles()).thenReturn(Set.of(mock(RoleEntity.class)));

        when(userRepository.findUserEntityByEmail("test@example.com")).thenReturn(userEntity);

        // Act
        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails, "UserDetails no debe ser nulo");
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        // Al menos una autoridad presente (no comprobamos nombre exacto porque depende de tu mapeo).
        assertFalse(userDetails.getAuthorities().isEmpty());
        verify(userRepository, times(1)).findUserEntityByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_shouldThrow_whenUserNotFound() {
        // Arrange
        when(userRepository.findUserEntityByEmail("missing@example.com")).thenReturn(null);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("missing@example.com");
        });
        verify(userRepository, times(1)).findUserEntityByEmail("missing@example.com");
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {

        String email = "test@example.com";
        Long documento = 123456L;

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new UserEntity()));


        ApiException exception = assertThrows(ApiException.class, () ->
                userService.createUser(email, documento, RoleEnum.PROPIETARIO)
        );

        assertEquals("El email ya está registrado", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(userRepository).findByEmail(email);
        verifyNoInteractions(roleRepository, passwordEncoder);
    }

    @Test
    void createUser_ShouldReturnUser_WhenEmailIsNew() {

        String email = "nuevo@example.com";
        Long documento = 123456L;

        RoleEntity role = new RoleEntity();
        role.setId(1L);
        role.setRoleEnum(RoleEnum.PROPIETARIO);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(roleRepository.findByRoleEnum(RoleEnum.PROPIETARIO)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(String.valueOf(documento))).thenReturn("encodedPassword");


        UserEntity createdUser = userService.createUser(email, documento, RoleEnum.PROPIETARIO);


        assertNotNull(createdUser);
        assertEquals(email, createdUser.getEmail());
        assertEquals("encodedPassword", createdUser.getContrasenia());
        assertTrue(createdUser.isEnabled());
        assertTrue(createdUser.isAccountNoExpired());
        assertTrue(createdUser.isAccountNoLocked());
        assertTrue(createdUser.isCredentialNoExpired());
        assertTrue(createdUser.getRoles().contains(role));

        verify(userRepository).save(createdUser);
        verify(roleRepository).findByRoleEnum(RoleEnum.PROPIETARIO);
        verify(passwordEncoder).encode(String.valueOf(documento));
    }
}