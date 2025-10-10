package com.condominio.dto.response;

public record AuthResponse(
        String token,
        String refreshToken,
        UserResponse user) {
}
