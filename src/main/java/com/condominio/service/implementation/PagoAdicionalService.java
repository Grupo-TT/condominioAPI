package com.condominio.service.implementation;

import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.PagoAdicionalRepository;
import com.condominio.service.interfaces.IPagoAdicional;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static com.condominio.util.constants.AppConstants.MAX_PAGO_ADICIONAL;


@Service
@RequiredArgsConstructor
public class PagoAdicionalService implements IPagoAdicional {

    private final PagoAdicionalRepository pagoAdicionalRepository;
    private final ActualizacionHelper   actualizacionHelper;



    public void save(PagoAdicional pagoAdicional) {
        pagoAdicionalRepository.save(pagoAdicional);

    }

    public SuccessResult<PagoAdicional> actualizarPagoAdicional(double nuevoValor) {

        if(nuevoValor > 0 && nuevoValor <= MAX_PAGO_ADICIONAL){

            PagoAdicional pagoAdicional = pagoAdicionalRepository.findById(1L)
                    .orElseThrow(() -> new ApiException
                            ("No existe un pago adicional registrado", HttpStatus.NOT_FOUND));

            pagoAdicional = actualizacionHelper.aplicarDatosComunes(pagoAdicional, nuevoValor);

            PagoAdicional newPagoAdicional = pagoAdicionalRepository.save(pagoAdicional);
            return new SuccessResult<>("Pago adicional actualizado correctamente", newPagoAdicional);

        }else{
            throw new ApiException("Ingrese un nuevo valor válido"
                    , HttpStatus.BAD_REQUEST);
        }
    }
}



