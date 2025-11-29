package com.condominio;

import com.condominio.persistence.model.CorreoEnviado;
import com.condominio.persistence.repository.CorreoEnviadoRepository;
import com.condominio.service.implementation.CorreoEnviadoService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorreoEnviadoServiceTest {

    private CorreoEnviadoRepository correoEnviadoRepository;
    private CorreoEnviadoService service;

    @BeforeEach
    void setUp() {
        correoEnviadoRepository = mock(CorreoEnviadoRepository.class);
        service = new CorreoEnviadoService(correoEnviadoRepository);
    }

    @Test
    void findAll_nonEmptyList_returnsList() {
        // Mock lista con correos
        CorreoEnviado correo = new CorreoEnviado();
        correo.setTitulo("Asunto 1");
        correo.setCuerpo("Cuerpo del correo");
        List<CorreoEnviado> lista = new ArrayList<>();
        lista.add(correo);

        when(correoEnviadoRepository.findAll()).thenReturn(lista);

        // Ejecutar
        List<CorreoEnviado> result = service.findAll();

        // Verificar
        assertEquals(1, result.size());
        assertEquals("Asunto 1", result.get(0).getTitulo());
        assertEquals("Cuerpo del correo", result.get(0).getCuerpo());
    }

    @Test
    void findAll_emptyList_throwsApiException() {
        when(correoEnviadoRepository.findAll()).thenReturn(List.of());

        ApiException ex = assertThrows(ApiException.class, () -> service.findAll());

        assertEquals("No hay correos enviados", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}

