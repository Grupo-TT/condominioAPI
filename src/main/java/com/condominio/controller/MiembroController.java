package com.condominio.controller;

import com.condominio.dto.response.MiembrosDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.implementation.MiembroService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("miembros")
@RequiredArgsConstructor
public class MiembroController {

    private final MiembroService miembroService;

    @GetMapping("/view-members/{idCasa}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<List<MiembrosDTO>> obtenerMiembrosPorCasa(@PathVariable Long idCasa) {

        return miembroService.obtenerMiembrosPorCasa(idCasa);
    }
}
