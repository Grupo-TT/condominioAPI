package com.condominio.controller;

import com.condominio.dto.response.*;
import com.condominio.dto.request.SolicitudReservaUpdateDTO;
import com.condominio.dto.response.*;
import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.service.interfaces.ISolicitudReservaRecursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/solicitud-recurso")
@RequiredArgsConstructor
public class SolicitudReservaRecursoController {
    private final ISolicitudReservaRecursoService solicitudReservaService;


    @GetMapping("/reservas")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO', 'ADMIN')")
    public SuccessResult<List<SolicitudReservaRecursoDTO>> findByEstado(
            @RequestParam("estado")EstadoSolicitud estado
            ){

        return solicitudReservaService.findByEstado(estado);
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<SolicitudReservaRecursoDTO> aprobar(@PathVariable Long id){
        return solicitudReservaService.aprobar(id);
    }

    @PutMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<SolicitudReservaRecursoDTO> rechazar(@PathVariable Long id){
        return solicitudReservaService.rechazar(id);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public  SuccessResult<Void> eliminar(@PathVariable Long id){
        return solicitudReservaService.deleteSolicitud(id);
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<SolicitudReservaUpdateDTO> update(@PathVariable Long id, @RequestBody SolicitudReservaUpdateDTO solicitud){
        return solicitudReservaService.update(id, solicitud);
    }

    @PostMapping("/crear")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
    public SuccessResult<SolicitudRecursoPropiDTO> crearSolicitud(@RequestBody SolicitudRecursoPropiDTO solicitudDTO){
        return solicitudReservaService.crearSolicitud(solicitudDTO);
    }

    @PutMapping("/invitados")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
    public SuccessResult<SolicitudRecursoPropiDTO> modificarCantidadInvitados(@RequestBody InvitadoDTO invitadoDTO){
        return solicitudReservaService.modificarCantidadInvitados(invitadoDTO);
    }

    @GetMapping("/mis-reservas/{id}")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
    public SuccessResult<List<SolicitudReservaDTO>> findAllByCasa(@PathVariable Long id){
        return solicitudReservaService.findReservasByCasa(id);
    }

    @DeleteMapping("/mis-reservas/delete/{id}")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO', 'ADMIN')")
    public SuccessResult<SolicitudReservaRecursoDTO> deleteReserva(@PathVariable Long id){
        return solicitudReservaService.cancelar(id);
    }

    @PutMapping("/mis-reservas/update")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
    public SuccessResult<SolicitudRecursoPropiDTO> findAllByPersona(@RequestBody SolicitudReservaUpdateDTO solicitudReservaUpdateDTO){
        return solicitudReservaService.actualizarSolicitud(solicitudReservaUpdateDTO);
    }
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO', 'ADMIN')")
    public ResponseEntity<SuccessResult<List<SolicitudReservaRecursoDTO>>> getAllSolicitudes() {
        SuccessResult<List<SolicitudReservaRecursoDTO>> result = solicitudReservaService.findAll();
        return ResponseEntity.ok(result);
    }

}
