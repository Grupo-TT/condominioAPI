package com.condominio.controller;

import com.condominio.dto.request.MovimientoRequestDTO;
import com.condominio.dto.response.MovimientoDTO;
import com.condominio.dto.response.MovimientosMesDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.IMovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovimientoController {
    private final IMovimientoService movimientoService;

    @GetMapping("/por-mes")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<MovimientosMesDTO> obtenerMovimientosPorMes(@RequestParam int mes, @RequestParam("año") int anio){
        return movimientoService.getMovimientosPorMes(mes, anio);
    }

    @PostMapping("crear-movimiento")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<MovimientoDTO> crearMovimiento(@RequestBody MovimientoRequestDTO movimientoRequestDTO){
        return movimientoService.crearMovimiento(movimientoRequestDTO);
    }

    @PutMapping("edit-movimiento/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<MovimientoDTO> actualizarMovimiento(@PathVariable Long id, @RequestBody MovimientoRequestDTO movimientoRequestDTO){
        return movimientoService.actualizarMovimiento(id, movimientoRequestDTO);
    }

    @DeleteMapping("eliminar-movimiento/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<Void> eliminarMovimiento(@PathVariable Long id){
        return movimientoService.eliminarMovimiento(id);
    }
}
