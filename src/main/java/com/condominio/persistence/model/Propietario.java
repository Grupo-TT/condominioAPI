package com.condominio.persistence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Getter
@Service
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Propietario extends Persona {
    @OneToOne
    private UserEntity user;
    @Enumerated(EnumType.STRING)
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private long telefono;
    private Boolean estado;
}
