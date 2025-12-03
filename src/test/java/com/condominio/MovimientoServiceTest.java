package com.condominio;

import com.condominio.dto.request.MovimientoRequestDTO;
import com.condominio.dto.response.MetricasDTO;
import com.condominio.dto.response.MovimientoDTO;
import com.condominio.dto.response.MovimientosMesDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Movimiento;
import com.condominio.persistence.model.TipoMovimiento;
import com.condominio.persistence.repository.MovimientoRepository;
import com.condominio.service.implementation.MovimientoService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovimientoServiceTest {
    @Mock
    private MovimientoRepository repo;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private MovimientoService service;

    private Movimiento movimiento1;
    private Movimiento movimiento2;

    private MovimientoDTO movimientoDto1;
    private MovimientoDTO movimientoDto2;

    @BeforeEach
    void setup() {
        movimiento1 = Movimiento.builder()
                .id(1L)
                .descripcion("Pago administración")
                .fechaMovimiento(LocalDate.of(2024, 1, 5))
                .tipoMovimiento(TipoMovimiento.ENTRADA)
                .monto(150_000)
                .responsable("Juan")
                .build();

        movimiento2 = Movimiento.builder()
                .id(2L)
                .descripcion("Pago servicios")
                .fechaMovimiento(LocalDate.of(2024, 1, 10))
                .tipoMovimiento(TipoMovimiento.SALIDA)
                .monto(430_000)
                .responsable(null)
                .build();

        movimientoDto1 = new MovimientoDTO();
        movimientoDto1.setId(1L);
        movimientoDto1.setDescripcion("Pago administración");
        movimientoDto1.setFecha(LocalDate.of(2024,1,5));
        movimientoDto1.setTipo(TipoMovimiento.ENTRADA);
        movimientoDto1.setMonto(150_000);

        movimientoDto2 = new MovimientoDTO();
        movimientoDto2.setId(2L);
        movimientoDto2.setDescripcion("Pago servicios");
        movimientoDto2.setFecha(LocalDate.of(2024,1,10));
        movimientoDto2.setTipo(TipoMovimiento.SALIDA);
        movimientoDto2.setMonto(430_000);
    }

    @Test
    void getMovimientosPorMes_returnsListAndMetrics() {
        // arrange
        int mes = 1;
        int anio = 2024;

        LocalDate desde = LocalDate.of(anio, mes, 1);
        LocalDate hasta = LocalDate.of(anio, mes, 31);

        when(repo.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(desde, hasta))
                .thenReturn(List.of(movimiento1, movimiento2));

        // cifras realistas en "pesos"
        when(repo.sumMontoByTipoBetween(TipoMovimiento.ENTRADA, desde, hasta)).thenReturn(150000);
        when(repo.sumMontoByTipoBetween(TipoMovimiento.SALIDA, desde, hasta)).thenReturn(430000);

        LocalDate desdeTodo = LocalDate.of(1900, 1, 1);
        when(repo.sumMontoByTipoBetween(TipoMovimiento.ENTRADA, desdeTodo, hasta)).thenReturn(1500000);
        when(repo.sumMontoByTipoBetween(TipoMovimiento.SALIDA, desdeTodo, hasta)).thenReturn(800000);

        // model mapper
        when(modelMapper.map(movimiento1, MovimientoDTO.class)).thenReturn(movimientoDto1);
        when(modelMapper.map(movimiento2, MovimientoDTO.class)).thenReturn(movimientoDto2);

        // act
        SuccessResult<MovimientosMesDTO> result = service.getMovimientosPorMes(mes, anio);

        // assert
        assertNotNull(result);
        assertEquals("Movimientos obtenidos exitosamente", result.message());
        assertNotNull(result.data());

        MovimientosMesDTO wrapper = result.data();
        assertNotNull(wrapper);
        assertNotNull(wrapper.getMovimientos());
        assertEquals(2, wrapper.getMovimientos().size());

        MetricasDTO metricas = wrapper.getMetricas();
        assertNotNull(metricas);

        // comprobaciones con las cifras realistas
        assertEquals(150000, metricas.getIngresos());
        assertEquals(430000, metricas.getEgresos());
        assertEquals(150000 - 430000, metricas.getBalance());      // -280000
        assertEquals(1500000 - 800000, metricas.getSaldoActual()); // 700000

        verify(repo).findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(desde, hasta);
        verify(modelMapper, times(2)).map(any(Movimiento.class), eq(MovimientoDTO.class));
    }

    @Test
    void crearMovimiento_savesAndReturnsDto() {
        // arrange
        MovimientoRequestDTO req = new MovimientoRequestDTO();
        req.setFecha(LocalDate.of(2024, 2, 1));
        req.setTipo(TipoMovimiento.ENTRADA);
        req.setDescripcion("Ingreso prueba");
        req.setMonto(100_000);
        req.setCategoria(null);
        req.setResponsable("Admin");

        Movimiento saved = Movimiento.builder()
                .id(10L)
                .fechaMovimiento(req.getFecha())
                .tipoMovimiento(req.getTipo())
                .categoriaMovimiento(req.getCategoria())
                .descripcion(req.getDescripcion())
                .monto(req.getMonto())
                .responsable(req.getResponsable())
                .build();

        MovimientoDTO mappedDto = new MovimientoDTO();
        mappedDto.setId(10L);
        mappedDto.setDescripcion(req.getDescripcion());
        mappedDto.setFecha(req.getFecha());
        mappedDto.setMonto(req.getMonto());

        when(repo.save(any(Movimiento.class))).thenReturn(saved);
        when(modelMapper.map(saved, MovimientoDTO.class)).thenReturn(mappedDto);

        // act
        SuccessResult<MovimientoDTO> result = service.crearMovimiento(req);

        // assert
        assertNotNull(result);
        assertEquals("Movimiento creado exitosamente", result.message());
        assertNotNull(result.data());
        MovimientoDTO dto = result.data();
        assertEquals(10L, dto.getId());
        assertEquals(req.getDescripcion(), dto.getDescripcion());

        ArgumentCaptor<Movimiento> captor = ArgumentCaptor.forClass(Movimiento.class);
        verify(repo, times(1)).save(captor.capture());
        Movimiento savedArg = captor.getValue();
        assertEquals(req.getFecha(), savedArg.getFechaMovimiento());
        assertEquals(req.getDescripcion(), savedArg.getDescripcion());
    }

    @Test
    void actualizarMovimiento_whenNotFound_throwsApiException() {
        // arrange
        Long id = 999L;
        MovimientoRequestDTO req = new MovimientoRequestDTO();
        when(repo.findById(id)).thenReturn(Optional.empty());

        // act & assert
        ApiException ex = assertThrows(ApiException.class, () -> service.actualizarMovimiento(id, req));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(repo, never()).save(any(Movimiento.class));
    }

    @Test
    void actualizarMovimiento_success_updatesAndReturnsDto() {
        // arrange
        Long id = 1L;
        Movimiento existing = Movimiento.builder()
                .id(id)
                .descripcion("desc old")
                .fechaMovimiento(LocalDate.of(2024,1,1))
                .monto(100)
                .build();

        MovimientoRequestDTO req = new MovimientoRequestDTO();
        req.setFecha(LocalDate.of(2024,1,10));
        req.setTipo(TipoMovimiento.SALIDA);
        req.setDescripcion("desc new");
        req.setMonto(250);
        req.setCategoria(null);
        req.setResponsable("Nuevo");

        when(repo.findById(id)).thenReturn(Optional.of(existing));

        Movimiento updatedEntity = Movimiento.builder()
                .id(id)
                .fechaMovimiento(req.getFecha())
                .tipoMovimiento(req.getTipo())
                .descripcion(req.getDescripcion())
                .monto(req.getMonto())
                .categoriaMovimiento(req.getCategoria())
                .responsable(req.getResponsable())
                .build();

        when(repo.save(any(Movimiento.class))).thenReturn(updatedEntity);

        MovimientoDTO mappedDto = new MovimientoDTO();
        mappedDto.setId(id);
        mappedDto.setDescripcion(req.getDescripcion());
        mappedDto.setFecha(req.getFecha());
        mappedDto.setMonto(req.getMonto());

        when(modelMapper.map(updatedEntity, MovimientoDTO.class)).thenReturn(mappedDto);

        // act
        SuccessResult<MovimientoDTO> result = service.actualizarMovimiento(id, req);

        // assert
        assertNotNull(result);
        assertEquals("Movimiento actualizado exitosamente", result.message());
        assertNotNull(result.data());
        assertEquals(req.getDescripcion(), result.data().getDescripcion());

        verify(repo).findById(id);
        verify(repo).save(any(Movimiento.class));
    }

    @Test
    void eliminarMovimiento_whenNotFound_throwsApiException() {
        // arrange
        Long id = 1000L;
        when(repo.existsById(id)).thenReturn(false);

        // act & assert
        ApiException ex = assertThrows(ApiException.class, () -> service.eliminarMovimiento(id));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(repo, never()).deleteById(anyLong());
    }

    @Test
    void eliminarMovimiento_success_deletes() {
        // arrange
        Long id = 5L;
        when(repo.existsById(id)).thenReturn(true);

        // act
        SuccessResult<Void> result = service.eliminarMovimiento(id);

        // assert
        assertNotNull(result);
        assertEquals("Movimiento eliminado exitosamente", result.message());
        assertNull(result.data());
        verify(repo).deleteById(id);
    }
}
