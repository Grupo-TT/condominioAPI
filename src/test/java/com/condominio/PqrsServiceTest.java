package com.condominio;

import com.condominio.dto.request.PqrsUpdateDTO;
import com.condominio.dto.response.PqrsDTO;
import com.condominio.dto.response.PqrsPropiDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.PqrsRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.implementation.PqrsService;
import com.condominio.util.exception.ApiException;
import com.condominio.util.helper.PersonaHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PqrsServiceTest {

    @Mock
    private PqrsRepository pqrsRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PersonaHelper personaHelper;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private PqrsService service;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        Mockito.reset(pqrsRepository, modelMapper, personaHelper, personaRepository);
    }

    @Test
    void findByEstado_shouldReturnDtosWithSolicitante_whenThereArePqrs() {
        EstadoPqrs estado = EstadoPqrs.PENDIENTE;
        Long casaId = 1L;
        Casa casa = new Casa(); casa.setId(casaId);

        PqrsEntity entity = new PqrsEntity();
        entity.setId(10L);
        entity.setCasa(casa);

        PqrsDTO dto = new PqrsDTO();

        Persona persona = new Persona();
        persona.setPrimerNombre("Ana");
        persona.setPrimerApellido("Lopez");
        UserEntity user = new UserEntity(); user.setEmail("ana@example.com");
        persona.setUser(user);

        when(pqrsRepository.findByEstadoPqrs(estado)).thenReturn(List.of(entity));
        when(modelMapper.map(entity, PqrsDTO.class)).thenReturn(dto);
        when(personaHelper.obtenerSolicitantePorCasa(casaId)).thenReturn(persona);
        when(personaHelper.toPersonaSimpleDTO(persona)).thenReturn(
                com.condominio.dto.response.PersonaSimpleDTO.builder()
                        .nombreCompleto("Ana Lopez").correo("ana@example.com").telefono(null).build()
        );

        SuccessResult<List<PqrsDTO>> res = service.findByEstado(estado);

        assertThat(res).isNotNull();
        assertThat(res.message()).contains("PQRS " + estado.name().toLowerCase() + " obtenidas correctamente");
        assertThat(res.data()).hasSize(1);

        verify(pqrsRepository).findByEstadoPqrs(estado);
        verify(modelMapper).map(entity, PqrsDTO.class);
        verify(personaHelper).obtenerSolicitantePorCasa(casaId);
        verify(personaHelper).toPersonaSimpleDTO(persona);
    }

    @Test
    void findByEstado_shouldThrow_whenEmpty() {
        EstadoPqrs estado = EstadoPqrs.REVISADA;
        when(pqrsRepository.findByEstadoPqrs(estado)).thenReturn(Collections.emptyList());

        ApiException ex = assertThrows(ApiException.class, () -> service.findByEstado(estado));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(ex.getMessage()).contains("No hay PQRS con estado: " + estado);

        verify(pqrsRepository).findByEstadoPqrs(estado);
        verifyNoInteractions(modelMapper, personaHelper);
    }

    @Test
    void update_shouldUpdateAndReturnDto_whenValid() {
        Long id = 5L;
        Casa casa = new Casa(); casa.setId(7L);
        PqrsEntity existing = new PqrsEntity();
        existing.setId(id);
        existing.setCasa(casa);

        // PqrsUpdateDTO is the new DTO used by update(...)
        PqrsUpdateDTO dto = new PqrsUpdateDTO();
        dto.setFechaRealizacion(LocalDate.now()); // valid (not future)
        dto.setTitulo("Nuevo titulo");
        dto.setDescripcion("Desc");
        dto.setTipoPqrs(TipoPqrs.PETICION);
        dto.setEstadoPqrs(EstadoPqrs.REVISADA);

        PqrsEntity saved = new PqrsEntity();
        saved.setId(id);

        PqrsUpdateDTO mappedDto = new PqrsUpdateDTO();

        when(pqrsRepository.findById(id)).thenReturn(Optional.of(existing));
        when(pqrsRepository.save(existing)).thenReturn(saved);
        when(modelMapper.map(saved, PqrsUpdateDTO.class)).thenReturn(mappedDto);

        SuccessResult<PqrsUpdateDTO> res = service.update(id, dto);

        assertThat(res).isNotNull();
        assertThat(res.message()).isEqualTo("PQRS modificada exitosamente");
        assertThat(res.data()).isEqualTo(mappedDto);

        verify(pqrsRepository).findById(id);
        verify(pqrsRepository).save(existing);
        verify(modelMapper).map(saved, PqrsUpdateDTO.class);
    }

    @Test
    void update_shouldThrowBadRequest_whenFechaRealizacionIsAfterToday() {
        Long id = 6L;
        PqrsEntity existing = new PqrsEntity();
        existing.setId(id);
        Casa casa = new Casa(); casa.setId(2L);
        existing.setCasa(casa);

        PqrsUpdateDTO dto = new PqrsUpdateDTO();
        dto.setFechaRealizacion(LocalDate.now().plusDays(2)); // invalid (future)

        when(pqrsRepository.findById(id)).thenReturn(Optional.of(existing));

        ApiException ex = assertThrows(ApiException.class, () -> service.update(id, dto));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("Por favor, ingresa una fecha y hora validas");

        verify(pqrsRepository).findById(id);
        verify(pqrsRepository, never()).save(any());
    }

    @Test
    void marcarRevisada_shouldMarkAndReturnDto_whenNotAlreadyReviewed() {
        Long id = 7L;
        Casa casa = new Casa(); casa.setId(3L);
        PqrsEntity entity = new PqrsEntity();
        entity.setId(id);
        entity.setCasa(casa);
        entity.setEstadoPqrs(EstadoPqrs.PENDIENTE);

        PqrsEntity saved = new PqrsEntity();
        saved.setId(id);

        PqrsDTO mapped = new PqrsDTO();

        when(pqrsRepository.findById(id)).thenReturn(Optional.of(entity));
        when(personaHelper.obtenerSolicitantePorCasa(casa.getId())).thenReturn(new Persona());
        when(pqrsRepository.save(entity)).thenReturn(saved);
        when(modelMapper.map(saved, PqrsDTO.class)).thenReturn(mapped);
        when(personaHelper.toPersonaSimpleDTO(any())).thenReturn(
                com.condominio.dto.response.PersonaSimpleDTO.builder().nombreCompleto("X").correo("x@x.com").build()
        );

        SuccessResult<PqrsDTO> res = service.marcarRevisada(id);

        assertThat(res).isNotNull();
        assertThat(res.message()).isEqualTo("PQRS marcada como revisada exitosamente");
        assertThat(res.data()).isEqualTo(mapped);

        verify(pqrsRepository).findById(id);
        verify(pqrsRepository).save(entity);
        verify(personaHelper).obtenerSolicitantePorCasa(casa.getId());
    }

    @Test
    void marcarRevisada_shouldThrowBadRequest_whenAlreadyRevisada() {
        Long id = 8L;
        PqrsEntity entity = new PqrsEntity();
        entity.setId(id);
        entity.setEstadoPqrs(EstadoPqrs.REVISADA);
        Casa casa = new Casa(); casa.setId(4L);
        entity.setCasa(casa);

        when(pqrsRepository.findById(id)).thenReturn(Optional.of(entity));
        when(personaHelper.obtenerSolicitantePorCasa(casa.getId())).thenReturn(new Persona());

        ApiException ex = assertThrows(ApiException.class, () -> service.marcarRevisada(id));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("La PQRS ya esta marcada como revisada revisada");

        verify(pqrsRepository).findById(id);
        verify(pqrsRepository, never()).save(any());
        verify(personaHelper).obtenerSolicitantePorCasa(casa.getId());
    }

