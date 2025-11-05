package com.condominio.controller;

import com.condominio.dto.request.CargoAdminUpdateDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.CargoAdministracion;
import com.condominio.service.interfaces.ICargoAdministracion;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("cargo-admin")
public class CargoAdministracionController {

    private final ICargoAdministracion cargoAdministracion;

    @PutMapping("/actualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<CargoAdministracion> actualizarCargoAdministracion(@RequestBody CargoAdminUpdateDTO dto){

        return cargoAdministracion.actualizarCargoAdministracion(dto.nuevoValor());
    }
}
