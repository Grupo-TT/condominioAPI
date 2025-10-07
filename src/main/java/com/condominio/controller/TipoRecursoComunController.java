package com.condominio.controller;


import com.condominio.dto.request.TipoRecursoComunDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.TipoRecursoComun;
import com.condominio.service.interfaces.ITipoRecursoComun;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Recurso")
@RequiredArgsConstructor
public class TipoRecursoComunController {

    private final ITipoRecursoComun tipoRecursoComunService;

    @GetMapping("/All")
    public ResponseEntity<SuccessResult<List<TipoRecursoComun>>> findAll() {
        List<TipoRecursoComun> recursos = tipoRecursoComunService.findAll();

        SuccessResult<List<TipoRecursoComun>> response =
                new SuccessResult<>("Recursos obtenidos exitosamente", recursos);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/Create")
    public ResponseEntity<SuccessResult<TipoRecursoComunDTO>> create(
            @RequestBody TipoRecursoComunDTO tipoRecurso) {

        SuccessResult<TipoRecursoComunDTO> result = tipoRecursoComunService.save(tipoRecurso);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}

