package com.condominio.controller;

import com.condominio.dto.response.SolicitudReparacionLocativaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.service.interfaces.ISolicitudReparacionLocativaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("solicitud-reparacion")
@RequiredArgsConstructor
public class SolicitudReparacionLocativaController {

    private final ISolicitudReparacionLocativaService solicitudReparacionLocativaService;

    @GetMapping("/solicitudes")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<List<SolicitudReparacionLocativaDTO>> findByEstado(
            @RequestParam ("estado")EstadoSolicitud estado){
        return solicitudReparacionLocativaService.findByEstado(estado);
    }
}
