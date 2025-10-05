package com.condominio;

import com.condominio.persistence.repository.MiembroRepository;
import com.condominio.service.implementation.MiembroService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MiembroServiceTest {

    @Mock
    private MiembroRepository miembroRepository;

    @InjectMocks
    private MiembroService miembroService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable= MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testCountByCasaId_ShouldReturnCorrectValue() {

        Long casaId = 1L;
        when(miembroRepository.countByCasaId(casaId)).thenReturn(3);


        int result = miembroService.countByCasaId(casaId);

        assertThat(result).isEqualTo(3);
        verify(miembroRepository).countByCasaId(casaId);
    }
}
