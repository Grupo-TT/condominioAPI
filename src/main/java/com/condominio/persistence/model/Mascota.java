package com.condominio.persistence.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private TipoMascota tipoMascota;
    @ManyToOne(targetEntity = Casa.class)
    private Casa casa;
}
