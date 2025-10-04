package com.condominio.persistence.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
public class ReparacionLocativa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private boolean estado;

    @ManyToOne(targetEntity = SolicitudReparacionLocativa.class)
    @JoinColumn(nullable = false)
    private SolicitudReparacionLocativa solicitudReparacionLocativa;
}
