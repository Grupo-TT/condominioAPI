package com.condominio.dto.response;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonaSimpleDTO {
    private String nombreCompleto;
    private Long telefono;
    private String correo;
}
