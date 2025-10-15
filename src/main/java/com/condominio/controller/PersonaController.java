package com.condominio.controller;

import com.condominio.dto.request.PersonaRegistroDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.IPersonaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/persona")
public class PersonaController {

    private final IPersonaService personaService;


    public PersonaController(IPersonaService personaService) {
        this.personaService = personaService;

    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN','PROPIETARIO')")
    public ResponseEntity<SuccessResult<PersonaRegistroDTO>> createPersona(
            @RequestBody PersonaRegistroDTO personaRegistro) {

        personaService.save(personaRegistro);

        SuccessResult<PersonaRegistroDTO> response =
                new SuccessResult<>("Persona creada exitosamente", personaRegistro);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}
