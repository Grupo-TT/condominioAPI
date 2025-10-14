package com.condominio.controller;

import com.condominio.dto.request.RecursoComunDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.RecursoComun;
import com.condominio.service.interfaces.IRecursoComunService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recurso")
@RequiredArgsConstructor
public class RecursoComunController {

    private final IRecursoComunService recursoComunService;

    @GetMapping("/all")
    public ResponseEntity<SuccessResult<List<RecursoComun>>> findAll(){
        List<RecursoComun> recursos = recursoComunService.findAll();
        SuccessResult<List<RecursoComun>> response =
                new SuccessResult<>("Recursos  comunes obtenidos exitosamente", recursos);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<SuccessResult<RecursoComun>> create(
            @RequestBody RecursoComunDTO recursoComun) {
        SuccessResult<RecursoComun> result = recursoComunService.save(recursoComun);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    @PutMapping("/edit/{id}")
    public ResponseEntity<SuccessResult<RecursoComun>> edit(
            @PathVariable Long id,
            @RequestBody RecursoComunDTO recursoComun) {

        SuccessResult<RecursoComun> result = recursoComunService.update(id, recursoComun);
        return ResponseEntity.ok(result);

    }

    @PutMapping("/enable/{id}")
    public ResponseEntity<SuccessResult<RecursoComun>> habilitar(
            @PathVariable Long id) {
        SuccessResult<RecursoComun> result = recursoComunService.habilitar(id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/disable/{id}")
    public ResponseEntity<SuccessResult<RecursoComun>> deshabilitar(
            @PathVariable Long id) {
        SuccessResult<RecursoComun> result = recursoComunService.deshabilitar(id);
        return ResponseEntity.ok(result);
    }

}
