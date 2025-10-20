package com.condominio.controller;

import com.condominio.dto.response.EstadoCuentaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.IObligacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("obligacion")
public class ObligacionController {
    private final IObligacionService obligacionService;

    @GetMapping("/{idCasa}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<EstadoCuentaDTO> obtenerObligaciones(@PathVariable Long idCasa) {
        return obligacionService.estadoDeCuentaCasa(idCasa);
    }

    @GetMapping("/paz-y-salvo/{idCasa}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> descargarPazYSalvo(@PathVariable Long idCasa) {
        return obligacionService.generarPazYSalvo(idCasa);

    }

}
