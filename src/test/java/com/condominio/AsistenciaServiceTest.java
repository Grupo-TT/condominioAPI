package com.condominio;

import com.condominio.dto.request.AsistenciaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Asamblea;
import com.condominio.persistence.model.Asistencia;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.repository.AsambleaRepository;
import com.condominio.persistence.repository.AsistenciaRepository;
import com.condominio.service.implementation.AsistenciaService;
import com.condominio.util.constants.AppConstants;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AsistenciaServiceTest {

    @Mock
    private AsambleaRepository asambleaRepository;

    @Mock
    private AsistenciaRepository asistenciaRepository;

    @InjectMocks
    private AsistenciaService service;

    @Test
    void registrarAsistencia_deberiaRegistrarAsistencias() {
        Date todayInBogota = Date.from(
                LocalDate.now(AppConstants.ZONE)
                        .atStartOfDay(AppConstants.ZONE)
                        .toInstant()
        );

        Asamblea asamblea = new Asamblea();
        asamblea.setId(1L);
        asamblea.setFecha(todayInBogota);

        AsistenciaDTO dto1 = new AsistenciaDTO(101, true);

        Casa casa1 = new Casa();
        casa1.setNumeroCasa(101);

        Asistencia asistencia = new Asistencia();
        asistencia.setAsamblea(asamblea);
        asistencia.setCasa(casa1);
        asistencia.setEstado(false);


        when(asambleaRepository.findById(1L)).thenReturn(Optional.of(asamblea));
        when(asistenciaRepository.findByAsambleaAndCasa_NumeroCasa(asamblea, casa1.getNumeroCasa())).thenReturn(Optional.of(asistencia));


        // Act
        SuccessResult<Void> result = service.registrarAsistencia(1L,dto1);

        // Assert
        verify(asambleaRepository).findById(1L);
        verify(asistenciaRepository, times(1)).save(any(Asistencia.class));

        assertThat(result.message()).isEqualTo("Asistencias registradas correctamente.");
        assertThat(result.data()).isNull();
    }

    @Test
    void registrarAsistencia_asambleaNoExiste_deberiaLanzarError() {
        lenient().when(asambleaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarAsistencia(1L, null))
                .isInstanceOf(ApiException.class)
                .hasMessage("No existe la asamblea.");
    }
}
