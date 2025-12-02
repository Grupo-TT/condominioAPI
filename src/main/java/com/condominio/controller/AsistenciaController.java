package com.condominio.controller;

import com.condominio.dto.request.AsistenciaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.IAsistenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asistencia")
@RequiredArgsConstructor
public class AsistenciaController {


    private final IAsistenciaService asistenciaService;

    @PostMapping("registrar/{idAsamblea}")
    @PreAuthorize("hasRole('ADMIN')")
    SuccessResult<Void> registrarAsistencia(@PathVariable Long idAsamblea, @RequestBody AsistenciaDTO asistencia){
        return asistenciaService.registrarAsistencia(idAsamblea, asistencia);
    }
}
