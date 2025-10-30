package com.condominio.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaDTO {
    private String nombreRecurso;
    private LocalDate fechaReserva;
}
