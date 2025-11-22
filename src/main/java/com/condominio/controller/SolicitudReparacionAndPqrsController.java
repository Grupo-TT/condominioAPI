package com.condominio.controller;

import com.condominio.dto.request.SolicitudReparacionUpdateDTO;
import com.condominio.dto.response.*;
//import com.condominio.persistence.model.EstadoPqrs;
import com.condominio.persistence.model.EstadoSolicitud;
//import com.condominio.service.interfaces.IDetallePqrs;
import com.condominio.service.interfaces.ISolicitudReparacionLocativaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("reparaciones-y-pqrs")
@RequiredArgsConstructor
public class SolicitudReparacionAndPqrsController {

    private final ISolicitudReparacionLocativaService solicitudReparacionLocativaService;
    //private final IDetallePqrs detallePqrsService;

    //----
    //Endpoints para Solicitudes de Reparaciones
    //----
    @GetMapping("/solicitudes-reparacion")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<List<SolicitudReparacionLocativaDTO>> obtenerSolicitudesReparacion(
            @RequestParam ("estado")EstadoSolicitud estado){
        return solicitudReparacionLocativaService.findByEstado(estado);
    }

    @PutMapping("/approve-solicitud-reparacion/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<SolicitudReparacionLocativaDTO> aprobar(@PathVariable Long id){
        return solicitudReparacionLocativaService.aprobar(id);
    }

    @PutMapping("/reject-solicitud-reparacion/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<SolicitudReparacionLocativaDTO> rechazar(@PathVariable Long id , @RequestParam("comentarios") String comentarios){
        return solicitudReparacionLocativaService.rechazar(id, comentarios);
    }

    @PutMapping("/edit-solicitud-reparacion/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<SolicitudReparacionUpdateDTO> updateSolicitud(@PathVariable Long id, @RequestBody SolicitudReparacionUpdateDTO solicitud){
        return solicitudReparacionLocativaService.update(id, solicitud);
    }

    @PostMapping("/crear-solicitud-reparacion")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
    public SuccessResult<SolicitudReparacionPropiDTO> crearSolicitud(@RequestBody SolicitudReparacionPropiDTO solicitudDTO){
        return solicitudReparacionLocativaService.crearSolicitud(solicitudDTO);
    }

    @PutMapping("/modificar-solicitud-reparacion/{id}")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
    public SuccessResult<SolicitudReparacionPropiDTO> modificarSolicitud(@PathVariable Long id, @RequestBody SolicitudReparacionPropiDTO solicitudDTO){
        return solicitudReparacionLocativaService.modificarSolicitud(id, solicitudDTO);
    }

    @DeleteMapping("/eliminar-solicitud-reparacion/{id}")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
    public SuccessResult<SolicitudReparacionLocativaDTO> eliminar(@PathVariable Long id){
        return solicitudReparacionLocativaService.eliminar(id);
    }

    //----
    //Endpoints para PQRS
    //----
//    @GetMapping("/all-pqrs")
//    @PreAuthorize("hasRole('ADMIN')")
//    public SuccessResult<List<DetallePqrsDTO>> obtenerDetallesPqrs(@RequestParam ("estado") EstadoPqrs estado){
//        return detallePqrsService.findByEstado(estado);
//    }
//
//    @PutMapping("/edit-pqrs/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public SuccessResult<DetallePqrsDTO> updateDetallePqrs(@PathVariable Long id, @RequestBody DetallePqrsDTO detallePqrs){
//        return detallePqrsService.update(id, detallePqrs);
//    }
//
//    @PutMapping("/marcar-revisada-pqrs/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public SuccessResult<DetallePqrsDTO> marcarRevisada(@PathVariable Long id){
//        return detallePqrsService.marcarRevisada(id);
//    }
//
//    @DeleteMapping("/delete-pqrs/{id}")
//    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
//    public SuccessResult<DetallePqrsDTO> eliminarPqrs(@PathVariable Long id){
//        return detallePqrsService.eliminar(id);
//    }
//
//    @PostMapping("/crear-pqrs")
//    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
//    public SuccessResult<DetallePqrsPropiDTO> crearPqrs(@RequestBody DetallePqrsPropiDTO detallePqrs){
//        return detallePqrsService.crearDetallePqrs(detallePqrs);
//    }
//
//    @PutMapping("/modificar-pqrs/{id}")
//    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ARRENDATARIO')")
//    public SuccessResult<DetallePqrsPropiDTO> modificarDetallePqrs(@PathVariable Long id, @RequestBody DetallePqrsPropiDTO detallePqrs){
//        return detallePqrsService.modificarDetallePqrs(id, detallePqrs);
//    }
//
}
