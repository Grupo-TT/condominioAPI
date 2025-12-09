package com.condominio.controller;

import com.condominio.dto.request.AsambleaDTO;
import com.condominio.dto.response.AsambleaConAsistenciaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Asamblea;
import com.condominio.service.interfaces.IAsambleaService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asamblea")
@RequiredArgsConstructor
public class AsambleaController {

    private final IAsambleaService asambleaService;


    @PostMapping("/crear")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<AsambleaDTO> create(@RequestBody AsambleaDTO asamblea) {

        return asambleaService.create(asamblea);
    }

    @GetMapping()
    @PreAuthorize("hasAnyRole( 'PROPIETARIO', 'ARRENDATARIO' , 'ADMIN')")
    public SuccessResult<List<Asamblea>> obtenerAsambleas() {
        return asambleaService.findAllAsambleas();
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<AsambleaDTO> edit(@RequestBody AsambleaDTO asamblea, @PathVariable Long id) {
        return asambleaService.edit(asamblea, id);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<Void> delete(@PathVariable Long id) {
        return asambleaService.delete(id);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<AsambleaConAsistenciaDTO> getAsambleaById(@PathVariable Long id){
        return asambleaService.getAsambleaById(id);
    }

    @PutMapping("/cambiar-estado/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<Void> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        return asambleaService.cambiarEstado(id, estado);
    }
}
