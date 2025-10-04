package com.condominio;

import com.condominio.persistence.model.Casa;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.service.implementation.CasaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;


class CasaServiceTest {

    @Mock
    private CasaRepository casaRepository;

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
}