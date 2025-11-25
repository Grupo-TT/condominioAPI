package com.condominio.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PersonaSimpleRolDTO extends PersonaSimpleDTO   {
    private List<String> roles;
}