//    @Test
//    void eliminar_shouldDeleteAndReturnDto_whenOwnerAndNotRevisada() {
//        // Arrange
//        String username = "owner@example.com";
//        SecurityContextHolder.getContext().setAuthentication(
//                new UsernamePasswordAuthenticationToken(username, null, null)
//        );
//
//        Long id = 20L;
//        Casa casa = new Casa(); casa.setId(50L);
//        PqrsEntity entity = new PqrsEntity();
//        entity.setId(id);
//        entity.setCasa(casa);
//        entity.setEstadoPqrs(EstadoPqrs.PENDIENTE);
//
//        Persona persona = new Persona();
//        persona.setCasa(casa);
//
//        PqrsDTO mapped = new PqrsDTO();
//
//        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(persona));
//        when(pqrsRepository.findById(id)).thenReturn(Optional.of(entity));
//        when(modelMapper.map(entity, PqrsDTO.class)).thenReturn(mapped);
//
//        // Use doReturn to avoid strict-stubbing issues
//        doReturn(persona).when(personaHelper).obtenerSolicitantePorCasa(eq(casa.getId()));
//        doReturn(com.condominio.dto.response.PersonaSimpleDTO.builder()
//                .nombreCompleto("Owner")
//                .correo("o@o.com")
//                .telefono(null)
//                .build())
//                .when(personaHelper).toPersonaSimpleDTO(persona);
//
//        // Act
//        SuccessResult<PqrsDTO> res = service.eliminar(id);
//
//        // Assert
//        assertThat(res).isNotNull();
//        assertThat(res.message()).isEqualTo("PQRS eliminada exitosamente");
//        assertThat(res.data()).isEqualTo(mapped);
//
//        verify(pqrsRepository).findById(id);
//        verify(modelMapper).map(entity, PqrsDTO.class);
//        verify(personaHelper).obtenerSolicitantePorCasa(casa.getId());
//        verify(personaHelper).toPersonaSimpleDTO(persona);
//        verify(pqrsRepository).delete(entity);
//    }

    @Test
    void eliminar_shouldThrowBadRequest_whenAlreadyRevisada() {
        String username = "owner2@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Long id = 21L;
        Casa casa = new Casa(); casa.setId(51L);
        PqrsEntity entity = new PqrsEntity();
        entity.setId(id);
        entity.setCasa(casa);
        entity.setEstadoPqrs(EstadoPqrs.REVISADA);

        Persona persona = new Persona();
        persona.setCasa(casa);

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(persona));
        when(pqrsRepository.findById(id)).thenReturn(Optional.of(entity));
        when(modelMapper.map(entity, PqrsDTO.class)).thenReturn(new PqrsDTO());
        when(personaHelper.obtenerSolicitantePorCasa(casa.getId())).thenReturn(persona);

        ApiException ex = assertThrows(ApiException.class, () -> service.eliminar(id));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("No se puede eliminar una PQRS que ya está marcada como revisada");

        verify(pqrsRepository, never()).delete(any());
    }

    @Test
    void crearPqrs_shouldSaveWithAuthenticatedUserCasa() {
        String username = "user@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Persona persona = new Persona();
        Casa casa = new Casa(); casa.setId(77L);
        persona.setCasa(casa);

        PqrsPropiDTO dto = new PqrsPropiDTO();
        dto.setTitulo("Título");
        dto.setDescripcion("Desc");
        dto.setTipoPqrs(TipoPqrs.PETICION);

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(persona));
        when(pqrsRepository.save(any(PqrsEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        SuccessResult<PqrsPropiDTO> res = service.crearPqrs(dto);

        assertThat(res).isNotNull();
        assertThat(res.message()).contains("PQRS creado exitosamente");
        assertThat(res.data()).isEqualTo(dto);

        verify(pqrsRepository).save(any(PqrsEntity.class));
    }

    @Test
    void crearPqrs_shouldThrowNotFound_whenUserMissing() {
        String username = "nouser@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        PqrsPropiDTO dto = new PqrsPropiDTO();
        dto.setTitulo("T");
        dto.setDescripcion("D");
        dto.setTipoPqrs(TipoPqrs.QUEJA);

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.crearPqrs(dto));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("Usuario no encontrado");

        verify(pqrsRepository, never()).save(any());
    }

    @Test
    void modificarPqrs_shouldModify_whenOwnerAndNotRevisada() {
        String username = "ownerMod@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Long id = 30L;
        Casa casa = new Casa(); casa.setId(99L);
        PqrsEntity existing = new PqrsEntity();
        existing.setId(id);
        existing.setCasa(casa);
        existing.setEstadoPqrs(EstadoPqrs.PENDIENTE);

        Persona persona = new Persona();
        persona.setCasa(casa);

        PqrsPropiDTO dto = new PqrsPropiDTO();
        dto.setTitulo("T new");
        dto.setDescripcion("D new");
        dto.setTipoPqrs(TipoPqrs.QUEJA);

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(persona));
        when(pqrsRepository.findById(id)).thenReturn(Optional.of(existing));
        when(pqrsRepository.save(existing)).thenReturn(existing);

        SuccessResult<PqrsPropiDTO> res = service.modificarPqrs(id, dto);

        assertThat(res).isNotNull();
        assertThat(res.message()).isEqualTo("PQRS modificado exitosamente");
        assertThat(res.data()).isEqualTo(dto);

        verify(pqrsRepository).save(existing);
    }

    @Test
    void modificarPqrs_shouldThrowBadRequest_whenAlreadyRevisada() {
        String username = "ownerMod2@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Long id = 31L;
        PqrsEntity existing = new PqrsEntity();
        existing.setId(id);
        existing.setEstadoPqrs(EstadoPqrs.REVISADA);
        Casa casa = new Casa(); casa.setId(12L);
        existing.setCasa(casa);

        Persona persona = new Persona();
        persona.setCasa(casa);

        PqrsPropiDTO dto = new PqrsPropiDTO();
        dto.setTitulo("T"); dto.setDescripcion("D"); dto.setTipoPqrs(TipoPqrs.SUGERENCIA);

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(persona));
        when(pqrsRepository.findById(id)).thenReturn(Optional.of(existing));

        ApiException ex = assertThrows(ApiException.class, () -> service.modificarPqrs(id, dto));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("No se puede modificar una PQRS que ya está marcarda como revisada");

        verify(pqrsRepository, never()).save(any());
    }

    @Test
    void verificarUsuarioAndPqrs_shouldThrowForbidden_whenUserNotOwner() {
        String username = "intruder@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        Long id = 40L;
        Casa casa = new Casa(); casa.setId(200L);
        PqrsEntity entity = new PqrsEntity();
        entity.setId(id);
        entity.setCasa(casa);

        Persona personaOther = new Persona();
        Casa otraCasa = new Casa(); otraCasa.setId(999L);
        personaOther.setCasa(otraCasa);

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(personaOther));
        when(pqrsRepository.findById(id)).thenReturn(Optional.of(entity));

        ApiException ex = assertThrows(ApiException.class, () -> service.verificarUsuarioAndPqrs(id));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getMessage()).contains("No autorizado");
    }

    @Test
    void verificarUsuarioAndPqrs_shouldThrowNotFound_whenPqrsMissing() {
        String username = "ownerX@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, null)
        );

        when(personaRepository.findByUserEmail(username)).thenReturn(Optional.of(new Persona()));
        Long id = 9999L;
        when(pqrsRepository.findById(id)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.verificarUsuarioAndPqrs(id));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("No se ha encontrado la PQRS");
    }
}