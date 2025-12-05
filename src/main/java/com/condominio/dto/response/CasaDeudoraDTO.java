package com.condominio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CasaDeudoraDTO {

    private int numeroCasa;
    private PersonaSimpleDTO propietario;
    private int saldoPendiente;
    private List<MostrarObligacionDTO> obligacionesPendientes;
    private LocalDate ultimoPago;
    private int interes;
}
