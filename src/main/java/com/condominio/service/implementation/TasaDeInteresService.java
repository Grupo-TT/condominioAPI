package com.condominio.service.implementation;


import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.TasaDeInteres;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.TasaDeInteresRepository;
import com.condominio.persistence.repository.UserRepository;
import com.condominio.service.interfaces.ITasaDeInteres;
import com.condominio.util.constants.AppConstants;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TasaDeInteresService implements ITasaDeInteres {


    private final TasaDeInteresRepository tasaDeInteresRepository;
    private final PersonaRepository personaRepository;
    private final UserRepository userRepository;

    public void save(TasaDeInteres tasaDeInteres) {
        tasaDeInteresRepository.save(tasaDeInteres);
    }


    public SuccessResult<TasaDeInteres> actualizarTasaDeInteres(double nuevoValor) {

        if(nuevoValor >= 0 && nuevoValor <= 100){

            TasaDeInteres tasaDeInteres = tasaDeInteresRepository.findById(1L)
                    .orElseThrow(() -> new ApiException
                            ("No existe una tasa de interes", HttpStatus.NOT_FOUND));

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            UserEntity userEntity = userRepository.findUserEntityByEmail(username);


            Persona persona = personaRepository.findPersonaByUser(userEntity);

            tasaDeInteres.setValorActual(tasaDeInteres.getNuevoValor());
            tasaDeInteres.setNuevoValor(nuevoValor);
            tasaDeInteres.setCorreoActualizador(username);
            tasaDeInteres.setNombreActualizador(persona.getNombreCompleto());
            tasaDeInteres.setFechaAplicacion(OffsetDateTime.now(AppConstants.ZONE));

            TasaDeInteres tasaGuardada = tasaDeInteresRepository.save(tasaDeInteres);
            return new SuccessResult<>("Cargo de administración actualizado correctamente", tasaGuardada);

        }else{
            throw new ApiException("Ingrese un nuevo valor válido"
                    , HttpStatus.BAD_REQUEST);
        }
    }
}
