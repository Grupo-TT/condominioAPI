package com.condominio.controller;

import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.service.interfaces.ISolicitudReservaRecursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/solicitud-recurso")
@RequiredArgsConstructor
public class SolicitudReservaRecursoController {
    private final ISolicitudReservaRecursoService solicitudReservaService;


    @GetMapping("/reservas")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<List<SolicitudReservaRecursoDTO>> findByEstado(
            @RequestParam("estado")EstadoSolicitud estado
            ){

        return solicitudReservaService.findByEstado(estado);
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<SolicitudReservaRecursoDTO> aprobar(@PathVariable Long id){
        return solicitudReservaService.aprobar(id);
    }

    @PutMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<SolicitudReservaRecursoDTO> rechazar(@PathVariable Long id){
        return solicitudReservaService.rechazar(id);
    }
}
