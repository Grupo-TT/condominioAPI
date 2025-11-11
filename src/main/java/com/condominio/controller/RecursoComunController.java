package com.condominio.controller;

import com.condominio.dto.request.RecursoComunDTO;
import com.condominio.dto.response.RecursoComunPropiDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.DisponibilidadRecurso;
import com.condominio.persistence.model.RecursoComun;
import com.condominio.service.interfaces.IRecursoComunService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recurso")
@RequiredArgsConstructor
public class RecursoComunController {

    private final IRecursoComunService recursoComunService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResult<List<RecursoComun>>> findAll(){
        List<RecursoComun> recursos = recursoComunService.findAll();
        SuccessResult<List<RecursoComun>> response =
                new SuccessResult<>("Recursos  comunes obtenidos exitosamente", recursos);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResult<RecursoComun>> create(
            @RequestBody RecursoComunDTO recursoComun) {
        SuccessResult<RecursoComun> result = recursoComunService.save(recursoComun);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    @PutMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResult<RecursoComun>> edit(
            @PathVariable Long id,
            @RequestBody RecursoComunDTO recursoComun) {

        SuccessResult<RecursoComun> result = recursoComunService.update(id, recursoComun);
        return ResponseEntity.ok(result);

    }

    @PutMapping("/change-availability/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResult<RecursoComun>> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam("disponibilidad") DisponibilidadRecurso disponibilidad) {
        SuccessResult<RecursoComun> result = recursoComunService.cambiarDisponibilidad(id, disponibilidad);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all-public")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
    public ResponseEntity<SuccessResult<List<RecursoComunPropiDTO>>> recursosPublic(){
        List<RecursoComunPropiDTO> recursos = recursoComunService.findByDisponibilidad();
        SuccessResult<List<RecursoComunPropiDTO>> response =
                new SuccessResult<>("Recursos  comunes obtenidos exitosamente", recursos);

        return ResponseEntity.ok(response);
    }
}
