package com.condominio.controller;

import com.condominio.dto.request.MiembroRegistroDTO;
import com.condominio.dto.response.MiembrosDTO;
import com.condominio.dto.response.MiembrosDatosDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Persona;
import com.condominio.service.implementation.MiembroService;
import com.condominio.service.implementation.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("miembros")
@RequiredArgsConstructor
public class MiembroController {

    private final MiembroService miembroService;
    private final PersonaService personaService;

    @GetMapping("/view-members/{idCasa}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResult<List<MiembrosDTO>> obtenerMiembrosPorCasa(@PathVariable Long idCasa) {

        return miembroService.obtenerMiembrosPorCasa(idCasa);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole( 'PROPIETARIO', 'ARRENDATARIO')")
    public ResponseEntity<SuccessResult<Void>> crearMiembro(@RequestBody  MiembroRegistroDTO miembroRegistroDTO) {
        return ResponseEntity.ok(miembroService.crearMiembro(miembroRegistroDTO));
    }
    @PutMapping("/{idMiembro}/edit")
    @PreAuthorize("hasAnyRole( 'PROPIETARIO', 'ARRENDATARIO')")
    public ResponseEntity<SuccessResult<Void>> actualizarMiembro(
            @PathVariable Long idMiembro,
            @RequestBody MiembrosDatosDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long casaUsuarioId = obtenerCasaId(userDetails);
        return ResponseEntity.ok(miembroService.actualizarMiembro(idMiembro, dto,casaUsuarioId));
    }

    @GetMapping("/all-casa-members")
    @PreAuthorize("hasAnyRole( 'PROPIETARIO', 'ARRENDATARIO')")
    public ResponseEntity<List<MiembrosDatosDTO>> obtenerMiembros(@AuthenticationPrincipal UserDetails userDetails) {

        List<MiembrosDatosDTO> miembros = miembroService.listarMiembrosPorCasa(obtenerCasaId(userDetails));
        return ResponseEntity.ok(miembros);
    }
    @PatchMapping("/{idMiembro}/edit-estado")
    @PreAuthorize("hasAnyRole( 'PROPIETARIO', 'ARRENDATARIO')")
    public ResponseEntity<SuccessResult<Void>> toggleEstado(@PathVariable Long idMiembro
            ,@AuthenticationPrincipal UserDetails userDetails) {

        Long casaUsuarioId = obtenerCasaId(userDetails);
        SuccessResult<Void> result = miembroService.ActualizarEstadoMiembro(idMiembro,casaUsuarioId);
        return ResponseEntity.ok(result);
    }

    private Long obtenerCasaId(UserDetails userDetails) {
        Persona persona = personaService.getPersonaFromUserDetails(userDetails);
        return persona.getCasa().getId();
    }

}
