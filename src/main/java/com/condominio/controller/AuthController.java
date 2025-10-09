package com.condominio.controller;

import com.condominio.dto.request.AuthRequest;
import com.condominio.dto.response.AuthResponse;
import com.condominio.dto.response.UserResponse;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.service.implementation.UserService;
import com.condominio.util.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(
                    request.username(), request.password()
            );

            Authentication auth = authenticationManager.authenticate(authToken);
            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            String token = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            UserEntity userEntity = userService.findByEmail(userDetails.getUsername());

            String nombre = null;
            if (userEntity != null) {
                Persona personaOpt = userService.findPersonaByUser(userEntity);
                    nombre = buildNombreCompleto(personaOpt);
                }

            List<String> roles = userEntity.getRoles().stream()
                    .map(r -> r.getRoleEnum().name())
                    .collect(Collectors.toList());

            var userResponse = new UserResponse(
                    userDetails.getUsername(),
                    nombre,
                    roles
            );

            return ResponseEntity.ok(new AuthResponse(token, refreshToken,userResponse));
        } catch (Exception ex) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

    }

    private static String buildNombreCompleto(Persona p) {

        return Stream.of(p.getPrimerNombre(), p.getSegundoNombre(),
                        p.getPrimerApellido(), p.getSegundoApellido())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }
}
