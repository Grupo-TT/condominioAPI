package com.condominio;

import com.condominio.dto.request.RecursoComunDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.RecursoComun;
import com.condominio.persistence.model.TipoRecursoComun;
import com.condominio.persistence.repository.RecursoComunRepository;
import com.condominio.persistence.repository.TipoRecursoComunRepository;
import com.condominio.service.implementation.RecursoComunService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecursoComunServiceTest {

    @Mock
    private RecursoComunRepository recursoComunRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private TipoRecursoComunRepository tipoRecursoComunRepository;

    @InjectMocks
    private RecursoComunService recursoComunService;

    @Test
    void testSave_Success() {

        RecursoComunDTO dto = new RecursoComunDTO();
        dto.setNombre("Cancha");
        dto.setDescripcion("Cancha de fútbol");

        TipoRecursoComun tipo = new TipoRecursoComun();
        tipo.setId(1L);
        dto.setTipoRecursoComun(tipo);

        RecursoComun entidad = new RecursoComun();
        entidad.setNombre("Cancha");
        entidad.setDescripcion("Cancha de fútbol");
        entidad.setTipoRecursoComun(tipo);

        when(recursoComunRepository.existsByNombreIgnoreCase("Cancha")).thenReturn(false);
        when(tipoRecursoComunRepository.findById(1L)).thenReturn(Optional.of(tipo));
        when(modelMapper.map(dto, RecursoComun.class)).thenReturn(entidad);
        when(recursoComunRepository.save(any(RecursoComun.class))).thenReturn(entidad);


        SuccessResult<RecursoComun> result = recursoComunService.save(dto);


        assertEquals("Recurso registrado correctamente", result.message());
        assertEquals("Cancha", result.data().getNombre());
        assertEquals("Cancha de fútbol", result.data().getDescripcion());
        assertEquals(1L, result.data().getTipoRecursoComun().getId());
        verify(recursoComunRepository, times(1)).save(any(RecursoComun.class));
    }

    @Test
    void testSave_DuplicateThrowsException() {

        RecursoComunDTO dto = new RecursoComunDTO();
        dto.setNombre("Cancha");

        when(recursoComunRepository.existsByNombreIgnoreCase("Cancha")).thenReturn(true);


        ApiException exception = assertThrows(ApiException.class, () -> recursoComunService.save(dto));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertTrue(exception.getMessage().contains("ya existe"));
        verify(recursoComunRepository, never()).save(any(RecursoComun.class));
    }

    @Test
    void testFindAll_Success() {

        RecursoComun recurso1 = new RecursoComun();
        recurso1.setNombre("Piscina");

        RecursoComun recurso2 = new RecursoComun();
        recurso2.setNombre("Salón comunal");

        when(recursoComunRepository.findAll()).thenReturn(Arrays.asList(recurso1, recurso2));


        List<RecursoComun> result = recursoComunService.findAll();


        assertEquals(2, result.size());
        assertEquals("Piscina", result.getFirst().getNombre());
        verify(recursoComunRepository, times(1)).findAll();
    }

    @Test
    void testFindAll_EmptyThrowsException() {

        when(recursoComunRepository.findAll()).thenReturn(Collections.emptyList());


        ApiException exception = assertThrows(ApiException.class, () -> recursoComunService.findAll());

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("No se encontraron recursos comunes .", exception.getMessage());
        verify(recursoComunRepository, times(1)).findAll();
    }

    @Test
    void save_WhenTipoRecursoNoExiste_ShouldThrowApiException() {

        RecursoComunDTO dto = new RecursoComunDTO();
        dto.setNombre("Gimnasio");
        dto.setDescripcion("Área de entrenamiento físico");

        TipoRecursoComun tipo = new TipoRecursoComun();
        tipo.setId(99L);
        dto.setTipoRecursoComun(tipo);

        when(recursoComunRepository.existsByNombreIgnoreCase("Gimnasio")).thenReturn(false);
        when(tipoRecursoComunRepository.findById(99L)).thenReturn(Optional.empty());


        ApiException exception = assertThrows(ApiException.class, () -> recursoComunService.save(dto));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("El Tipo de recurso no existe"));

        verify(recursoComunRepository, never()).save(any());
    }
}

