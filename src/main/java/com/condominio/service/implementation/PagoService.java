package com.condominio.service.implementation;

import com.condominio.dto.response.ObligacionDTO;
import com.condominio.dto.response.PagoDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.*;
import com.condominio.service.interfaces.IPagoService;
import com.condominio.util.events.CreatedPagoEvent;
import com.condominio.util.exception.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PagoService implements IPagoService {

    private final PagoRepository pagoRepository;
    private final PagoDetalleRepository pagoDetalleRepository;
    private final PersonaRepository personaRepository;
    private final ObligacionRepository obligacionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public SuccessResult<ObligacionDTO> registrarPago(PagoDTO pagoDTO) {
        Casa casa = null;
        Persona propietario = null;
        Obligacion obligacion = null;
        ObligacionDTO obligacionDTO = null;

        Optional<Obligacion> obligacionOptional = obligacionRepository.findById(pagoDTO.getIdObligacion());
        if (obligacionOptional.isPresent()) {
            obligacion = obligacionOptional.get();
            casa = obligacion.getCasa();
            propietario = personaRepository.findPropietarioByCasaId(casa.getId()).
                    orElse(null);
            if ((pagoDTO.getMontoAPagar() > obligacion.getMonto()) && (obligacion.getEstadoPago() == EstadoPago.PENDIENTE) ) {
                throw new ApiException("El valor ingresado supera la deuda actual.", HttpStatus.BAD_REQUEST);
            }else if(pagoDTO.getMontoAPagar() == obligacion.getMonto()) {
                obligacion.setEstadoPago(EstadoPago.CONDONADO);
                Pago pago = Pago.builder()
                        .fechaPago(LocalDate.now())
                        .total(pagoDTO.getMontoAPagar())
                        .build();
                pagoRepository.save(pago);
                pagoDetalleRepository.save(PagoDetalle.builder()
                                .montoPagado(pagoDTO.getMontoAPagar())
                                .obligacion(obligacion)
                                .pago(pago)
                                .build());
                obligacionRepository.save(obligacion);
                obligacionDTO = ObligacionDTO.builder()
                        .id(obligacion.getId())
                        .casa(casa.getNumeroCasa())
                        .monto(obligacion.getMonto())
                        .motivo(obligacion.getMotivo())
                        .estado(String.valueOf(EstadoPago.CONDONADO))
                        .fechaPago(LocalDate.now())
                        .build();

                applicationEventPublisher.publishEvent(new CreatedPagoEvent(propietario.getUser().getEmail(), obligacionDTO));

            }else{
                throw new ApiException("El valor ingresado es menor a la deuda actual.", HttpStatus.BAD_REQUEST);
            }

        }
        return new SuccessResult<>("Pago realizado correctamente", obligacionDTO);
    }

    public Optional<LocalDate> obtenerFechaUltimoPagoPorCasa(Long idCasa) {
        return pagoDetalleRepository.findFechaUltimoPagoByCasaId(idCasa);
    }
}
