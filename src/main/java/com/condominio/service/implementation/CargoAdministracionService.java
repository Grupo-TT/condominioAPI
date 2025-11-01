package com.condominio.service.implementation;

import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.ActualizacionHelper;
import com.condominio.persistence.model.CargoAdministracion;
import com.condominio.persistence.repository.CargoAdministracionRepository;
import com.condominio.service.interfaces.ICargoAdministracion;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CargoAdministracionService implements ICargoAdministracion {

    private final CargoAdministracionRepository cargoAdministracionRepository;
    private final ActualizacionHelper actualizacionHelper;

    public  void save(CargoAdministracion cargoAdministracion) {
        cargoAdministracionRepository.save(cargoAdministracion);
    }


    public SuccessResult<CargoAdministracion> actualizarCargoAdministracion(double nuevoValor) {

        CargoAdministracion cargo = cargoAdministracionRepository.findById(1L)
                .orElseThrow(() -> new ApiException("No existe el cargo de administración", HttpStatus.NOT_FOUND));

        if(nuevoValor <= 0){
            throw new ApiException("El nuevo valor no puede ser tan bajo "
                    , HttpStatus.BAD_REQUEST);
        }

        cargo = actualizacionHelper.aplicarDatosComunes(cargo , nuevoValor);

        CargoAdministracion guardado = cargoAdministracionRepository.save(cargo);

        return new SuccessResult<>("Cargo de administración actualizado correctamente", guardado);
    }
}
