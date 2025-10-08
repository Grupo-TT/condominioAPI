package com.condominio;

import com.condominio.persistence.repository.MascotaRepository;
import com.condominio.service.implementation.MascotaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MascotaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

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
}