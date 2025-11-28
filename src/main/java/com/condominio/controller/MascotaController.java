package com.condominio.controller;

import com.condominio.dto.request.MascotaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.IMascotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mascota")
@RequiredArgsConstructor
public class MascotaController {

    private final IMascotaService mascotaService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole( 'PROPIETARIO', 'ARRENDATARIO')")
    public SuccessResult<Void> agregarMascota(@RequestBody MascotaDTO mascotaDTO) {
        return mascotaService.addMascota(mascotaDTO);
    }
    @PutMapping("/subtract")
    @PreAuthorize("hasAnyRole( 'PROPIETARIO', 'ARRENDATARIO')")
    public SuccessResult<Void> modificarCantidadMascota(@RequestBody MascotaDTO mascotaDTO) {
        return mascotaService.subtractMascota(mascotaDTO);
    }
}
