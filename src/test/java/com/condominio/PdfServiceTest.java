package com.condominio;

import com.condominio.service.implementation.PdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PdfServiceTest {

    private PdfService pdfService;
    private SpringTemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        templateEngine = Mockito.mock(SpringTemplateEngine.class);
        pdfService = new PdfService(templateEngine);
    }

    @Test
    void generarPdf_DeberiaGenerarPdfCorrectamente() throws IOException {

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html><body><p>PDF Test</p></body></html>");


        byte[] resultado = pdfService.generarPdf("Miguel Echeverría", 44L, "2025-10-20");


        assertNotNull(resultado);
        assertTrue(resultado.length > 0, "El PDF generado no debería estar vacío");
    }

    @Test
    void generarPdf_SiFallaGeneracionLanzaIOException() {

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("Fallo en Thymeleaf"));


        assertThrows(IOException.class, () ->
                pdfService.generarPdf("Miguel", 1L, "2025-10-20")
        );
    }

    @Test
    void convertirLogoABase64_SiNoExisteDevuelveNull() throws IOException {


        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html><body>sin logo</body></html>");

        byte[] resultado = pdfService.generarPdf("SinLogo", 99L, "2025-10-20");

        assertNotNull(resultado);
    }
    @Test
    void generarPdf_SinLogo_NoLanzaExcepcion() throws IOException {

        ReflectionTestUtils.setField(pdfService, "templateEngine", templateEngine);
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html><body>Sin logo</body></html>");


        byte[] pdfBytes = pdfService.generarPdf("Carlos", 2L, "2025-10-21");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
