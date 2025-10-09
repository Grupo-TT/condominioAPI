package com.condominio.controller;

import com.condominio.dto.request.AuthRequest;
import com.condominio.dto.response.AuthResponse;
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



@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(
                    request.username(), request.password()
            );

            Authentication auth = authenticationManager.authenticate(authToken);
            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            String token = jwtUtil.generateAccessToken(userDetails);

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception ex) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }
}
