package com.condominio.controller;

import com.condominio.dto.request.MultaActualizacionDTO;
import com.condominio.dto.request.MultaRegistroDTO;
import com.condominio.dto.request.RecursoComunDTO;
import com.condominio.dto.response.EstadoCuentaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Obligacion;
import com.condominio.persistence.model.RecursoComun;
import com.condominio.service.interfaces.IObligacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("obligacion")
public class ObligacionController {
    private final IObligacionService obligacionService;

    @GetMapping("/{idCasa}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<EstadoCuentaDTO> obtenerObligaciones(@PathVariable Long idCasa) {
        return obligacionService.estadoDeCuentaCasa(idCasa);
    }

    @GetMapping("/paz-y-salvo/{idCasa}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> descargarPazYSalvo(@PathVariable Long idCasa) {
        return obligacionService.generarPazYSalvo(idCasa);

    }

    @PostMapping("/multa/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResult<Obligacion>> create(
            @RequestBody MultaRegistroDTO multa) {
        SuccessResult<Obligacion> result = obligacionService.save(multa);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/multa/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResult<Obligacion>> edit(
            @PathVariable Long id,
            @RequestBody MultaActualizacionDTO multa) {

        SuccessResult<Obligacion> result = obligacionService.update(id, multa);
        return ResponseEntity.ok(result);
    }
}
