package com.condominio.dto.response;

import java.util.List;

public record UserResponse(
        String email,
        String nombre,
        List<String> roles
) {
}
