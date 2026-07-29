package com.biblioteca.config;

import com.biblioteca.datastructures.PilaHistorial;
import com.biblioteca.modelo.AccionHistorial;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean singleton de la PilaHistorial compartida.
 * Al ser un @Bean independiente, cualquier servicio puede inyectarlo
 * sin crear dependencias circulares entre servicios.
 */
@Configuration
public class PilaHistorialBean {

    @Bean
    public PilaHistorial<AccionHistorial> pilaHistorial() {
        return new PilaHistorial<>();
    }
}
