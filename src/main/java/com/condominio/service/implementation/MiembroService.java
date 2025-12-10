package com.condominio.service.implementation;

import com.condominio.dto.request.MiembroActualizacionDTO;
import com.condominio.dto.request.MiembroRegistroDTO;
import com.condominio.dto.response.MiembrosDTO;
import com.condominio.dto.response.MiembrosDatosDTO;
import com.condominio.dto.response.MiembrosResponseDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.Miembro;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.persistence.repository.MiembroRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.interfaces.IMiembroService;
import com.condominio.util.constants.AppConstants;
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
    private final CasaRepository casaRepository;

    @Override
    public int countByCasaId(Long idCasa) {
        return miembroRepository.countByEstadoAndCasa_Id(true, idCasa);
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

    @Override
    public SuccessResult<Void> crearMiembro(MiembroRegistroDTO miembroRegistroDTO) {
        Casa validarCasa = casaRepository.findById(miembroRegistroDTO.getIdCasa())
                .orElseThrow(() ->  new ApiException(
                        String.format(AppConstants.CASA_NO_EXISTE, miembroRegistroDTO.getIdCasa()),
                        HttpStatus.NOT_FOUND
                ));
        if(miembroRepository.existsByNumeroDocumento(miembroRegistroDTO.getNumeroDocumento())) {
            throw new ApiException("El numero  de documento " +
                    "ya se  encuentra registrado", HttpStatus.BAD_REQUEST);

        }
        if(personaRepository.existsByNumeroDocumento(miembroRegistroDTO.getNumeroDocumento())) {
            throw new ApiException("El numero  de documento " +
                    "ya se  encuentra registrado", HttpStatus.BAD_REQUEST);
        }
        Miembro newMiembro= Miembro.builder()
                .nombre(miembroRegistroDTO.getNombre())
                .numeroDocumento(miembroRegistroDTO.getNumeroDocumento())
                .tipoDocumento(miembroRegistroDTO.getTipoDocumento())
                .telefono(miembroRegistroDTO.getTelefono())
                .parentesco(miembroRegistroDTO.getParentesco())
                .casa(validarCasa)
                .estado(true)
                .build();

        miembroRepository.save(newMiembro);

        return new SuccessResult<>("Miembro registrado correctamente",null);
    }

    public List<MiembrosDatosDTO> listarMiembrosPorCasa(Long casaId) {
        List<Miembro> miembros = miembroRepository.findByCasaId(casaId);

        return miembros.stream()
                .map(miembro -> {
                    MiembrosDatosDTO dto = new MiembrosDatosDTO();
                    dto.setId(miembro.getId());
                    dto.setIdCasa(miembro.getCasa().getId());
                    dto.setNombre(miembro.getNombre());
                    dto.setTipoDocumento(miembro.getTipoDocumento());
                    dto.setNumeroDocumento(miembro.getNumeroDocumento());
                    dto.setTelefono(miembro.getTelefono());
                    dto.setParentesco(miembro.getParentesco());
                    dto.setEstado(miembro.getEstado());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    public SuccessResult<Void> actualizarMiembro(Long idMiembro, MiembroActualizacionDTO dto, Long casaUsuarioId) {
        Miembro miembro = miembroRepository.findById(idMiembro)
                .orElseThrow(() -> new ApiException(
                        String.format(AppConstants.MIEMBRO_NO_EXISTE, idMiembro),
                        HttpStatus.NOT_FOUND
                ));

        if (Boolean.FALSE.equals(miembro.getEstado())) {
            throw new ApiException(
                    "Este miembro está inactivo",
                    HttpStatus.OK
            );
        }

        if (!miembro.getCasa().getId().equals(casaUsuarioId)) {
            throw new ApiException(
                    "No puedes modificar miembros que no pertenezcan a tu casa",
                    HttpStatus.FORBIDDEN
            );
        }
        if(miembroRepository.existsByNumeroDocumentoAndIdNot(dto.getNumeroDocumento(), idMiembro)) {
            throw new ApiException(AppConstants.DOCUMENTO_REPETIDO, HttpStatus.OK);
        }
        if(personaRepository.existsByNumeroDocumento(dto.getNumeroDocumento())){
            throw new ApiException(AppConstants.DOCUMENTO_REPETIDO, HttpStatus.OK);
        }
        miembro.setNombre(dto.getNombre());
        miembro.setTipoDocumento(dto.getTipoDocumento());
        miembro.setNumeroDocumento(dto.getNumeroDocumento());
        miembro.setTelefono(dto.getTelefono());
        miembro.setParentesco(dto.getParentesco());

        miembroRepository.save(miembro);

        return new SuccessResult<>("Miembro actualizado correctamente", null);
    }

    public SuccessResult<Void> actualizarEstadoMiembro(Long idMiembro,Long casaUsuarioId) {
        Miembro miembro = miembroRepository.findById(idMiembro)
                .orElseThrow(() -> new ApiException(
                        String.format(AppConstants.MIEMBRO_NO_EXISTE, idMiembro),
                        HttpStatus.NOT_FOUND
                ));

        if (!miembro.getCasa().getId().equals(casaUsuarioId)) {
            throw new ApiException(
                    "No puedes modificar miembros que no pertenezcan a tu casa",
                    HttpStatus.FORBIDDEN
            );
        }

        miembro.setEstado(!miembro.getEstado());
        miembroRepository.save(miembro);

        String mensaje = miembro.getEstado()
                ? "Miembro habilitado correctamente"
                : "Miembro deshabilitado correctamente";

        return new SuccessResult<>(mensaje, null);
    }


    public SuccessResult<MiembrosResponseDTO> obtenerMiembrosPorCasaConEstado(Long idCasa) {

        List<MiembrosDTO> miembros = new ArrayList<>();


        personaRepository.findPropietarioByCasaId(idCasa)
                .map(p -> convertirPersonaAMiembroDTO(p, "PROPIETARIO"))
                .ifPresent(miembros::add);


        boolean arrendatarioExiste = personaRepository.findArrendatarioByCasaId(idCasa)
                .map(p -> convertirPersonaAMiembroDTO(p, "ARRENDATARIO"))
                .map(p -> {
                    miembros.add(p);
                    return true;
                })
                .orElse(false);


        List<Miembro> miembrosActivos = miembroRepository.findByCasaIdAndEstadoTrue(idCasa);
        boolean miembrosExisten = !miembrosActivos.isEmpty();
        miembros.addAll(
                miembrosActivos.stream()
                        .map(this::convertirMiembroEntidadADTO)
                        .toList()
        );


        MiembrosResponseDTO response = new MiembrosResponseDTO(miembros, arrendatarioExiste, miembrosExisten);
        return new SuccessResult<>("Miembros encontrados", response);
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
                persona.getTipoDocumento(),
                persona.getTelefono(),
                user.getEmail()
        );
    }

    private MiembrosDTO convertirMiembroEntidadADTO(Miembro miembro) {
        return new MiembrosDTO(
                miembro.getNombre(),
                miembro.getParentesco(),
                miembro.getNumeroDocumento(),
                miembro.getTipoDocumento(),
                miembro.getTelefono(),
                null);

    }
}
