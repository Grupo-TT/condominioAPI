package com.condominio.service.implementation;

import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.CargoAdministracion;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.CargoAdministracionRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.UserRepository;
import com.condominio.service.interfaces.ICargoAdministracion;
import com.condominio.util.constants.AppConstants;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CargoAdministracionService implements ICargoAdministracion {

    private final CargoAdministracionRepository cargoAdministracionRepository;
    private final PersonaRepository personaRepository;
    private final UserRepository userRepository;

    public  void save(CargoAdministracion cargoAdministracion) {
        cargoAdministracionRepository.save(cargoAdministracion);
    }


    public SuccessResult<CargoAdministracion> actualizarCargoAdministracion(double nuevoValor) {

        if(nuevoValor <= 0){
            throw new ApiException("El nuevo valor no puede ser tan bajo "
                    , HttpStatus.BAD_REQUEST);
        }

        CargoAdministracion cargo = cargoAdministracionRepository.findById(1L)
                .orElseThrow(() -> new ApiException("No existe el cargo de administración", HttpStatus.NOT_FOUND));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity userEntity = userRepository.findUserEntityByEmail(username);


        Persona persona = personaRepository.findPersonaByUser(userEntity);


        cargo.setValorActual(cargo.getNuevoValor());

        cargo.setNuevoValor(nuevoValor);

        cargo.setCorreoActualizador(username);

        cargo.setNombreActualizador(persona.getNombreCompleto());

        cargo.setFechaAplicacion(OffsetDateTime.now(AppConstants.ZONE));

        CargoAdministracion guardado = cargoAdministracionRepository.save(cargo);

        return new SuccessResult<>("Cargo de administración actualizado correctamente", guardado);
    }
}
