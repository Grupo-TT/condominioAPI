package com.condominio.util.configuration;

import com.condominio.dto.request.PersonaRegistroDTO;
import com.condominio.dto.request.RecursoComunDTO;
import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.RecursoComun;
import com.condominio.persistence.model.SolicitudReservaRecurso;
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

        modelMapper.addMappings(new PropertyMap<RecursoComunDTO, RecursoComun>() {
            @Override
            protected void configure() {
                skip(destination.getId());
            }
        });

        modelMapper.addMappings(new PropertyMap<SolicitudReservaRecurso, SolicitudReservaRecursoDTO>() {
            @Override
            protected void configure() {

                skip(destination.getSolicitante());
            }
        });

        return modelMapper;
    }
}


