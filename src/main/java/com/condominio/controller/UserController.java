package com.condominio.controller;

import com.condominio.dto.request.PasswordUpdateDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.service.interfaces.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final IUserService userService;


    @PutMapping("/update-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPIETARIO', 'ARRENDATARIO')")
    public ResponseEntity<SuccessResult<Void>> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        return ResponseEntity.ok(userService.changePassword(userDetails,passwordUpdateDTO));
    }

    @PostMapping("/recuperar-password")
    public SuccessResult<String> recuperarPassword(@RequestParam String email) {
        userService.recuperarPassword(email);
        return new SuccessResult<>(
                "La contraseña temporal fue enviada al correo.",
                email
        );
    }
}
