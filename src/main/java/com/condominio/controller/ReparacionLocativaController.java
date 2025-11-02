package com.condominio.controller;

import com.condominio.dto.response.ReparacionLocativaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.IReparacionLocativaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("reparacion-locativa")
@RequiredArgsConstructor
public class ReparacionLocativaController {

    private final IReparacionLocativaService reparacionLocativaService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResult<List<ReparacionLocativaDTO>>> findAll(){
        List<ReparacionLocativaDTO> reparaciones = reparacionLocativaService.findAll();
        SuccessResult<List<ReparacionLocativaDTO>> response =
                new SuccessResult<>("Reparaciones locativas obtenidas exitosamente", reparaciones);

        return ResponseEntity.ok(response);
    }
}
