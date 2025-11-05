package com.condominio.controller;

import com.condominio.dto.request.TasaDeInteresUpdateDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.PagoAdicional;
import com.condominio.service.interfaces.IPagoAdicional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("pago-adicional")
public class PagoAdicionalController {

    private final IPagoAdicional pagoAdicional;

    @PutMapping("/actualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<PagoAdicional> actualizarTasaDeInteres(@RequestBody TasaDeInteresUpdateDTO dto){

        return pagoAdicional.actualizarPagoAdicional(dto.nuevoValor());
    }
}
