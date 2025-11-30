package com.condominio.controller;

import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.CorreoEnviado;
import com.condominio.service.implementation.CorreoEnviadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<Void> delete(@PathVariable Long id) {
        return correoEnviadoService.delete(id);
    }
}
