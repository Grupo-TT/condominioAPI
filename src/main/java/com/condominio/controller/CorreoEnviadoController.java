package com.condominio.controller;

import com.condominio.persistence.model.CorreoEnviado;
import com.condominio.persistence.repository.CorreoEnviadoRepository;
import com.condominio.service.implementation.CorreoEnviadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("comunicados")
public class CorreoEnviadoController {

    private final CorreoEnviadoService correoEnviadoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<CorreoEnviado> findAll() {
        return correoEnviadoService.findAll();
    }
}
