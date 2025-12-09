package com.condominio.controller;

import com.condominio.dto.response.AccountStatusDTO;
import com.condominio.dto.response.InfoCasaPropiDTO;
import com.condominio.dto.response.SolicitudPropiDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.IDashboardPropiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard-propietario")
@RequiredArgsConstructor
public class DashboardPropiController {
    private final IDashboardPropiService service;

    @GetMapping("/info")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
    public SuccessResult<InfoCasaPropiDTO> getBasicInfo() {
        return service.getPropiBasicInfo();
    }

    @GetMapping("/account-status")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
    public SuccessResult<AccountStatusDTO> getAccountStatus() {
        return service.getAccountStatus();
    }

    @GetMapping("/solicitudes")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
    public SuccessResult<List<SolicitudPropiDTO>> getSolicitudes() {
        return service.getSolicitudesPropietario();
    }

}
