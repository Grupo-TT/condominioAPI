package com.condominio.service.implementation;

import com.condominio.service.interfaces.IPdfService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Service
public class PdfService implements IPdfService {

    private final SpringTemplateEngine templateEngine;

    public PdfService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generarPdf(String nombreCompleto, Long idCasa, String fechaEmision) throws IOException {
        Context context = new Context();


        context.setVariable("nombre", nombreCompleto);


        String casaTexto = "Casa " + idCasa;
        context.setVariable("casa", casaTexto);


        context.setVariable("fecha", fechaEmision);


        context.setVariable("estado", "al_dia");


        context.setVariable("condominioNombre", "Condominio Flor del  campo");

        String logoBase64 = convertirLogoABase64("static/images/logo.png");

        if (logoBase64 != null) {
            context.setVariable("logoPresent", true);
            context.setVariable("logoBase64", logoBase64);
        } else {
            context.setVariable("logoPresent", false);
        }

        context.setVariable("firmaPresent", false);
        context.setVariable("firmaNombre", "Administración Condominio");


        final String html;
        try {
            html = templateEngine.process("UpToDatePDF/uptodate", context);
        } catch (Exception e) {

            throw new IOException("Error procesando la plantilla Thymeleaf: " + e.getMessage(), e);
        }

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception ex) {
            throw new IOException("Error generando PDF: " + ex.getMessage(), ex);
        }
    }

    private String convertirLogoABase64(String rutaRelativa) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(rutaRelativa)) {
            if (inputStream == null) return null;

            byte[] bytes = inputStream.readAllBytes();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            return null;
        }
    }
}
