package com.condominio.util.configuration;

import com.condominio.dto.request.PersonaRegistroDTO;
import com.condominio.persistence.model.Persona;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();


        modelMapper.addMappings(new PropertyMap<PersonaRegistroDTO, Persona>() {
            @Override
            protected void configure() {
                skip(destination.getId());
                skip(destination.getEstado());
                skip(destination.getJunta());
                skip(destination.getComiteConvivencia());
                skip(destination.getUser());
                skip(destination.getCasa());
            }
        });

        return modelMapper;
    }
}


