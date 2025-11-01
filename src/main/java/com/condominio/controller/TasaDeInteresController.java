package com.condominio.controller;

import com.condominio.dto.request.TasaDeInteresUpdateDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.TasaDeInteres;
import com.condominio.service.interfaces.ITasaDeInteres;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("tasa-interes")
public class TasaDeInteresController {

    private final ITasaDeInteres tasaDeInteres;

    @PutMapping("/actualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<TasaDeInteres> actualizarTasaDeInteres(@RequestBody TasaDeInteresUpdateDTO dto){

        return tasaDeInteres.actualizarTasaDeInteres(dto.nuevoValor());
    }
}
