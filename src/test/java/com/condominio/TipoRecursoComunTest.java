package com.condominio;

import com.condominio.dto.request.TipoRecursoComunDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.TipoRecursoComun;
import com.condominio.persistence.repository.TipoRecursoComunRepository;
import com.condominio.service.implementation.TipoRecursoComunService;
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
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;


@ExtendWith(MockitoExtension.class)
public class TipoRecursoComunTest {
    @Mock
    private TipoRecursoComunRepository tipoRecursoComunRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TipoRecursoComunService tipoRecursoComunService;

    @Test
    void testSave_Success() {

        TipoRecursoComunDTO dto = new TipoRecursoComunDTO();
        dto.setNombre("Piscina");
        dto.setDescripcion("Área común para recreación");

        TipoRecursoComun entidad = new TipoRecursoComun();
        entidad.setNombre("Piscina");
        entidad.setDescripcion("Área común para recreación");

        when(tipoRecursoComunRepository.existsByNombreIgnoreCase("Piscina")).thenReturn(false);
        when(modelMapper.map(dto, TipoRecursoComun.class)).thenReturn(entidad);


        SuccessResult<TipoRecursoComunDTO> result = tipoRecursoComunService.save(dto);


        assertEquals("Tipo de recurso registrado correctamente", result.message());
        assertEquals(dto.getNombre(), result.data().getNombre());
        assertEquals(dto.getDescripcion(), result.data().getDescripcion());
        verify(tipoRecursoComunRepository, times(1)).save(any(TipoRecursoComun.class));
    }

    @Test
    void testSave_DuplicateThrowsException() {

        TipoRecursoComunDTO dto = new TipoRecursoComunDTO();
        dto.setNombre("Piscina");
        dto.setDescripcion("Área común para recreación");

        when(tipoRecursoComunRepository.existsByNombreIgnoreCase("Piscina")).thenReturn(true);


        ApiException exception = assertThrows(ApiException.class, () -> tipoRecursoComunService.save(dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("ya se  encuentra registrado"));
        verify(tipoRecursoComunRepository, never()).save(any(TipoRecursoComun.class));
    }

    @Test
    void testFindAll_Success() {
        TipoRecursoComun recurso1 = new TipoRecursoComun();
        recurso1.setNombre("Gimnasio");
        TipoRecursoComun recurso2 = new TipoRecursoComun();
        recurso2.setNombre("Piscina");

        when(tipoRecursoComunRepository.findAll()).thenReturn(Arrays.asList(recurso1, recurso2));


        List<TipoRecursoComun> result = tipoRecursoComunService.findAll();


        assertEquals(2, result.size());
        assertEquals("Gimnasio", result.getFirst().getNombre());
        verify(tipoRecursoComunRepository, times(1)).findAll();
    }

    @Test
    void testFindAll_EmptyThrowsException() {

        when(tipoRecursoComunRepository.findAll()).thenReturn(Collections.emptyList());


        ApiException exception = assertThrows(ApiException.class, () -> tipoRecursoComunService.findAll());

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("No se encontraron tipos de recursos.", exception.getMessage());
        verify(tipoRecursoComunRepository, times(1)).findAll();
    }

}

