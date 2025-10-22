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
import java.util.Objects;
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

            int montoDeuda = obligacion.getMonto() - Objects.requireNonNullElse(obligacion.getMontoPagado(), 0);

            if ((pagoDTO.getMontoAPagar() > montoDeuda) && (obligacion.getEstadoPago() != EstadoPago.CONDONADO) ) {
                throw new ApiException("El valor ingresado supera la deuda actual.", HttpStatus.BAD_REQUEST);
            }else if(pagoDTO.getMontoAPagar() == montoDeuda) {
                obligacion.setEstadoPago(EstadoPago.CONDONADO);
                obligacion.setMontoPagado(obligacion.getMonto());
                obligacionDTO = realizarPago(pagoDTO, obligacion, casa);
                applicationEventPublisher.publishEvent(new CreatedPagoEvent(propietario.getUser().getEmail(), obligacionDTO));

            }else{
                obligacion.setEstadoPago(EstadoPago.POR_COBRAR);
                obligacion.setMontoPagado(pagoDTO.getMontoAPagar());
                obligacionDTO = realizarPago(pagoDTO, obligacion, casa);
                applicationEventPublisher.publishEvent(new CreatedPagoEvent(propietario.getUser().getEmail(), obligacionDTO));
            }
        }
        return new SuccessResult<>("Pago realizado correctamente", obligacionDTO);
    }

    public ObligacionDTO realizarPago(PagoDTO pagoDTO , Obligacion obligacion, Casa casa) {
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
        int saldoPendiente = obligacion.getMonto() - obligacion.getMontoPagado();

        return ObligacionDTO.builder()
                .id(obligacion.getId())
                .casa(casa.getNumeroCasa())
                .monto(pagoDTO.getMontoAPagar())
                .saldo(saldoPendiente)
                .motivo(obligacion.getMotivo())
                .estado(String.valueOf(obligacion.getEstadoPago()))
                .fechaPago(LocalDate.now())
                .build();
    }

}
