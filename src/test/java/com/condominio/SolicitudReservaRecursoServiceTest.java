package com.condominio;

import com.condominio.dto.response.PersonaSimpleDTO;
import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.SolicitudReservaRecursoRepository;
import com.condominio.service.implementation.SolicitudReservaRecursoService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudReservaRecursoServiceTest {

    @Mock
    private SolicitudReservaRecursoRepository solicitudReservaRecursoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SolicitudReservaRecursoService solicitudReservaRecursoService;

    @Test
    void testFindPendientes_ShouldThrowException_WhenNoPendingRequests() {
        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> solicitudReservaRecursoService.findPendientes())
                .isInstanceOf(ApiException.class)
                .hasMessage("No hay solicitudes pendientes");

        verify(solicitudReservaRecursoRepository).findByEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        verifyNoInteractions(personaRepository);
    }

    @Test
    void testFindPendientes_ShouldReturnDTOs_WhenArrendatarioExists() {

        Casa newCasa = new Casa();
        newCasa.setId(1L);

        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setCasa(newCasa);
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);

        Persona arrendatario = new Persona();
        arrendatario.setPrimerNombre("Juan");
        arrendatario.setPrimerApellido("Pérez");
        arrendatario.setTelefono(123456789L);
        UserEntity user = new UserEntity();
        user.setEmail("juan@example.com");
        arrendatario.setUser(user);

        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(1L))
                .thenReturn(Optional.of(arrendatario));

        SolicitudReservaRecursoDTO mappedDto = new SolicitudReservaRecursoDTO();
        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(mappedDto);


        SuccessResult<List<SolicitudReservaRecursoDTO>> result = solicitudReservaRecursoService.findPendientes();


        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        assertThat(result.message()).isEqualTo("Solicitudes pendientes obtenidas correctamente");

        PersonaSimpleDTO solicitante = result.data().getFirst().getSolicitante();
        assertThat(solicitante.getNombreCompleto()).isEqualTo("Juan Pérez");
        assertThat(solicitante.getTelefono()).isEqualTo(123456789L);
        assertThat(solicitante.getCorreo()).isEqualTo("juan@example.com");

        verify(personaRepository).findArrendatarioByCasaId(1L);
        verify(personaRepository, never()).findPropietarioByCasaId(anyLong());
    }

    @Test
    void testFindPendientes_ShouldReturnDTOs_WhenOnlyPropietarioExists() {
        Casa newCasa = new Casa();
        newCasa.setId(2L);

        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setCasa(newCasa);
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);

        Persona propietario = new Persona();
        propietario.setPrimerNombre("María");
        propietario.setPrimerApellido("López");
        propietario.setTelefono(987654321L);
        UserEntity user = new UserEntity();
        user.setEmail("maria@example.com");
        propietario.setUser(user);

        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(2L))
                .thenReturn(Optional.empty());
        when(personaRepository.findPropietarioByCasaId(2L))
                .thenReturn(Optional.of(propietario));

        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        SuccessResult<List<SolicitudReservaRecursoDTO>> result = solicitudReservaRecursoService.findPendientes();

        assertThat(result.data()).hasSize(1);
        PersonaSimpleDTO solicitante = result.data().getFirst().getSolicitante();
        assertThat(solicitante.getNombreCompleto()).isEqualTo("María López");
        assertThat(solicitante.getCorreo()).isEqualTo("maria@example.com");

        verify(personaRepository).findArrendatarioByCasaId(2L);
        verify(personaRepository).findPropietarioByCasaId(2L);
    }

    @Test
    void testFindPendientes_ShouldThrowException_WhenNoSolicitanteFound() {
        Casa newCasa = new Casa();
        newCasa.setId(3L);

        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setCasa(newCasa);
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);

        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(3L)).thenReturn(Optional.empty());
        when(personaRepository.findPropietarioByCasaId(3L)).thenReturn(Optional.empty());

        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        assertThatThrownBy(() -> solicitudReservaRecursoService.findPendientes())
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No se encontro un solicitante (arrendatario o propietario)");

        verify(personaRepository).findArrendatarioByCasaId(3L);
        verify(personaRepository).findPropietarioByCasaId(3L);
    }
}
