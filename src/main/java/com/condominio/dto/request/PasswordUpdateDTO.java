package com.condominio.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data

public class PasswordUpdateDTO {

    private String currentPassword;
    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    private String newPassword;

    @NotBlank(message = "La confirmación de la nueva contraseña no puede estar vacía")
    private String confirmPassword;
}
