package com.condominio.controller;

import com.condominio.dto.request.MiembroRegistroDTO;
import com.condominio.dto.response.MiembrosDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.implementation.MiembroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole( 'PROPIETARIO', 'ARRENDATARIO')")
    public ResponseEntity<SuccessResult<Void>> crearMiembro(@RequestBody  MiembroRegistroDTO miembroRegistroDTO) {
        return ResponseEntity.ok(miembroService.crearMiembro(miembroRegistroDTO));
    }
}
