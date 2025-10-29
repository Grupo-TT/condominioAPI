package com.condominio.controller;

import com.condominio.dto.response.ReservaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.IReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reserva")
@RequiredArgsConstructor
public class ReservaController {

    private final IReservaService reservaService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
    public ResponseEntity<SuccessResult<List<ReservaDTO>>> reservas(){
        List<ReservaDTO> reservas = reservaService.findAllProximas();
        SuccessResult<List<ReservaDTO>> response =
                new SuccessResult<>("Reservas obtenidas correctamente", reservas);

        return ResponseEntity.ok(response);
    }
}
