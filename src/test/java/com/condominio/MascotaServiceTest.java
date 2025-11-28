package com.condominio;

import com.condominio.dto.request.MascotaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.Mascota;
import com.condominio.persistence.model.TipoMascota;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.persistence.repository.MascotaRepository;
import com.condominio.service.implementation.MascotaService;
import com.condominio.util.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MascotaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private CasaRepository casaRepository;

    @InjectMocks
    private MascotaService mascotaService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }


    @Test
    void testCountByCasaId_ShouldReturnCorrectValue() {

        Long casaId = 1L;
        when(mascotaRepository.countByCasaId(casaId)).thenReturn(2);


        int result = mascotaService.countByCasaId(casaId);


        assertThat(result).isEqualTo(2);
        verify(mascotaRepository).countByCasaId(casaId);
    }

    @Test
    void addMascota_ShouldIncreaseQuantity_WhenMascotaExists() {
        // Arrange
        MascotaDTO dto = new MascotaDTO();
        dto.setTipoMascota(TipoMascota.PERRO);
        dto.setCantidad((short)1);
        dto.setIdCasa(1L);
        Mascota mascota = Mascota.builder()
                .tipoMascota(TipoMascota.PERRO)
                .cantidad((short)3)
                .build();
        Casa casa = Casa.builder().id(1L).build();

        when(mascotaRepository.findByTipoMascotaAndCasa_Id(TipoMascota.PERRO, 1L))
                .thenReturn(Optional.of(mascota));
        when(casaRepository.findById(1L)).thenReturn(Optional.of(casa));

        // Act
        SuccessResult<Void> result = mascotaService.addMascota(dto);

        // Assert
        assertEquals("Mascota creada satisfactoriamente", result.message());
        assertEquals(4, mascota.getCantidad());
        verify(mascotaRepository).save(mascota);
    }

    @Test
    void addMascota_ShouldCreateNew_WhenMascotaDoesNotExist() {
        // Arrange
        MascotaDTO dto = new MascotaDTO();
        dto.setTipoMascota(TipoMascota.GATO);
        dto.setCantidad((short)1);
        dto.setIdCasa(4L);
        Casa casa = Casa.builder().id(4L).build();

        when(mascotaRepository.findByTipoMascotaAndCasa_Id(TipoMascota.GATO, 1L))
                .thenReturn(Optional.empty());
        when(casaRepository.findById(4L)).thenReturn(Optional.of(casa));

        // Act
        SuccessResult<Void> result = mascotaService.addMascota(dto);

        // Assert
        assertEquals("Mascota creada satisfactoriamente", result.message());
        verify(mascotaRepository).save(any(Mascota.class));
    }

    @Test
    void addMascota_ShouldThrowException_WhenCasaNotFound() {
        // Arrange
        MascotaDTO dto = new MascotaDTO();
        dto.setTipoMascota(TipoMascota.GATO);
        dto.setCantidad((short)99);
        dto.setIdCasa(3L);
        when(mascotaRepository.findByTipoMascotaAndCasa_Id(TipoMascota.GATO, 99L))
                .thenReturn(Optional.empty());
        when(casaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class,
                () -> mascotaService.addMascota(dto));

        assertEquals("No se encontró la casa.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    // -------------------- subtractMascota --------------------

    @Test
    void subtractMascota_ShouldUpdateQuantity_WhenMascotaExists() {
        // Arrange
        MascotaDTO dto = new MascotaDTO();
        dto.setTipoMascota(TipoMascota.PERRO);
        dto.setCantidad((short)1);
        dto.setIdCasa(1L);
        Mascota mascota = Mascota.builder()
                .tipoMascota(TipoMascota.PERRO)
                .cantidad((short)5)
                .build();

        when(mascotaRepository.findByTipoMascotaAndCasa_Id(TipoMascota.PERRO, 1L))
                .thenReturn(Optional.of(mascota));

        // Act
        SuccessResult<Void> result = mascotaService.subtractMascota(dto);

        // Assert
        assertEquals("Se modificó la cantidad satisfactoriamente", result.message());
        assertEquals(1, mascota.getCantidad());
        verify(mascotaRepository).save(mascota);
    }

    @Test
    void subtractMascota_ShouldThrowException_WhenMascotaDoesNotExist() {
        // Arrange
        MascotaDTO dto = new MascotaDTO();
        dto.setTipoMascota(TipoMascota.PERRO);
        dto.setCantidad((short)1);
        dto.setIdCasa(2L);

        when(mascotaRepository.findByTipoMascotaAndCasa_Id(TipoMascota.PERRO, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class,
                () -> mascotaService.subtractMascota(dto));

        assertEquals("No tiene mascotas para editar.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void subtractMascota_ShouldThrowException_WhenCantidadIsNegative() {
        // Arrange
        MascotaDTO dto = new MascotaDTO();
        dto.setTipoMascota(TipoMascota.PERRO);
        dto.setCantidad((short)1);
        dto.setIdCasa(-1L);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class,
                () -> mascotaService.subtractMascota(dto));

        assertEquals("No tiene mascotas para editar.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}