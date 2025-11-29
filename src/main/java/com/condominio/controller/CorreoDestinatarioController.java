package com.condominio.controller;

import com.condominio.dto.response.DestinatarioInfoDTO;
import com.condominio.service.implementation.CorreoDestinatarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("destinatarios")
public class CorreoDestinatarioController {

    private final CorreoDestinatarioService correoDestinatarioService;

    @GetMapping("/{idCorreo}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DestinatarioInfoDTO> findDestinatariosByCorreo(@PathVariable Long idCorreo) {
        return correoDestinatarioService.getDestinatariosInfo(idCorreo);
    }
}
