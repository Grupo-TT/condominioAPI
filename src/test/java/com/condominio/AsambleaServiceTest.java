package com.condominio;

import com.condominio.dto.request.AsambleaDTO;
import com.condominio.dto.response.AsambleaConAsistenciaDTO;
import com.condominio.dto.response.CasaSimpleDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.AsambleaRepository;
import com.condominio.persistence.repository.AsistenciaRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.implementation.AsambleaService;
import com.condominio.service.implementation.EmailService;
import com.condominio.util.constants.AppConstants;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.condominio.persistence.model.EstadoAsamblea.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsambleaServiceTest {

    @Mock
    private AsambleaRepository asambleaRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private AsistenciaRepository asistenciaRepository;

    private AsambleaService asambleaService;

    @BeforeEach
    void setUp() {
        asambleaService = new AsambleaService(asambleaRepository, modelMapper, personaRepository, emailService, asistenciaRepository);
    }

    @Test
    void create_shouldThrowException_whenFechaAnterior() {
        AsambleaDTO dto = new AsambleaDTO();
        // Fecha de ayer
        dto.setFecha(Date.from(LocalDate.now(AppConstants.ZONE).minusDays(1).atStartOfDay(AppConstants.ZONE).toInstant()));

        ApiException exception = assertThrows(ApiException.class, () -> asambleaService.create(dto));
        assertEquals("Por favor ingresar una fecha valida", exception.getMessage());
    }

    @Test
    void create_shouldSaveAsamblea_andSendEmails() {
        // Fecha válida
        Date fecha = Date.from(LocalDate.now(AppConstants.ZONE).plusDays(1).atStartOfDay(AppConstants.ZONE).toInstant());
        AsambleaDTO dto = new AsambleaDTO();
        dto.setTitulo("Reunión");
        dto.setFecha(fecha);
        dto.setHoraInicio(LocalTime.of(10, 0));

        Asamblea entidad = new Asamblea();
        entidad.setTitulo(dto.getTitulo());
        entidad.setFecha(dto.getFecha());
        entidad.setHoraInicio(dto.getHoraInicio());

        // Mock del ModelMapper
        when(modelMapper.map(dto, Asamblea.class)).thenReturn(entidad);
        // Mock del repo
        when(asambleaRepository.save(entidad)).thenReturn(entidad);

        // Mock personas
        Persona persona = new Persona();
        UserEntity user = new UserEntity ();
        user.setEmail("user@correo.com");
        persona.setUser(user);
        when(personaRepository.findAll()).thenReturn(Collections.singletonList(persona));

        SuccessResult<AsambleaDTO> result = asambleaService.create(dto);


        verify(asambleaRepository).save(entidad);
        verify(emailService).enviarInvitacionesAsambleaMasivas(Collections.singletonList(persona), entidad);
        assertEquals("Asamblea programada correctamente", result.message());
        assertEquals(dto, result.data());
    }

    @Test
    void testCreate_FechaPasada_LanzaApiException() {
        AsambleaDTO dto = new AsambleaDTO();
        dto.setTitulo("Asamblea Pasada");
        dto.setFecha(Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        ApiException thrown = assertThrows(ApiException.class, () -> {
            asambleaService.create(dto);
        });

        assertEquals("Por favor ingresar una fecha valida", thrown.getMessage());
        verifyNoInteractions(asambleaRepository, emailService);
    }


    @Test
    void findAllAsambleas_sinRegistros_deberiaLanzarExcepcion() {
        when(asambleaRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> asambleaService.findAllAsambleas())
                .isInstanceOf(ApiException.class)
                .hasMessage("No se encontraron registros.");
    }

    @Test
    void edit_deberiaActualizarAsamblea() {
        Asamblea existente = new Asamblea();
        existente.setId(1L);

        Date todayInBogota = Date.from(
                LocalDate.now(AppConstants.ZONE)
                        .atStartOfDay(AppConstants.ZONE)
                        .toInstant()
        );

        AsambleaDTO dto = new AsambleaDTO();
        dto.setTitulo("Nueva Asamblea");
        dto.setDescripcion("Desc");
        dto.setLugar("Salon");
        dto.setEstado(PROGRAMADA);
        dto.setFecha(todayInBogota);
        dto.setHoraInicio(LocalTime.now());

        when(asambleaRepository.findById(1L)).thenReturn(Optional.of(existente));

        SuccessResult<AsambleaDTO> result = asambleaService.edit(dto, 1L);

        assertThat(result.data().getTitulo()).isEqualTo("Nueva Asamblea");
        verify(asambleaRepository).save(existente);
    }

    @Test
    void edit_registroNoExiste_deberiaLanzarError() {
        when(asambleaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asambleaService.edit(new AsambleaDTO(), 1L))
                .isInstanceOf(ApiException.class)
                .hasMessage("No se pudo actualizar el registro.");
    }

    @Test
    void delete_deberiaEliminarSinErrores() {
        doNothing().when(asambleaRepository).deleteById(1L);

        SuccessResult<Void> result = asambleaService.delete(1L);

        assertThat(result.message()).isEqualTo("Se eliminó la asamblea satisfactoriamente.");
        verify(asambleaRepository).deleteById(1L);
    }

    @Test
    void getAsambleaById_deberiaRetornarDTO() {
        Asamblea asamblea = new Asamblea();
        asamblea.setId(1L);
        asamblea.setTitulo("Asamblea 1");
        asamblea.setDescripcion("Desc");

        Asistencia p = new Asistencia();
        p.setNombreResponsable("Juan Perez");
        p.setCasa(new Casa());
        p.setEstado(false);
        p.getCasa().setNumeroCasa(101);

        when(asambleaRepository.findById(1L)).thenReturn(Optional.of(asamblea));
        when(asistenciaRepository.findAllByAsamblea(asamblea)).thenReturn(List.of(p));

        SuccessResult<AsambleaConAsistenciaDTO> result = asambleaService.getAsambleaById(1L);

        CasaSimpleDTO propietario = result.data().getPropietarios().get(0);

        assertThat(propietario.getNumeroCasa()).isEqualTo(101);
        assertThat(propietario.getNombrePropietario()).isEqualTo("Juan Perez");

        verify(asambleaRepository).findById(1L);
    }

    @Test
    void getAsambleaById_noExiste_deberiaLanzarExcepcion() {
        when(asambleaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asambleaService.getAsambleaById(1L))
                .isInstanceOf(ApiException.class)
                .hasMessage("No se pudo obtener la información del registro.");
    }

    @Test
    void cambairEstado_ok(){
        Asamblea existente = new Asamblea();
        existente.setId(1L);
        existente.setEstado(PROGRAMADA);

        when(asambleaRepository.findById(1L)).thenReturn(Optional.of(existente));
        SuccessResult<Void> result = asambleaService.cambiarEstado(1L, "REALIZADA");

        assertThat(result.message()).isEqualTo("Se cambió a realizada la asamblea.");
    }

    @Test
    void cambairEstado_ProgramadaACancelada(){
        Asamblea existente = new Asamblea();
        existente.setId(1L);
        existente.setEstado(PROGRAMADA);

        when(asambleaRepository.findById(1L)).thenReturn(Optional.of(existente));
        SuccessResult<Void> result = asambleaService.cambiarEstado(1L, "CANCELADA");

        assertThat(result.message()).isEqualTo("Se cambió el estado de la asamblea.");
    }

    @Test
    void cambairEstado_RealizadaACancelada_deberiaLanzarExcepcion() {
        Asamblea existente = new Asamblea();
        existente.setId(1L);
        existente.setEstado(REALIZADA);

        when(asambleaRepository.findById(1L)).thenReturn(Optional.of(existente));

        ApiException thrown = assertThrows(ApiException.class, () -> {
            asambleaService.cambiarEstado(1L, "CANCELADA");
        });

        assertEquals("No se puede cancelar una asamblea realizada.", thrown.getMessage());
    }

    @Test
    void cambairEstado_CanceladaARealizada_deberiaLanzarExcepcion() {
        Asamblea existente = new Asamblea();
        existente.setId(1L);
        existente.setEstado(CANCELADA);

        when(asambleaRepository.findById(1L)).thenReturn(Optional.of(existente));

        ApiException thrown = assertThrows(ApiException.class, () -> {
            asambleaService.cambiarEstado(1L, "REALIZADA");
        });

        assertEquals("No se puede finalizar una asamblea cancelada.", thrown.getMessage());
    }


}
