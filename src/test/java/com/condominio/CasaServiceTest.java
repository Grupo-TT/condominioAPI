package com.condominio;

import static org.assertj.core.api.Assertions.assertThat;

import com.condominio.dto.response.*;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.persistence.repository.MascotaRepository;
import com.condominio.persistence.repository.ObligacionRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.implementation.CasaService;
import com.condominio.service.implementation.MascotaService;
import com.condominio.service.implementation.MiembroService;
import com.condominio.service.interfaces.IPagoService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


class CasaServiceTest {

    @Mock
    private CasaRepository casaRepository;

    @Mock
    private ObligacionRepository obligacionRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private MiembroService miembroService;

    @Mock
    private MascotaService mascotaService;

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private IPagoService pagoService;

    @InjectMocks
    private CasaService casaService;

    private Casa casa;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        casa = new Casa();
        casa.setId(1L);
        casa.setNumeroCasa(101);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testFindById_WhenCasaExists() {
        when(casaRepository.findById(1L)).thenReturn(Optional.of(casa));

        Optional<Casa> result = casaService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result).map(Casa::getNumeroCasa).hasValue(101);

        verify(casaRepository).findById(1L);
    }

    @Test
    void testFindById_WhenCasaDoesNotExist() {
        when(casaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Casa> result = casaService.findById(99L);

        assertThat(result).isNotPresent();
        verify(casaRepository).findById(99L);
    }

    @Test
    void testSave_ShouldCallRepositorySave() {
        casaService.save(casa);

        verify(casaRepository).save(casa);
    }

    @Test
    void testEstadoDeCuenta_WhenCasaHasObligacionesPendientes() {

        Obligacion obligacion1 = new Obligacion();
        obligacion1.setId(1L);
        obligacion1.setMonto(500);
        obligacion1.setEstadoPago(EstadoPago.PENDIENTE);
        obligacion1.setTipoPago(TipoPago.DINERO);
        obligacion1.setFechaGenerada(LocalDate.now());

        Obligacion obligacion2 = new Obligacion();
        obligacion2.setId(2L);
        obligacion2.setMonto(300);
        obligacion2.setEstadoPago(EstadoPago.CONDONADO);
        obligacion2.setTipoPago(TipoPago.DINERO);

        when(obligacionRepository.findByCasaId(1L))
                .thenReturn(List.of(obligacion1, obligacion2));


        SuccessResult<CasaCuentaDTO> result = casaService.estadoDeCuenta(1L);


        assertThat(result).isNotNull();
        assertThat(result.data().getSaldoPendienteTotal()).isEqualTo(500);
        assertThat(result.data().getMultasActivas()).hasSize(1);
        assertThat(result.message()).isEqualTo("Estado de cuenta obtenido correctamente");

        verify(obligacionRepository).findByCasaId(1L);
    }

    @Test
    void testObtenerCasas_WhenCasasExist() {


        Casa newCasa = new Casa();
        newCasa.setId(1L);
        newCasa.setNumeroCasa(101);

        when(casaRepository.findAll()).thenReturn(List.of(newCasa));


        Persona propietario = new Persona();
        propietario.setPrimerNombre("Juan");
        propietario.setPrimerApellido("Pérez");

        UserEntity user = new UserEntity();
        user.setEmail("juan@example.com");
        propietario.setUser(user);
        propietario.setTelefono(123456789L);

        when(personaRepository.findPropietarioByCasaId(1L))
                .thenReturn(Optional.of(propietario));


        when(personaRepository.findArrendatarioByCasaId(1L))
                .thenReturn(Optional.of(new Persona()));


        when(miembroService.countByCasaId(1L)).thenReturn(2);


        when(mascotaRepository.contarPorTipo(1L))
                .thenReturn(List.of(
                        new MascotaCountDTO(TipoMascota.PERRO, 1L),
                        new MascotaCountDTO(TipoMascota.GATO, 2L)
                ));


        when(obligacionRepository.existsByCasaIdAndEstadoPago(1L, EstadoPago.PENDIENTE))
                .thenReturn(false);


        SuccessResult<List<CasaInfoDTO>> result = casaService.obtenerCasas();


        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);

        CasaInfoDTO dto = result.data().getFirst();
        assertThat(dto).isNotNull();


        PersonaSimpleDTO propietarioDTO = dto.getPropietario();
        assertThat(propietarioDTO).isNotNull();
        assertThat(propietarioDTO.getNombreCompleto()).isEqualTo("Juan Pérez");
        assertThat(propietarioDTO.getTelefono()).isEqualTo(123456789L);
        assertThat(propietarioDTO.getCorreo()).isEqualTo("juan@example.com");


        assertThat(dto.getNumeroCasa()).isEqualTo(101);
        assertThat(dto.getCantidadMiembros()).isEqualTo(2);


        assertThat(dto.getMascotas()).containsEntry("TipoMascota.PERRO", 1);
        assertThat(dto.getMascotas()).containsEntry("TipoMascota.GATO", 2);
        assertThat(dto.getMascotas().getOrDefault("TipoMascota.OTRO", 0)).isEqualTo(0L);


        assertThat(dto.getUsoCasa()).isEqualTo(UsoCasa.ARRENDADA);
        assertThat(dto.getEstadoFinancieroCasa()).isEqualTo(EstadoFinancieroCasa.AL_DIA);


        assertThat(result.message()).isEqualTo("Casas obtenidas correctamente");


        verify(casaRepository).findAll();
        verify(personaRepository).findPropietarioByCasaId(1L);
        verify(personaRepository).findArrendatarioByCasaId(1L);
        verify(miembroService).countByCasaId(1L);
        verify(mascotaRepository).contarPorTipo(1L);
        verify(obligacionRepository).existsByCasaIdAndEstadoPago(1L, EstadoPago.PENDIENTE);
    }

    @Test
    void testObtenerCasas_NoCasas_ShouldThrowApiException() {
        when(casaRepository.findAll()).thenReturn(List.of());
        assertThrows(ApiException.class, () -> casaService.obtenerCasas());
        verify(casaRepository).findAll();
    }

    @Test
    void testObtenerCasas_ShouldBuildPersonaSimpleDTO_WhenPropietarioExists() {

        Casa newCasa = new Casa();
        newCasa.setId(1L);
        newCasa.setNumeroCasa(101);

        when(casaRepository.findAll()).thenReturn(List.of(newCasa));


        Persona propietario = new Persona();
        propietario.setPrimerNombre("Juan");
        propietario.setPrimerApellido("Pérez");
        propietario.setTelefono(123456789L);

        UserEntity user = new UserEntity();
        user.setEmail("juan@example.com");
        propietario.setUser(user);

        when(personaRepository.findPropietarioByCasaId(1L))
                .thenReturn(Optional.of(propietario));
        when(miembroService.countByCasaId(1L)).thenReturn(2);
        when(mascotaService.countByCasaId(1L)).thenReturn(1);

        SuccessResult<List<CasaInfoDTO>> result = casaService.obtenerCasas();


        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);

        CasaInfoDTO dto = result.data().getFirst();


        PersonaSimpleDTO propietarioDTO = dto.getPropietario();
        assertThat(propietarioDTO).isNotNull();
        assertThat(propietarioDTO.getNombreCompleto()).isEqualTo("Juan Pérez");
        assertThat(propietarioDTO.getTelefono()).isEqualTo(123456789L);
        assertThat(propietarioDTO.getCorreo()).isEqualTo("juan@example.com");


        assertThat(dto.getNumeroCasa()).isEqualTo(101);
        assertThat(dto.getCantidadMiembros()).isEqualTo(2);
        assertThat(dto.getCantidadMascotas()).isEqualTo(1);
        assertThat(result.message()).isEqualTo("Casas obtenidas correctamente");


        verify(casaRepository).findAll();
        verify(personaRepository).findPropietarioByCasaId(1L);
        verify(miembroService).countByCasaId(1L);
        verify(mascotaService).countByCasaId(1L);
    }

    @Test
    void testObtenerCasas_ShouldReturnNullPropietario_WhenNoPropietarioExists() {

        Casa newCasa = new Casa();
        newCasa.setId(2L);
        newCasa.setNumeroCasa(202);

        when(casaRepository.findAll()).thenReturn(List.of(newCasa));


        when(personaRepository.findPropietarioByCasaId(2L))
                .thenReturn(Optional.empty());
        when(miembroService.countByCasaId(2L)).thenReturn(3);
        when(mascotaService.countByCasaId(2L)).thenReturn(0);

        SuccessResult<List<CasaInfoDTO>> result = casaService.obtenerCasas();

        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);

        CasaInfoDTO dto = result.data().getFirst();


        assertThat(dto.getPropietario()).isNull();


        assertThat(dto.getNumeroCasa()).isEqualTo(202);
        assertThat(dto.getCantidadMiembros()).isEqualTo(3);
        assertThat(dto.getCantidadMascotas()).isEqualTo(0);
        assertThat(result.message()).isEqualTo("Casas obtenidas correctamente");


        verify(casaRepository).findAll();
        verify(personaRepository).findPropietarioByCasaId(2L);
        verify(miembroService).countByCasaId(2L);
        verify(mascotaService).countByCasaId(2L);
    }

    @Test
    void testObtenerCasas_CasaCompleta() {
        Casa newCasa = new Casa();
        newCasa.setId(1L);
        newCasa.setNumeroCasa(101);
        when(casaRepository.findAll()).thenReturn(List.of(newCasa));

        Persona propietario = new Persona();
        propietario.setPrimerNombre("Juan");
        propietario.setPrimerApellido("Pérez");
        propietario.setTelefono(123L);
        UserEntity user = new UserEntity();
        user.setEmail("juan@example.com");
        propietario.setUser(user);
        when(personaRepository.findPropietarioByCasaId(1L)).thenReturn(Optional.of(propietario));

        when(personaRepository.findArrendatarioByCasaId(1L)).thenReturn(Optional.of(new Persona()));
        when(miembroService.countByCasaId(1L)).thenReturn(2);

        when(mascotaRepository.contarPorTipo(1L)).thenReturn(List.of(
                new MascotaCountDTO(TipoMascota.PERRO, 1L),
                new MascotaCountDTO(TipoMascota.GATO, 2L)
        ));

        when(obligacionRepository.existsByCasaIdAndEstadoPago(1L, EstadoPago.PENDIENTE))
                .thenReturn(false);

        SuccessResult<List<CasaInfoDTO>> result = casaService.obtenerCasas();
        CasaInfoDTO dto = result.data().getFirst();

        assertThat(dto.getPropietario()).isNotNull();
        assertThat(dto.getUsoCasa()).isEqualTo(UsoCasa.ARRENDADA);
        assertThat(dto.getEstadoFinancieroCasa()).isEqualTo(EstadoFinancieroCasa.AL_DIA);
        assertThat(dto.getMascotas().get("TipoMascota.PERRO")).isEqualTo(1);
        assertThat(dto.getMascotas().get("TipoMascota.GATO")).isEqualTo(2);
        assertThat(dto.getMascotas().get("TipoMascota.OTRO")).isEqualTo(0);
    }

    @Test
    void testObtenerCasas_SinPropietarioNiArrendatario_ConDeuda() {
        Casa newCasa = new Casa();
        newCasa.setId(2L);
        newCasa.setNumeroCasa(202);
        when(casaRepository.findAll()).thenReturn(List.of(newCasa));

        when(personaRepository.findPropietarioByCasaId(2L)).thenReturn(Optional.empty());
        when(personaRepository.findArrendatarioByCasaId(2L)).thenReturn(Optional.empty());
        when(miembroService.countByCasaId(2L)).thenReturn(0);

        when(mascotaRepository.contarPorTipo(2L)).thenReturn(List.of());
        when(obligacionRepository.existsByCasaIdAndEstadoPago(2L, EstadoPago.PENDIENTE))
                .thenReturn(true);

        SuccessResult<List<CasaInfoDTO>> result = casaService.obtenerCasas();
        CasaInfoDTO dto = result.data().getFirst();

        assertThat(dto.getPropietario()).isNull();
        assertThat(dto.getUsoCasa()).isEqualTo(UsoCasa.RESIDENCIAL);
        assertThat(dto.getEstadoFinancieroCasa()).isEqualTo(EstadoFinancieroCasa.EN_MORA);
        assertThat(dto.getMascotas().get("TipoMascota.PERRO")).isEqualTo(0);
        assertThat(dto.getMascotas().get("TipoMascota.GATO")).isEqualTo(0);
        assertThat(dto.getMascotas().get("TipoMascota.OTRO")).isEqualTo(0);
    }

    @Test
    void testObtenerCasas_PropietarioSinArrendatario_MascotasParciales() {
        Casa newCasa = new Casa();
        newCasa.setId(3L);
        newCasa.setNumeroCasa(303);
        when(casaRepository.findAll()).thenReturn(List.of(newCasa));

        Persona propietario = new Persona();
        propietario.setPrimerNombre("Ana");
        propietario.setPrimerApellido("Lopez");
        propietario.setTelefono(555L);
        UserEntity user = new UserEntity();
        user.setEmail("ana@example.com");
        propietario.setUser(user);
        when(personaRepository.findPropietarioByCasaId(3L)).thenReturn(Optional.of(propietario));

        when(personaRepository.findArrendatarioByCasaId(3L)).thenReturn(Optional.empty());
        when(miembroService.countByCasaId(3L)).thenReturn(1);

        when(mascotaRepository.contarPorTipo(3L)).thenReturn(List.of(
                new MascotaCountDTO(TipoMascota.GATO, 3L)
        ));
        when(obligacionRepository.existsByCasaIdAndEstadoPago(3L, EstadoPago.PENDIENTE))
                .thenReturn(false);

        SuccessResult<List<CasaInfoDTO>> result = casaService.obtenerCasas();
        CasaInfoDTO dto = result.data().getFirst();

        assertThat(dto.getPropietario()).isNotNull();
        assertThat(dto.getUsoCasa()).isEqualTo(UsoCasa.RESIDENCIAL);
        assertThat(dto.getEstadoFinancieroCasa()).isEqualTo(EstadoFinancieroCasa.AL_DIA);
        assertThat(dto.getMascotas().get("TipoMascota.PERRO")).isEqualTo(0);
        assertThat(dto.getMascotas().get("TipoMascota.GATO")).isEqualTo(3);
        assertThat(dto.getMascotas().get("TipoMascota.OTRO")).isEqualTo(0);
    }

    @Test
    void testObtenerCasasConObligacionesPorCobrar_WhenCasasExist() {
        Casa newCasa = new Casa();
        newCasa.setId(1L);
        newCasa.setNumeroCasa(101);

        UserEntity user = new UserEntity();
        user.setEmail("propietario@mail.com");

        Persona propietario = new Persona();
        propietario.setId(1L);
        propietario.setPrimerNombre("Juan");
        propietario.setPrimerApellido("Pérez");
        propietario.setTelefono(3123456789L);
        propietario.setUser(user);

        Obligacion obligacion = new Obligacion();
        obligacion.setId(10L);
        obligacion.setEstadoPago(EstadoPago.PENDIENTE);
        obligacion.setMotivo("Cuota de administración");
        obligacion.setMonto(50000);
        obligacion.setValorPendiente(50000);
        obligacion.setCasa(newCasa);

        when(casaRepository.findCasasConObligacionesPorCobrar()).thenReturn(List.of(newCasa));
        when(personaRepository.findPropietarioByCasaId(1L)).thenReturn(Optional.of(propietario));
        when(obligacionRepository.findByCasaIdAndEstadoPagoIsNotOrderByFechaGeneradaDesc(1L, EstadoPago.CONDONADO))
                .thenReturn(List.of(obligacion));

        SuccessResult<List<CasaDeudoraDTO>> result = casaService.obtenerCasasConObligacionesPorCobrar();

        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        assertThat(result.message()).isEqualTo("Casas con obligaciones por cobrar obtenidas correctamente");

        CasaDeudoraDTO dto = result.data().getFirst();
        assertThat(dto.getNumeroCasa()).isEqualTo(101);
        assertThat(dto.getSaldoPendiente()).isEqualTo(50000);
        assertThat(dto.getPropietario().getNombreCompleto()).isEqualTo("Juan Pérez");
        assertThat(dto.getPropietario().getCorreo()).isEqualTo("propietario@mail.com");
        assertThat(dto.getObligacionesPendientes()).hasSize(1);
        assertThat(dto.getObligacionesPendientes().getFirst().getMotivo()).isEqualTo("Cuota de administración");

        verify(casaRepository).findCasasConObligacionesPorCobrar();
        verify(personaRepository).findPropietarioByCasaId(1L);
        verify(obligacionRepository).findByCasaIdAndEstadoPagoIsNotOrderByFechaGeneradaDesc(1L, EstadoPago.CONDONADO);
    }

    @Test
    void testObtenerCasasConObligacionesPorCobrar_WhenNoPropietario_ShouldReturnNullPropietario() {
        Casa newCasa = new Casa();
        newCasa.setId(2L);
        newCasa.setNumeroCasa(202);

        Obligacion obligacion = new Obligacion();
        obligacion.setId(20L);
        obligacion.setEstadoPago(EstadoPago.PENDIENTE);
        obligacion.setMotivo("Mantenimiento");
        obligacion.setMonto(30000);
        obligacion.setValorPendiente(30000);
        obligacion.setCasa(newCasa);

        when(casaRepository.findCasasConObligacionesPorCobrar()).thenReturn(List.of(newCasa));
        when(personaRepository.findPropietarioByCasaId(2L)).thenReturn(Optional.empty());
        when(obligacionRepository.findByCasaIdAndEstadoPagoIsNotOrderByFechaGeneradaDesc(2L, EstadoPago.CONDONADO))
                .thenReturn(List.of(obligacion));

        SuccessResult<List<CasaDeudoraDTO>> result = casaService.obtenerCasasConObligacionesPorCobrar();

        assertThat(result).isNotNull();
        CasaDeudoraDTO dto = result.data().getFirst();
        assertThat(dto.getPropietario()).isNull();
        assertThat(dto.getSaldoPendiente()).isEqualTo(30000);
        assertThat(dto.getObligacionesPendientes()).hasSize(1);
    }

    @Test
    void testObtenerCasasConObligacionesPorCobrar_WhenNoCasasExist_ShouldThrowApiException() {
        when(casaRepository.findCasasConObligacionesPorCobrar()).thenReturn(List.of());

        assertThrows(ApiException.class, () -> casaService.obtenerCasasConObligacionesPorCobrar());

        verify(casaRepository).findCasasConObligacionesPorCobrar();
    }

    @Test
    void testObtenerObligacionesPorCasa_WhenCasasExist() {
        // Arrange
        Casa newCasa = new Casa();
        newCasa.setId(1L);
        newCasa.setNumeroCasa(101);

        UserEntity user = new UserEntity();
        user.setEmail("propietario@mail.com");

        Persona propietario = new Persona();
        propietario.setId(1L);
        propietario.setPrimerNombre("Juan");
        propietario.setPrimerApellido("Pérez");
        propietario.setTelefono(3123456789L);
        propietario.setUser(user);

        Obligacion obligacion1 = new Obligacion();
        obligacion1.setId(10L);
        obligacion1.setMotivo("Cuota de administración");
        obligacion1.setMonto(50000);
        obligacion1.setValorTotal(60000);
        obligacion1.setValorPendiente(50000);
        obligacion1.setMontoPagado(10000);
        obligacion1.setEstadoPago(EstadoPago.PENDIENTE);
        obligacion1.setTipoObligacion(TipoObligacion.ADMINISTRACION);
        obligacion1.setCasa(newCasa);
        obligacion1.setFechaGenerada(LocalDate.of(2025, 1, 1));

        Obligacion obligacion2 = new Obligacion();
        obligacion2.setId(11L);
        obligacion2.setMotivo("Fondo de reserva");
        obligacion2.setMonto(40000);
        obligacion2.setValorTotal(40000);
        obligacion2.setValorPendiente(40000);
        obligacion2.setMontoPagado(0);
        obligacion2.setEstadoPago(EstadoPago.PENDIENTE);
        obligacion2.setTipoObligacion(TipoObligacion.MULTA);
        obligacion2.setCasa(newCasa);
        obligacion2.setFechaGenerada(LocalDate.of(2025, 2, 1));

        when(casaRepository.obtenerObligacionesPorCasa()).thenReturn(List.of(newCasa));
        when(personaRepository.findPropietarioByCasaId(1L)).thenReturn(Optional.of(propietario));
        when(obligacionRepository.findByCasaIdOrderByFechaGeneradaDesc(1L))
                .thenReturn(List.of(obligacion1, obligacion2));
        when(pagoService.obtenerFechaUltimoPagoPorCasa(1L))
                .thenReturn(Optional.of(LocalDate.of(2025, 3, 15)));

        // Act
        SuccessResult<List<CasaDeudoraDTO>> result = casaService.obtenerObligacionesPorCasa();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        assertThat(result.message()).isEqualTo("Casas con obligaciones por cobrar obtenidas correctamente");

        CasaDeudoraDTO dto = result.data().getFirst();
        assertThat(dto.getNumeroCasa()).isEqualTo(101);
        assertThat(dto.getPropietario()).isNotNull();
        assertThat(dto.getPropietario().getNombreCompleto()).isEqualTo("Juan Pérez");
        assertThat(dto.getPropietario().getCorreo()).isEqualTo("propietario@mail.com");
        assertThat(dto.getSaldoPendiente()).isEqualTo(90000);
        assertThat(dto.getUltimoPago()).isEqualTo(LocalDate.of(2025, 3, 15));

        assertThat(dto.getObligacionesPendientes()).hasSize(2);
        assertThat(dto.getObligacionesPendientes().get(0).getMotivo()).isEqualTo("Cuota de administración");
        assertThat(dto.getObligacionesPendientes().get(1).getMotivo()).isEqualTo("Fondo de reserva");

        verify(casaRepository).obtenerObligacionesPorCasa();
        verify(personaRepository).findPropietarioByCasaId(1L);
        verify(obligacionRepository).findByCasaIdOrderByFechaGeneradaDesc(1L);
        verify(pagoService).obtenerFechaUltimoPagoPorCasa(1L);

    }

    @Test
    void testObtenerObligacionesPorCasa_WhenNoCasas_ShouldThrowApiException() {
        when(casaRepository.obtenerObligacionesPorCasa()).thenReturn(List.of());

        assertThrows(ApiException.class, () -> casaService.obtenerObligacionesPorCasa());

        verify(casaRepository).obtenerObligacionesPorCasa();
    }

    @Test
    void testObtenerObligacionesPorCasa_WhenNoPropietario() {
        Casa newCasa = new Casa();
        newCasa.setId(2L);
        newCasa.setNumeroCasa(202);

        Obligacion obligacion = new Obligacion();
        obligacion.setId(20L);
        obligacion.setMotivo("Mantenimiento");
        obligacion.setEstadoPago(EstadoPago.PENDIENTE);
        obligacion.setMonto(30000);
        obligacion.setValorPendiente(30000);
        obligacion.setCasa(newCasa);

        when(casaRepository.obtenerObligacionesPorCasa()).thenReturn(List.of(newCasa));
        when(personaRepository.findPropietarioByCasaId(2L)).thenReturn(Optional.empty());
        when(obligacionRepository.findByCasaIdOrderByFechaGeneradaDesc(2L)).thenReturn(List.of(obligacion));
        when(pagoService.obtenerFechaUltimoPagoPorCasa(2L)).thenReturn(Optional.empty());

        SuccessResult<List<CasaDeudoraDTO>> result = casaService.obtenerObligacionesPorCasa();

        CasaDeudoraDTO dto = result.data().getFirst();
        assertThat(dto.getPropietario()).isNull();
        assertThat(dto.getSaldoPendiente()).isEqualTo(30000);
        assertThat(dto.getUltimoPago()).isNull();
    }

}