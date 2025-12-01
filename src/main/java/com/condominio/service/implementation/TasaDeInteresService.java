package com.condominio.service.implementation;


import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.ActualizacionHelper;
import com.condominio.persistence.model.TasaDeInteres;
import com.condominio.persistence.repository.TasaDeInteresRepository;
import com.condominio.service.interfaces.ITasaDeInteres;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TasaDeInteresService implements ITasaDeInteres {


    private final TasaDeInteresRepository tasaDeInteresRepository;
    private final ActualizacionHelper actualizacionHelper;

    public void save(TasaDeInteres tasaDeInteres) {
        tasaDeInteresRepository.save(tasaDeInteres);
    }


    public SuccessResult<TasaDeInteres> actualizarTasaDeInteres(double nuevoValor) {

        if(nuevoValor >= 0 && nuevoValor <= 100){

            TasaDeInteres tasaDeInteres = tasaDeInteresRepository.findById(1L)
                    .orElseThrow(() -> new ApiException
                            ("No existe una tasa de interes", HttpStatus.NOT_FOUND));

            tasaDeInteres  = actualizacionHelper.aplicarDatosComunes(tasaDeInteres, nuevoValor,true);

            TasaDeInteres tasaGuardada = tasaDeInteresRepository.save(tasaDeInteres);
            return new SuccessResult<>("Tasa de interes actualizada correctamente", tasaGuardada);

        }else{
            throw new ApiException("Ingrese un nuevo valor válido"
                    , HttpStatus.BAD_REQUEST);
        }
    }
}
