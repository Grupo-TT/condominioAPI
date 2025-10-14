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
import org.mockito.ArgumentCaptor;
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

    @Test
    void testUpdate_Success() {
        Long id = 1L;


        RecursoComunDTO dto = new RecursoComunDTO();
        dto.setNombre("Piscina");
        dto.setDescripcion("Piscina olímpica");
        TipoRecursoComun tipo = new TipoRecursoComun();
        tipo.setId(2L);
        dto.setTipoRecursoComun(tipo);


        RecursoComun oldRecurso = new RecursoComun();
        oldRecurso.setId(id);
        oldRecurso.setNombre("Cancha");
        oldRecurso.setDescripcion("Cancha de fútbol");
        oldRecurso.setTipoRecursoComun(new TipoRecursoComun());


        TipoRecursoComun tipoEncontrado = new TipoRecursoComun();
        tipoEncontrado.setId(2L);
        tipoEncontrado.setNombre("Zona húmeda");
        tipoEncontrado.setDescripcion("Área de piscina y jacuzzi");


        when(recursoComunRepository.findById(id)).thenReturn(Optional.of(oldRecurso));
        when(recursoComunRepository.findByNombreIgnoreCase("Piscina")).thenReturn(Optional.empty());
        when(tipoRecursoComunRepository.findById(2L)).thenReturn(Optional.of(tipoEncontrado));
        when(recursoComunRepository.save(any(RecursoComun.class))).thenAnswer(invocation -> invocation.getArgument(0));


        SuccessResult<RecursoComun> result = recursoComunService.update(id, dto);


        assertNotNull(result);
        assertEquals("Recurso modificado exitosamente", result.message());
        assertEquals("Piscina", result.data().getNombre());
        assertEquals("Piscina olímpica", result.data().getDescripcion());
        assertNotNull(result.data().getTipoRecursoComun());
        assertEquals(2L, result.data().getTipoRecursoComun().getId());
        assertEquals("Zona húmeda", result.data().getTipoRecursoComun().getNombre());
        assertEquals("Área de piscina y jacuzzi", result.data().getTipoRecursoComun().getDescripcion());


        verify(recursoComunRepository).findById(id);
        verify(recursoComunRepository).findByNombreIgnoreCase("Piscina");
        verify(tipoRecursoComunRepository).findById(2L);
        verify(recursoComunRepository).save(any(RecursoComun.class));
    }

    @Test
    void testUpdate_ThrowsConflictWhenAnotherResourceHasSameName() {
        Long id = 1L;


        RecursoComunDTO dto = new RecursoComunDTO();
        dto.setNombre("Piscina");
        dto.setDescripcion("Piscina olímpica");
        TipoRecursoComun tipo = new TipoRecursoComun();
        tipo.setId(2L);
        dto.setTipoRecursoComun(tipo);


        RecursoComun oldRecurso = new RecursoComun();
        oldRecurso.setId(id);
        oldRecurso.setNombre("Cancha");
        oldRecurso.setDescripcion("Cancha de fútbol");
        oldRecurso.setTipoRecursoComun(new TipoRecursoComun());


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
        verifyNoInteractions(tipoRecursoComunRepository);
        verify(recursoComunRepository, never()).save(any(RecursoComun.class));
    }
    @Test
    void testSave_ThrowsException_WhenTipoRecursoIsNull() {

        RecursoComunDTO dto = new RecursoComunDTO();
        dto.setNombre("Cancha");
        dto.setDescripcion("Cancha de fútbol");
        dto.setTipoRecursoComun(null);

        when(recursoComunRepository.existsByNombreIgnoreCase("Cancha")).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () -> recursoComunService.save(dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Tipo de recurso válido"));

        verify(recursoComunRepository, never()).save(any(RecursoComun.class));
        verify(tipoRecursoComunRepository, never()).findById(anyLong());
    }

    @Test
    void testSave_ThrowsException_WhenTipoRecursoIdIsNull() {
        RecursoComunDTO dto = new RecursoComunDTO();
        dto.setNombre("Cancha");
        dto.setDescripcion("Cancha de fútbol");
        dto.setTipoRecursoComun(new TipoRecursoComun());

        when(recursoComunRepository.existsByNombreIgnoreCase("Cancha")).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () -> recursoComunService.save(dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Tipo de recurso válido"));

        verify(recursoComunRepository, never()).save(any(RecursoComun.class));
        verify(tipoRecursoComunRepository, never()).findById(anyLong());
    }

    @Test
    void findById_shouldReturnOptional_whenExists() {

        Long id = 1L;
        RecursoComun recurso = new RecursoComun();
        recurso.setId(id);

        when(recursoComunRepository.findById(id)).thenReturn(Optional.of(recurso));

        Optional<RecursoComun> result = recursoComunService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(recursoComunRepository, times(1)).findById(id);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {

        Long id = 2L;
        when(recursoComunRepository.findById(id)).thenReturn(Optional.empty());

        Optional<RecursoComun> result = recursoComunService.findById(id);

        assertTrue(result.isEmpty());
        verify(recursoComunRepository, times(1)).findById(id);
    }

    @Test
    void habilitar_shouldSetEstadoTrue_andSave_whenResourceExists() {

        Long id = 10L;
        RecursoComun recurso = new RecursoComun();
        recurso.setId(id);
        recurso.setEstadoRecurso(false);

        when(recursoComunRepository.findById(id)).thenReturn(Optional.of(recurso));
        when(recursoComunRepository.save(any(RecursoComun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SuccessResult<RecursoComun> result = recursoComunService.habilitar(id);

        assertNotNull(result);
        assertEquals("Recurso habilitado exitosamente", result.message());
        assertNotNull(result.data());
        assertTrue(result.data().isEstadoRecurso(), "El recurso debe quedar habilitado (true)");

        ArgumentCaptor<RecursoComun> captor = ArgumentCaptor.forClass(RecursoComun.class);
        verify(recursoComunRepository, times(1)).save(captor.capture());
        assertTrue(captor.getValue().isEstadoRecurso());
    }

    @Test
    void deshabilitar_shouldSetEstadoFalse_andSave_whenResourceExists() {

        Long id = 11L;
        RecursoComun recurso = new RecursoComun();
        recurso.setId(id);
        recurso.setEstadoRecurso(true);

        when(recursoComunRepository.findById(id)).thenReturn(Optional.of(recurso));
        when(recursoComunRepository.save(any(RecursoComun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SuccessResult<RecursoComun> result = recursoComunService.deshabilitar(id);

        // Assert
        assertNotNull(result);
        assertEquals("Recurso deshabilitado exitosamente", result.message());
        assertNotNull(result.data());
        assertFalse(result.data().isEstadoRecurso(), "El recurso debe quedar deshabilitado (false)");

        ArgumentCaptor<RecursoComun> captor = ArgumentCaptor.forClass(RecursoComun.class);
        verify(recursoComunRepository, times(1)).save(captor.capture());
        assertFalse(captor.getValue().isEstadoRecurso());
    }

    @Test
    void habilitar_shouldThrowNoSuchElementException_whenResourceNotFound() {

        Long id = 20L;
        when(recursoComunRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> recursoComunService.habilitar(id));
        verify(recursoComunRepository, never()).save(any());
    }

    @Test
    void deshabilitar_shouldThrowNoSuchElementException_whenResourceNotFound() {

        Long id = 21L;
        when(recursoComunRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> recursoComunService.deshabilitar(id));
        verify(recursoComunRepository, never()).save(any());
    }


}

