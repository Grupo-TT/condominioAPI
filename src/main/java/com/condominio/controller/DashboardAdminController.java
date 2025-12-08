package com.condominio.controller;

import com.condominio.dto.response.MovimientosMesDTO;
import com.condominio.dto.response.ResumenFinancieroDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.IDashboardAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard-admin")
@RequiredArgsConstructor
public class DashboardAdminController {

    private final IDashboardAdminService dashboardAdminService;

    @GetMapping("/resumen-year")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<ResumenFinancieroDTO> getResumenFinancieronByYear(@RequestParam int year) {
        return dashboardAdminService.getResumenFinancieronByYear(year);
    }

    @GetMapping("/resumen-month")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<MovimientosMesDTO> getResumenFinancieronByYear() {
        return dashboardAdminService.getResumenFinancieronMesReciente();
    }
}
