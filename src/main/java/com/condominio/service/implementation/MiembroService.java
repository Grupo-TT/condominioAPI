package com.condominio.service.implementation;

import com.condominio.dto.response.MiembrosDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Miembro;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.MiembroRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.interfaces.IMiembroService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MiembroService implements IMiembroService {

    private final MiembroRepository miembroRepository;
    private final PersonaRepository personaRepository;
    @Override
    public int countByCasaId(Long IdCasa) {
        return miembroRepository.countByCasaId(IdCasa);
    }

    @Override
    public SuccessResult<List<MiembrosDTO>> obtenerMiembrosPorCasa(Long idCasa) {

        List<MiembrosDTO> miembros = new ArrayList<>();
        personaRepository.findPropietarioByCasaId(idCasa)
                .map(p -> convertirPersonaAMiembroDTO(p, "PROPIETARIO"))
                .ifPresent(miembros::add);



        personaRepository.findArrendatarioByCasaId(idCasa)
                .map(p -> convertirPersonaAMiembroDTO(p, "ARRENDATARIO"))
                .ifPresent(miembros::add);


        List<Miembro> miembrosActivos = miembroRepository.findByCasaIdAndEstadoTrue(idCasa);
        miembros.addAll(
                miembrosActivos.stream()
                        .map(this::convertirMiembroEntidadADTO)
                        .toList()
        );
        if (miembros.isEmpty()) {
            throw new ApiException("No hay miembros registrados para esta casa", HttpStatus.NOT_FOUND);
        }

        return new SuccessResult<>("Miembros encontrados", miembros);
    }
    private MiembrosDTO convertirPersonaAMiembroDTO(Persona persona, String tipoMiembro) {
        UserEntity user = persona.getUser();

        String nombreCompleto = Stream.of(
                        persona.getPrimerNombre(),
                        persona.getSegundoNombre(),
                        persona.getPrimerApellido(),
                        persona.getSegundoApellido()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        return new MiembrosDTO(
                nombreCompleto,
                tipoMiembro,
                persona.getNumeroDocumento(),
                persona.getTelefono(),
                user.getEmail()
        );
    }

    private MiembrosDTO convertirMiembroEntidadADTO(Miembro miembro) {
        return new MiembrosDTO(
                miembro.getNombre(),
                miembro.getParentesco(),
                miembro.getNumeroDocumento(),
                miembro.getTelefono(),
                null);

    }
}
