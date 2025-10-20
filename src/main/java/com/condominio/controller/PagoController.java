package com.condominio.controller;

import com.condominio.dto.response.ObligacionDTO;
import com.condominio.dto.response.PagoDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.IPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pago")
public class PagoController {

    private final IPagoService pagoService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    SuccessResult<ObligacionDTO> realizarPago(@RequestBody PagoDTO pagoDTO) {
        return pagoService.registrarPago(pagoDTO);
    }
}
