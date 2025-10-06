package com.condominio;

import static org.assertj.core.api.Assertions.assertThat;
import com.condominio.dto.response.CasaCuentaDTO;
import com.condominio.dto.response.CasaInfoDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.persistence.repository.ObligacionRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.implementation.CasaService;
import com.condominio.service.implementation.MascotaService;
import com.condominio.service.implementation.MiembroService;
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


        SuccessResult<CasaCuentaDTO> result = casaService.EstadoDeCuenta(1L);


        assertThat(result).isNotNull();
        assertThat(result.data().getSaldoPendienteTotal()).isEqualTo(500);
        assertThat(result.data().getMultasActivas()).hasSize(2);
        assertThat(result.message()).isEqualTo("Estado de cuenta obtenido correctamente");

        verify(obligacionRepository).findByCasaId(1L);
    }

    @Test
    void testObtenerCasas_WhenCasasExist() {

        when(casaRepository.findAll()).thenReturn(List.of(casa));

        Persona propietario = new Persona();
        propietario.setId(1L);
        propietario.setPrimerNombre("Juan");

        when(personaRepository.findPropietarioByCasaId(1L))
                .thenReturn(Optional.of(propietario));
        when(miembroService.countByCasaId(1L)).thenReturn(2);
        when(mascotaService.countByCasaId(1L)).thenReturn(1);


        SuccessResult<List<CasaInfoDTO>> result = casaService.obtenerCasas();


        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        CasaInfoDTO dto = result.data().getFirst();

        assertThat(dto.getNumeroCasa()).isEqualTo(101);
        assertThat(dto.getPropietario()).isEqualTo(propietario);
        assertThat(dto.getCantidadMiembros()).isEqualTo(2);
        assertThat(dto.getCantidadMascotas()).isEqualTo(1);
        assertThat(result.message()).isEqualTo("Casas obtenidas correctamente");

        verify(casaRepository).findAll();
        verify(personaRepository).findPropietarioByCasaId(1L);
        verify(miembroService).countByCasaId(1L);
        verify(mascotaService).countByCasaId(1L);
    }

    @Test
    void testObtenerCasas_WhenNoCasasExist_ShouldThrowApiException() {
        when(casaRepository.findAll()).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> casaService.obtenerCasas());
    }
}