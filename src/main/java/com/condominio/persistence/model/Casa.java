package com.condominio.persistence.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
    private Long id;
    @ManyToOne
    private Propietario propietario;
    @OneToOne
    private Arrendatario arrendatario;

}
