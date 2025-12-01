package com.condominio.controller;

import com.condominio.dto.response.CasaCuentaDTO;
import com.condominio.dto.response.CasaDeudoraDTO;
import com.condominio.dto.response.CasaInfoDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.ICasaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("casa")
public class CasaController {

    private final ICasaService casaService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<List<CasaInfoDTO>>  listarCasas() {
        return casaService.obtenerCasas();
    }

    @GetMapping("/{idCasa}/estado-cuenta")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<CasaCuentaDTO> obtenerEstadoCuenta(
            @PathVariable Long idCasa
            ) {
        return casaService.estadoDeCuenta(idCasa);
    }

    @GetMapping("/por-cobrar")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<List<CasaDeudoraDTO>> obtenerCasasConObligacionesPorCobrar() {
        return casaService.obtenerCasasConObligacionesPorCobrar();
    }

    @GetMapping("/obligaciones-casa")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<List<CasaDeudoraDTO>> obtenerObligacionesPorCasa() {
        return casaService. obtenerObligacionesPorCasa();
    }
}
