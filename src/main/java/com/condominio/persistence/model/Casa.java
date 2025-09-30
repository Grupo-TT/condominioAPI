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
public class Casa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int numeroCasa;
    @ManyToOne
    private Propietario propietario;
    @OneToOne
    private Arrendatario arrendatario;

}
