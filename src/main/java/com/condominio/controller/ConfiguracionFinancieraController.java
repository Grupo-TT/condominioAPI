package com.condominio.controller;

import com.condominio.dto.response.ConfiguracionFinancieraDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.implementation.ConfiguracionFinancieraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/configuracion-financiera")
@RequiredArgsConstructor
public class ConfiguracionFinancieraController {

    private final ConfiguracionFinancieraService configuracionService;

    @GetMapping
    public ResponseEntity<SuccessResult<ConfiguracionFinancieraDTO>> obtenerConfiguracion() {
        ConfiguracionFinancieraDTO data = configuracionService.obtenerConfiguracion();
        return ResponseEntity.ok(new SuccessResult<>("Configuración cargada correctamente", data));
    }
}
