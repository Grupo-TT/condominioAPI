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
@RequestMapping("/Recurso")
@RequiredArgsConstructor
public class RecursoComunController {

    private final IRecursoComunService recursoComunService;

    @GetMapping("/All/Recursos")
    public ResponseEntity<SuccessResult<List<RecursoComun>>> findAll(){
        List<RecursoComun> recursos = recursoComunService.findAll();
        SuccessResult<List<RecursoComun>> response =
                new SuccessResult<>("Recursos obtenidos exitosamente", recursos);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/Create")
    public ResponseEntity<SuccessResult<RecursoComunDTO>> create(
            @RequestBody RecursoComunDTO recursoComun) {
        SuccessResult<RecursoComunDTO> result = recursoComunService.save(recursoComun);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
