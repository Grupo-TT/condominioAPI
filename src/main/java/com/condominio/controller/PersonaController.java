package com.condominio.controller;

import com.condominio.dto.request.PersonaRegistroDTO;
import com.condominio.dto.request.PersonaUpdateDTO;
import com.condominio.dto.response.PersonaPerfilDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Persona;
import com.condominio.service.interfaces.IPersonaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/persona")
public class PersonaController {

    private final IPersonaService personaService;


    public PersonaController(IPersonaService personaService) {
        this.personaService = personaService;

    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResult<PersonaRegistroDTO>> createPersona(
            @RequestBody PersonaRegistroDTO personaRegistro) {

        personaService.save(personaRegistro);

        SuccessResult<PersonaRegistroDTO> response =
                new SuccessResult<>("Persona creada exitosamente", personaRegistro);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/perfil")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPIETARIO', 'ARRENDATARIO')")
    public ResponseEntity<PersonaPerfilDTO> getPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        PersonaPerfilDTO perfil = personaService.getPersonaPerfil(userDetails);
        return ResponseEntity.ok(perfil);
    }
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPIETARIO', 'ARRENDATARIO')")
    public ResponseEntity<SuccessResult<Void>> updatePersona(
            @Valid @RequestBody PersonaUpdateDTO personaUpdateDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(personaService.updatePersona(personaUpdateDTO, userDetails));
    }
}
