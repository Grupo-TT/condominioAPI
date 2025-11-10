package com.condominio;

import com.condominio.persistence.model.ActualizacionHelper;
import com.condominio.persistence.model.Cargo;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActualizacionHelperTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private ActualizacionHelper actualizacionHelper;

    @BeforeEach
    void setUp() {
        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@user.com");
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("test@user.com");
        when(userRepository.findUserEntityByEmail("test@user.com")).thenReturn(userEntity);

        Persona persona = mock(Persona.class);
        when(persona.getNombreCompleto()).thenReturn("John Tester");
        when(personaRepository.findPersonaByUser(userEntity)).thenReturn(persona);
    }


    @Test
    void aplicarDatosComunes_sinDividirPorCien() {
        Cargo cargo = new Cargo();
        cargo.setNuevoValor(500);

        Cargo result = actualizacionHelper.aplicarDatosComunes(cargo, 800);

        assertEquals(500, result.getValorActual());
        assertEquals(800, result.getNuevoValor());
        assertEquals("test@user.com", result.getCorreoActualizador());
        assertEquals("John Tester", result.getNombreActualizador());

    }

    @Test
    void aplicarDatosComunes_conDividirPorCien() {
        Cargo cargo = new Cargo();
        cargo.setNuevoValor(1000);

        Cargo result = actualizacionHelper.aplicarDatosComunes(cargo, 800, true);

        assertEquals(1000, result.getValorActual());
        assertEquals(8.0, result.getNuevoValor());
    }
}
