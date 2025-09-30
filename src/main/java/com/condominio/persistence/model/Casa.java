package com.condominio.persistence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Getter
@Service
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Casa {
    @Id
    private Long id;
    private int numeroCasa;
    @ManyToOne
    private Propietario propietario;
    @OneToOne
    private Arrendatario arrendatario;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Miembro> miembros;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Mascota> mascotas;

}
