package com.condominio;


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
    void testFindByEstado_ShouldReturnPendientes() {

        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        Casa newCasa = new Casa();
        newCasa.setId(1L);
        solicitud.setCasa(newCasa);

        Persona persona = new Persona();
        persona.setPrimerNombre("Carlos");
        persona.setPrimerApellido("Pérez");
        persona.setTelefono(123456789L);

        UserEntity user = new UserEntity();
        user.setEmail("carlos@example.com");
        persona.setUser(user);

        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(1L)).thenReturn(Optional.of(persona));
        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        SuccessResult<List<SolicitudReservaRecursoDTO>> result =
                solicitudReservaRecursoService.findByEstado(EstadoSolicitud.PENDIENTE);

        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;
        assertThat(result.message())
                .contains("Solicitudes " + estado.name().toLowerCase() + " obtenidas correctamente");
    }

    @Test
    void testFindByEstado_ShouldThrow_WhenEmpty() {
        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.APROBADA))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> solicitudReservaRecursoService.findByEstado(EstadoSolicitud.APROBADA))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No hay solicitudes con estado: APROBADA");
    }

    @Test
    void testFindByEstado_ShouldThrow_WhenNoSolicitanteFound() {
        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        Casa newCasa = new Casa();
        newCasa.setId(2L);
        solicitud.setCasa(newCasa);


        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(2L)).thenReturn(Optional.empty());
        when(personaRepository.findPropietarioByCasaId(2L)).thenReturn(Optional.empty());
        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        assertThatThrownBy(() -> solicitudReservaRecursoService.findByEstado(EstadoSolicitud.PENDIENTE))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No se encontro un solicitante");
    }

    @Test
    void testFindByEstado_ShouldReturnAprobadas() {

        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setEstadoSolicitud(EstadoSolicitud.APROBADA);
        Casa newCasa = new Casa();
        newCasa.setId(1L);
        solicitud.setCasa(newCasa);

        Persona persona = new Persona();
        persona.setPrimerNombre("María");
        persona.setPrimerApellido("Gómez");
        persona.setTelefono(987654321L);

        UserEntity user = new UserEntity();
        user.setEmail("maria@example.com");
        persona.setUser(user);

        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.APROBADA))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(1L)).thenReturn(Optional.of(persona));
        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        SuccessResult<List<SolicitudReservaRecursoDTO>> result =
                solicitudReservaRecursoService.findByEstado(EstadoSolicitud.APROBADA);

        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        EstadoSolicitud estado = EstadoSolicitud.APROBADA;
        assertThat(result.message())
                .contains("Solicitudes " + estado.name().toLowerCase() + " obtenidas correctamente");
    }

    @Test
    void testFindByEstado_ShouldReturnRechazadas() {

        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setEstadoSolicitud(EstadoSolicitud.RECHAZADA);
        Casa newCasa = new Casa();
        newCasa.setId(1L);
        solicitud.setCasa(newCasa);

        Persona persona = new Persona();
        persona.setPrimerNombre("Laura");
        persona.setPrimerApellido("Torres");
        persona.setTelefono(321654987L);

        UserEntity user = new UserEntity();
        user.setEmail("laura@example.com");
        persona.setUser(user);

        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.RECHAZADA))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(1L)).thenReturn(Optional.of(persona));
        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        SuccessResult<List<SolicitudReservaRecursoDTO>> result =
                solicitudReservaRecursoService.findByEstado(EstadoSolicitud.RECHAZADA);

        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        EstadoSolicitud estado = EstadoSolicitud.RECHAZADA;
        assertThat(result.message())
                .contains("Solicitudes " + estado.name().toLowerCase() + " obtenidas correctamente");
    }

    @Test
    void testFindByEstado_ShouldThrowException_WhenNoSolicitudes() {
        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of());

        assertThatThrownBy(() -> solicitudReservaRecursoService.findByEstado(EstadoSolicitud.PENDIENTE))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No hay solicitudes con estado: PENDIENTE");
    }
    @Test
    void testFindByEstado_ShouldThrowException_WhenNoSolicitanteFound() {
        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        Casa casa = new Casa();
        casa.setId(1L);
        solicitud.setCasa(casa);

        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(1L)).thenReturn(Optional.empty());
        when(personaRepository.findPropietarioByCasaId(1L)).thenReturn(Optional.empty());
        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        assertThatThrownBy(() -> solicitudReservaRecursoService.findByEstado(EstadoSolicitud.PENDIENTE))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No se encontro un solicitante (arrendatario o propietario)");
    }

    @Test
    void testFindByEstado_ShouldUsePropietario_WhenNoArrendatario() {
        SolicitudReservaRecurso solicitud = new SolicitudReservaRecurso();
        solicitud.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        Casa casa = new Casa();
        casa.setId(1L);
        solicitud.setCasa(casa);

        Persona propietario = new Persona();
        propietario.setPrimerNombre("Ana");
        propietario.setPrimerApellido("Lopez");
        propietario.setTelefono(123456789L);
        UserEntity user = new UserEntity();
        user.setEmail("ana@example.com");
        propietario.setUser(user);

        when(solicitudReservaRecursoRepository.findByEstadoSolicitud(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));
        when(personaRepository.findArrendatarioByCasaId(1L)).thenReturn(Optional.empty());
        when(personaRepository.findPropietarioByCasaId(1L)).thenReturn(Optional.of(propietario));
        when(modelMapper.map(any(SolicitudReservaRecurso.class), eq(SolicitudReservaRecursoDTO.class)))
                .thenReturn(new SolicitudReservaRecursoDTO());

        SuccessResult<List<SolicitudReservaRecursoDTO>> result =
                solicitudReservaRecursoService.findByEstado(EstadoSolicitud.PENDIENTE);

        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        assertThat(result.message()).contains("Solicitudes pendiente");
    }
}
