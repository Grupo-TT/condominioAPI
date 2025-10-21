package com.condominio.controller;

import com.condominio.dto.request.AsambleaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.IAsambleaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/asamblea")
@RequiredArgsConstructor
public class AsambleaController {

    private final IAsambleaService asambleaService;


    @PostMapping("/crear")
    public SuccessResult<AsambleaDTO> create(@RequestBody AsambleaDTO asamblea) {

        return asambleaService.create(asamblea);
    }
}
