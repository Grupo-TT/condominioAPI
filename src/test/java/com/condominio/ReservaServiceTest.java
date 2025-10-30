package com.condominio;

import com.condominio.dto.response.ReservaDTO;
import com.condominio.persistence.model.RecursoComun;
import com.condominio.persistence.model.Reserva;
import com.condominio.persistence.model.SolicitudReservaRecurso;
import com.condominio.persistence.repository.ReservaRepository;
import com.condominio.service.implementation.ReservaService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaService reservaService;

    @Test
    void findAllProximas_exitoso_retornaListaDTO() {

        RecursoComun recursoComun = RecursoComun.builder()
                .id(1L)
                .nombre("Piscina")
                .build();

        SolicitudReservaRecurso solicitud = SolicitudReservaRecurso.builder()
                .id(1L)
                .fechaSolicitud(LocalDate.now().plusDays(1))
                .recursoComun(recursoComun)
                .build();

        Reserva reserva = Reserva.builder()
                .id(1L)
                .estado(true)
                .solicitudReservaRecurso(solicitud)
                .build();

        when(reservaRepository.findBySolicitudReservaRecurso_FechaSolicitudGreaterThanEqual(any(LocalDate.class)))
                .thenReturn(List.of(reserva));

        List<ReservaDTO> resultado = reservaService.findAllProximas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Piscina", resultado.get(0).getNombreRecurso());
        assertEquals(reserva.getSolicitudReservaRecurso().getFechaSolicitud(), resultado.get(0).getFechaReserva());

        verify(reservaRepository, times(1))
                .findBySolicitudReservaRecurso_FechaSolicitudGreaterThanEqual(any(LocalDate.class));
    }

    @Test
    void findAllProximas_sinResultados_lanzaApiException() {
        when(reservaRepository.findBySolicitudReservaRecurso_FechaSolicitudGreaterThanEqual(any(LocalDate.class)))
                .thenReturn(List.of());

        ApiException ex = assertThrows(ApiException.class, () -> reservaService.findAllProximas());

        assertEquals("No hay reservas de los recursos comunes.", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}
