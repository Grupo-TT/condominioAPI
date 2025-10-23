package com.condominio;

import com.condominio.dto.request.RecursoComunDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.RecursoComun;
import com.condominio.persistence.model.TipoRecursoComun;
import com.condominio.persistence.repository.RecursoComunRepository;
import com.condominio.service.implementation.RecursoComunService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecursoComunServiceTest {

    @Mock
    private RecursoComunRepository recursoComunRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RecursoComunService recursoComunService;

    @Test
    void testSave_Success() {

        RecursoComunDTO dto = new RecursoComunDTO();
        dto.setNombre("Cancha");
        dto.setDescripcion("Cancha de fútbol");

        TipoRecursoComun tipo = TipoRecursoComun.ZONA;
        dto.setTipoRecursoComun(tipo);

        RecursoComun entidad = new RecursoComun();
        entidad.setNombre("Cancha");
        entidad.setDescripcion("Cancha de fútbol");
        entidad.setTipoRecursoComun(tipo);

        when(recursoComunRepository.existsByNombreIgnoreCase("Cancha")).thenReturn(false);
        when(modelMapper.map(dto, RecursoComun.class)).thenReturn(entidad);
        when(recursoComunRepository.save(any(RecursoComun.class))).thenReturn(entidad);


        SuccessResult<RecursoComun> result = recursoComunService.save(dto);


        assertEquals("Recurso registrado correctamente", result.message());
        assertEquals("Cancha", result.data().getNombre());
        assertEquals("Cancha de fútbol", result.data().getDescripcion());
        assertEquals(tipo, result.data().getTipoRecursoComun());
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
    void testUpdate_Success() {
        Long id = 1L;


        RecursoComunDTO dto = new RecursoComunDTO();
        dto.setNombre("Piscina");
        dto.setDescripcion("Piscina olímpica");
        TipoRecursoComun tipo = TipoRecursoComun.ZONA;
        dto.setTipoRecursoComun(tipo);


        RecursoComun oldRecurso = new RecursoComun();
        oldRecurso.setId(id);
        oldRecurso.setNombre("Cancha");
        oldRecurso.setDescripcion("Cancha de fútbol");
        oldRecurso.setTipoRecursoComun(tipo);


        TipoRecursoComun tipoEncontrado = TipoRecursoComun.ZONA;


        when(recursoComunRepository.findById(id)).thenReturn(Optional.of(oldRecurso));
        when(recursoComunRepository.findByNombreIgnoreCase("Piscina")).thenReturn(Optional.empty());
        when(recursoComunRepository.save(any(RecursoComun.class))).thenAnswer(invocation -> invocation.getArgument(0));


        SuccessResult<RecursoComun> result = recursoComunService.update(id, dto);


        assertNotNull(result);
        assertEquals("Recurso modificado exitosamente", result.message());
        assertEquals("Piscina", result.data().getNombre());
        assertEquals("Piscina olímpica", result.data().getDescripcion());
        assertNotNull(result.data().getTipoRecursoComun());
        assertEquals(tipoEncontrado, result.data().getTipoRecursoComun());



        verify(recursoComunRepository).findById(id);
        verify(recursoComunRepository).findByNombreIgnoreCase("Piscina");
        verify(recursoComunRepository).save(any(RecursoComun.class));
    }

    @Test
    void testUpdate_ThrowsConflictWhenAnotherResourceHasSameName() {
        Long id = 1L;


        RecursoComunDTO dto = new RecursoComunDTO();
        dto.setNombre("Piscina");
        dto.setDescripcion("Piscina olímpica");
        TipoRecursoComun tipo = TipoRecursoComun.ZONA;
        dto.setTipoRecursoComun(tipo);


        RecursoComun oldRecurso = new RecursoComun();
        oldRecurso.setId(id);
        oldRecurso.setNombre("Cancha");
        oldRecurso.setDescripcion("Cancha de fútbol");
        oldRecurso.setTipoRecursoComun(tipo);


        RecursoComun other = new RecursoComun();
        other.setId(99L);
        other.setNombre("Piscina");
        other.setDescripcion("Piscina comunitaria");


        when(recursoComunRepository.findById(id)).thenReturn(Optional.of(oldRecurso));
        when(recursoComunRepository.findByNombreIgnoreCase("Piscina")).thenReturn(Optional.of(other));


        ApiException ex = assertThrows(ApiException.class, () -> recursoComunService.update(id, dto));
        assertEquals("Ya existe un recurso con ese nombre", ex.getMessage());



        verify(recursoComunRepository).findById(id);
        verify(recursoComunRepository).findByNombreIgnoreCase("Piscina");
        verify(recursoComunRepository, never()).save(any(RecursoComun.class));
    }
    @Test
    void testSave_ThrowsException_WhenTipoRecursoIsNull() {
        Long id = 1L;
        RecursoComunDTO dto = new RecursoComunDTO();
        dto.setNombre("Cancha");
        dto.setDescripcion("Cancha de fútbol");
        dto.setTipoRecursoComun(null);

        RecursoComun oldRecurso = new RecursoComun();
        oldRecurso.setId(id);

        when(recursoComunRepository.findById(id)).thenReturn(Optional.of(oldRecurso));

        ApiException exception = assertThrows(ApiException.class, () -> recursoComunService.update(id, dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Tipo de recurso válido"));

        verify(recursoComunRepository).findById(id);
        verify(recursoComunRepository, never()).save(any(RecursoComun.class));
    }

    @Test
    void habilitar_shouldSetEstadoTrue_andSave_whenResourceExists() {

        Long id = 1L;
        RecursoComun recurso = new RecursoComun();
        recurso.setId(id);
        recurso.setEstadoRecurso(false);

        when(recursoComunRepository.findById(id)).thenReturn(Optional.of(recurso));
        when(recursoComunRepository.save(any(RecursoComun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SuccessResult<RecursoComun> result = recursoComunService.habilitar(id);

        assertNotNull(result);
        assertEquals("Recurso habilitado exitosamente", result.message());
        assertTrue(result.data().isEstadoRecurso(), "El recurso debe quedar habilitado (true)");

        verify(recursoComunRepository).findById(id);
        verify(recursoComunRepository).save(recurso);
    }

    @Test
    void deshabilitar_shouldSetEstadoFalse_andSave_whenResourceExists() {

        Long id = 2L;
        RecursoComun recurso = new RecursoComun();
        recurso.setId(id);
        recurso.setEstadoRecurso(true);

        when(recursoComunRepository.findById(id)).thenReturn(Optional.of(recurso));
        when(recursoComunRepository.save(any(RecursoComun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SuccessResult<RecursoComun> result = recursoComunService.deshabilitar(id);

        assertNotNull(result);
        assertEquals("Recurso deshabilitado exitosamente", result.message());
        assertFalse(result.data().isEstadoRecurso(), "El recurso debe quedar deshabilitado (false)");

        verify(recursoComunRepository).findById(id);
        verify(recursoComunRepository).save(recurso);
    }

    @Test
    void habilitar_shouldThrowApiException_whenResourceNotFound() {

        Long id = 3L;
        when(recursoComunRepository.findById(id)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> recursoComunService.habilitar(id));

        assertEquals("El recurso no existe", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(recursoComunRepository).findById(id);
        verify(recursoComunRepository, never()).save(any());
    }

    @Test
    void deshabilitar_shouldThrowApiException_whenResourceNotFound() {

        Long id = 4L;
        when(recursoComunRepository.findById(id)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> recursoComunService.deshabilitar(id));

        assertEquals("El recurso no existe", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(recursoComunRepository).findById(id);
        verify(recursoComunRepository, never()).save(any());
    }

    @Test
    void habilitar_shouldThrowApiException_whenAlreadyEnabled() {

        Long id = 2L;
        RecursoComun recurso = new RecursoComun();
        recurso.setId(id);
        recurso.setEstadoRecurso(true);

        when(recursoComunRepository.findById(id)).thenReturn(Optional.of(recurso));

        ApiException ex = assertThrows(ApiException.class, () -> recursoComunService.habilitar(id));
        assertEquals("El recurso ya está habilitado", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(recursoComunRepository).findById(id);
        verify(recursoComunRepository, never()).save(any());
    }

    @Test
    void deshabilitar_shouldThrowApiException_whenAlreadyDisabled() {

        Long id = 11L;
        RecursoComun recurso = new RecursoComun();
        recurso.setId(id);
        recurso.setEstadoRecurso(false);

        when(recursoComunRepository.findById(id)).thenReturn(Optional.of(recurso));

        ApiException ex = assertThrows(ApiException.class, () -> recursoComunService.deshabilitar(id));
        assertEquals("El recurso ya está deshabilitado", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(recursoComunRepository).findById(id);
        verify(recursoComunRepository, never()).save(any());
    }

    @Test
    void testFindByTipoRecurso_ReturnsListOfResources() {

        TipoRecursoComun tipo = TipoRecursoComun.ZONA;

        RecursoComun recurso1 = new RecursoComun();
        recurso1.setId(1L);
        recurso1.setNombre("Piscina Olímpica");
        recurso1.setTipoRecursoComun(tipo);

        RecursoComun recurso2 = new RecursoComun();
        recurso2.setId(2L);
        recurso2.setNombre("Piscina Infantil");
        recurso2.setTipoRecursoComun(tipo);

        List<RecursoComun> recursos = List.of(recurso1, recurso2);

        when(recursoComunRepository.findByTipoRecursoComun(tipo)).thenReturn(recursos);

        List<RecursoComun> result = recursoComunService.findByTipoRecurso(tipo);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Piscina Olímpica", result.get(0).getNombre());
        assertEquals(TipoRecursoComun.ZONA, result.get(0).getTipoRecursoComun());
        verify(recursoComunRepository, times(1)).findByTipoRecursoComun(tipo);
    }

    @Test
    void testFindByTipoRecurso_ReturnsEmptyList_WhenNoResourcesFound() {

        TipoRecursoComun tipo = TipoRecursoComun.ZONA;
        when(recursoComunRepository.findByTipoRecursoComun(tipo)).thenReturn(Collections.emptyList());

        List<RecursoComun> result = recursoComunService.findByTipoRecurso(tipo);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(recursoComunRepository, times(1)).findByTipoRecursoComun(tipo);
    }

}

