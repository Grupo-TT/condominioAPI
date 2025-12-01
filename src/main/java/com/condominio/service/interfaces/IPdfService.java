package com.condominio.service.interfaces;

import java.io.IOException;

public interface IPdfService {
    byte[] generarPdf(String nombre, Long idCasa, String fechaEmision) throws IOException;
}
