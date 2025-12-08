package com.condominio;

import com.condominio.dto.response.ResumenFinancieroDTO;
import com.condominio.dto.response.ResumenFinancieroMesDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Movimiento;
import com.condominio.persistence.model.TipoMovimiento;
import com.condominio.persistence.repository.MovimientoRepository;
import com.condominio.service.implementation.DashboardAdminService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardAdminTest {

    @Mock
    MovimientoRepository movimientoRepository;

    @InjectMocks
    DashboardAdminService dashboardAdminService;

    @Test
    void testGetResumenFinancieronByYear_ok() {

        List<Movimiento> movimientos = List.of(
                Movimiento.builder().fechaMovimiento(LocalDate.of(2025, 1, 5)).tipoMovimiento(TipoMovimiento.ENTRADA).monto(100000).build(),
                Movimiento.builder().fechaMovimiento(LocalDate.of(2025, 1, 10)).tipoMovimiento(TipoMovimiento.SALIDA).monto(40000).build(),
                Movimiento.builder().fechaMovimiento(LocalDate.of(2025, 2, 3)).tipoMovimiento(TipoMovimiento.ENTRADA).monto(200000).build(),
                Movimiento.builder().fechaMovimiento(LocalDate.of(2025, 2, 7)).tipoMovimiento(TipoMovimiento.SALIDA).monto(100000).build()
        );

        when(movimientoRepository.findByYear(2025)).thenReturn(movimientos);

        SuccessResult<ResumenFinancieroDTO> result =
                dashboardAdminService.getResumenFinancieronByYear(2025);

        assertEquals("Información obtenida", result.message());

        ResumenFinancieroDTO dto = result.data();
        assertEquals(2025, dto.getYear());
        assertEquals(2, dto.getMeses().size());

        ResumenFinancieroMesDTO enero = dto.getMeses().get(0);
        assertEquals("Ene", enero.getMes());
        assertEquals(100000, enero.getEntradas());
        assertEquals(40000, enero.getSalidas());

        ResumenFinancieroMesDTO febrero = dto.getMeses().get(1);
        assertEquals("Feb", febrero.getMes());
        assertEquals(200000, febrero.getEntradas());
        assertEquals(100000, febrero.getSalidas());

        verify(movimientoRepository, times(1)).findByYear(2025);
    }

    @Test
    void testGetResumenFinancieronByYear_sinMovimientos() {
        when(movimientoRepository.findByYear(2025)).thenReturn(List.of());

        ApiException exception = assertThrows(ApiException.class,
                () -> dashboardAdminService.getResumenFinancieronByYear(2025));

        assertEquals("No hay movimientos para este año", exception.getMessage());
        assertEquals(HttpStatus.OK, exception.getStatus());
    }
}
