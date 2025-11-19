package com.condominio.controller;

import com.condominio.dto.response.SolicitudReparacionLocativaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.service.interfaces.ISolicitudReparacionLocativaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<SolicitudReparacionLocativaDTO> aprobar(@PathVariable Long id){
        return solicitudReparacionLocativaService.aprobar(id);
    }

    @PutMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<SolicitudReparacionLocativaDTO> rechazar(@PathVariable Long id , @RequestParam("comentarios") String comentarios){
        return solicitudReparacionLocativaService.rechazar(id, comentarios);
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<SolicitudReparacionLocativaDTO> update(@PathVariable Long id, @RequestBody SolicitudReparacionLocativaDTO solicitud){
        return solicitudReparacionLocativaService.update(id, solicitud);
    }

}
