package com.condominio.service.implementation;

import com.condominio.dto.response.ReservaDTO;
import com.condominio.persistence.model.Reserva;
import com.condominio.persistence.repository.ReservaRepository;
import com.condominio.service.interfaces.IReservaService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService implements IReservaService {

    private final ReservaRepository reservaRepository;

    @Override
    public List<ReservaDTO> findAllProximas() {
        List<Reserva> reservasProximas = reservaRepository.findBySolicitudReservaRecurso_FechaSolicitudGreaterThanEqual(LocalDate.now());

        if (reservasProximas.isEmpty()) {
            throw new ApiException("No hay reservas de los recursos comunes.", HttpStatus.NOT_FOUND);
        }

        return  reservasProximas.stream().map(reserva -> ReservaDTO.builder()
                .fechaReserva(reserva.getSolicitudReservaRecurso().getFechaSolicitud())
                .nombreRecurso(reserva.getSolicitudReservaRecurso().getRecursoComun().getNombre())
                .build())
                .toList();
    }
}
