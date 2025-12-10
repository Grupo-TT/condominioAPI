package com.condominio;

import com.condominio.dto.response.*;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.*;
import com.condominio.service.implementation.DashboardPropiService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardPropiServiceTest {

    @Mock
    private PersonaRepository personaRepository;
    @Mock
    private MiembroRepository miembroRepository;
    @Mock
    private MascotaRepository mascotaRepository;
    @Mock
    private ObligacionRepository obligacionRepository;
    @Mock
    private PagoDetalleRepository pagoDetalleRepository;
    @Mock
    private SolicitudReservaRecursoRepository solicitudRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private DashboardPropiService service;

    private Persona persona;
    private Casa casa;

    @BeforeAll
    static void beforeAll() {
        // Hacemos que el SecurityContext sea heredable por hilos hijos (útil en CI)
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @BeforeEach
    void setup() {
        // datos base
        casa = Casa.builder()
                .id(11L)
                .numeroCasa(101)
                .build();

        UserEntity user = new UserEntity();
        user.setEmail("propietario@example.com");

        persona = new Persona();
        persona.setId(1L);
        persona.setPrimerNombre("Juan");
        persona.setPrimerApellido("Perez");
        persona.setUser(user);
        persona.setCasa(casa);

        // establecer autenticación por defecto para cada test
        TestSecurityUtil.setAuthenticationWithUsername("propietario@example.com");
    }

    @AfterEach
    void tearDown() {
        // limpiar contexto para no contaminar otros tests
        TestSecurityUtil.clearAuth();
        // resetear mocks (opcional, MockitoExtension normalmente lo maneja)
        clearInvocations(personaRepository, miembroRepository, mascotaRepository, obligacionRepository, pagoDetalleRepository, solicitudRepository, modelMapper);
    }

    // ---------- getPropiBasicInfo tests ----------

    @Test
    void getPropiBasicInfo_success_residencial() {
        // arrange
        when(personaRepository.findByUserEmail("propietario@example.com")).thenReturn(Optional.of(persona));
        when(personaRepository.findArrendatarioByCasaId(casa.getId())).thenReturn(Optional.empty());
        when(miembroRepository.countByCasaId(casa.getId())).thenReturn(3);
        when(mascotaRepository.countByCasaId(casa.getId())).thenReturn(2);

        // act
        SuccessResult<InfoCasaPropiDTO> result = service.getPropiBasicInfo();

        // assert
        assertNotNull(result);
        assertEquals("Info del Propietario Obtenida", result.message());
        InfoCasaPropiDTO dto = result.data();
        assertNotNull(dto);
        assertEquals(101, dto.getNumeroCasa());
        assertEquals(UsoCasa.RESIDENCIAL, dto.getTipoUso());
        assertEquals(3, dto.getCantidadMiembros());
        assertEquals(2, dto.getCantidadMascotas());

        verify(personaRepository).findByUserEmail("propietario@example.com");
        verify(miembroRepository).countByCasaId(casa.getId());
        verify(mascotaRepository).countByCasaId(casa.getId());
    }

    @Test
    void getPropiBasicInfo_success_arrendada() {
        // arrange
        when(personaRepository.findByUserEmail("propietario@example.com")).thenReturn(Optional.of(persona));
        when(personaRepository.findArrendatarioByCasaId(casa.getId()))
                .thenReturn(Optional.of(new Persona())); // si hay arrendatario -> ARRENDADA
        when(miembroRepository.countByCasaId(casa.getId())).thenReturn(1);
        when(mascotaRepository.countByCasaId(casa.getId())).thenReturn(0);

        // act
        SuccessResult<InfoCasaPropiDTO> result = service.getPropiBasicInfo();

        // assert
        InfoCasaPropiDTO dto = result.data();
        assertEquals(UsoCasa.ARRENDADA, dto.getTipoUso());
    }

    @Test
    void getPropiBasicInfo_noPersona_throws() {
        // arrange
        when(personaRepository.findByUserEmail("propietario@example.com")).thenReturn(Optional.empty());

        // act & assert
        ApiException ex = assertThrows(ApiException.class, () -> service.getPropiBasicInfo());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void getPropiBasicInfo_noCasa_throws() {
        // persona sin casa
        persona.setCasa(null);
        when(personaRepository.findByUserEmail("propietario@example.com")).thenReturn(Optional.of(persona));

        ApiException ex = assertThrows(ApiException.class, () -> service.getPropiBasicInfo());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    // ---------- getAccountStatus tests ----------

    @Test
    void getAccountStatus_withLastPayment() {
        // arrange
        when(personaRepository.findByUserEmail("propietario@example.com")).thenReturn(Optional.of(persona));

        // obligations: one with pending value 200_000
        Obligacion o1 = Obligacion.builder()
                .id(5L)
                .monto(300_000)
                .valorPendiente(200_000)
                .motivo("Administración Enero")
                .build();
        when(obligacionRepository.findByCasaId(casa.getId())).thenReturn(List.of(o1));

        // pago detalle and pago (pago parcial: 200k < 300k)
        Pago pago = Pago.builder()
                .id(10L)
                .fechaPago(LocalDate.of(2024, 2, 15))
                .total(200_000)
                .build();

        PagoDetalle pd = PagoDetalle.builder()
                .id(20L)
                .montoPagado(200_000)
                .pago(pago)
                .obligacion(o1)
                .build();

        when(pagoDetalleRepository.findTopByObligacionCasaIdOrderByPagoFechaPagoDesc(casa.getId()))
                .thenReturn(Optional.of(pd));

        // act
        SuccessResult<AccountStatusDTO> result = service.getAccountStatus();

        // assert
        assertNotNull(result);
        assertEquals("Estado de la cuenta obtenido exitosamente", result.message());
        AccountStatusDTO dto = result.data();
        assertNotNull(dto);
        assertEquals(200_000, dto.getSaldoPendiente());
        assertEquals(EstadoFinancieroCasa.EN_MORA, dto.getEstadoCasa());

        UltimoPagoDTO ultimo = dto.getUltimoPago();
        assertNotNull(ultimo);
        assertEquals(LocalDate.of(2024, 2, 15), ultimo.getFecha());
        assertEquals("Administración Enero", ultimo.getConcepto());
        assertEquals(200_000, ultimo.getValor());
        // Pago parcial -> no fue pago completo
        assertFalse(ultimo.isFueAbonoCompleto());

        verify(obligacionRepository).findByCasaId(casa.getId());
        verify(pagoDetalleRepository).findTopByObligacionCasaIdOrderByPagoFechaPagoDesc(casa.getId());
    }

    @Test
    void getAccountStatus_withoutLastPayment() {
        // arrange
        when(personaRepository.findByUserEmail("propietario@example.com")).thenReturn(Optional.of(persona));
        when(obligacionRepository.findByCasaId(casa.getId())).thenReturn(List.of()); // sin obligaciones
        when(pagoDetalleRepository.findTopByObligacionCasaIdOrderByPagoFechaPagoDesc(casa.getId()))
                .thenReturn(Optional.empty());

        // act
        SuccessResult<AccountStatusDTO> result = service.getAccountStatus();

        // assert
        AccountStatusDTO dto = result.data();
        assertNotNull(dto);
        assertEquals(0, dto.getSaldoPendiente());
        assertEquals(EstadoFinancieroCasa.AL_DIA, dto.getEstadoCasa());
        assertNull(dto.getUltimoPago());

        verify(obligacionRepository).findByCasaId(casa.getId());
    }

    // ---------- getSolicitudesPropietario tests ----------

    @Test
    void getSolicitudesPropietario_returnsMappedList() {
        // arrange
        when(personaRepository.findByUserEmail("propietario@example.com")).thenReturn(Optional.of(persona));

        SolicitudReservaRecurso s1 = SolicitudReservaRecurso.builder()
                .id(100L)
                .fechaCreacion(LocalDate.of(2024, 3, 1))
                .fechaSolicitud(LocalDate.of(2024, 3, 10))
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(12, 0))
                .numeroInvitados(20)
                .estadoSolicitud(EstadoSolicitud.PENDIENTE)
                .casa(casa)
                .recursoComun(RecursoComun.builder().nombre("Salón Social").build())
                .build();

        when(solicitudRepository.findAllByCasa_Id(casa.getId())).thenReturn(List.of(s1));

        SolicitudPropiDTO dtoMock = new SolicitudPropiDTO();
        dtoMock.setId(100L);

        when(modelMapper.map(s1, SolicitudPropiDTO.class)).thenReturn(dtoMock);

        // act
        SuccessResult<List<SolicitudPropiDTO>> result = service.getSolicitudesPropietario();

        // assert
        assertNotNull(result);
        assertEquals("Solicitudes obtenidas exitosamente", result.message());
        List<SolicitudPropiDTO> lista = result.data();
        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(100L, lista.get(0).getId());

        verify(solicitudRepository).findAllByCasa_Id(casa.getId());
        verify(modelMapper).map(s1, SolicitudPropiDTO.class);
    }

//    @Test
//    void getSolicitudesPropietario_noCasa_throws() {
//        persona.setCasa(null);
//        when(personaRepository.findByUserEmail("propietario@example.com")).thenReturn(Optional.of(persona));
//
//        //ApiException ex = assertThrows(ApiException.class, () -> service.getSolicitudesPropietario());
//        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
//    }

    // ---------- helper for setting SecurityContext principal ----------
    /**
     * Simple util to set Authentication principal for tests.
     * Uses UsernamePasswordAuthenticationToken with String principal (email).
     */
    static class TestSecurityUtil {
        static void setAuthenticationWithUsername(String username) {
            var authorities = java.util.List.<org.springframework.security.core.GrantedAuthority>of();
            org.springframework.security.core.Authentication auth =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        static void clearAuth() {
            SecurityContextHolder.clearContext();
        }
    }
}
