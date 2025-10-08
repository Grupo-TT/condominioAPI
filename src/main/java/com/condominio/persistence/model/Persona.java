package com.condominio.persistence.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    @Enumerated(EnumType.STRING)
    private TipoDocumento tipoDocumento;
    private Long numeroDocumento;
    private Long telefono;
    @OneToOne
    private UserEntity user;
    private Boolean estado;
    private Boolean junta;
    private Boolean comiteConvivencia;
    @ManyToOne
    private Casa casa;
}
