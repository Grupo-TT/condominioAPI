package com.condominio.dto.request;

import com.condominio.persistence.model.TipoRecursoComun;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecursoComunDTO {

    private String nombre;
    private String descripcion;
    private TipoRecursoComun recursoComun;
}
