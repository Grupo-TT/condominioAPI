package com.condominio.controller;

import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.ISolicitudReservaRecursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/solicitud-recurso")
@RequiredArgsConstructor
public class SolicitudReservaRecursoController {
    private final ISolicitudReservaRecursoService solicitudReservaService;


    @GetMapping("/pendientes")
    public SuccessResult<List<SolicitudReservaRecursoDTO>> findPendientes(){

        return solicitudReservaService.findPendientes();
    }
}
