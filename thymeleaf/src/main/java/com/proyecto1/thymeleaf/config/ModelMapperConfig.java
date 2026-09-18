package com.proyecto1.thymeleaf.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conversion entre entidades JPA y DTOs.
 *
 * Se usa la estrategia STRICT a proposito: con la estrategia por defecto
 * ModelMapper adivina correspondencias por parecido de nombre y puede
 * escribir campos que nadie pidio (por ejemplo el id o el status de una
 * entidad). STRICT solo mapea lo que coincide exactamente.
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);
        return modelMapper;
    }
}
