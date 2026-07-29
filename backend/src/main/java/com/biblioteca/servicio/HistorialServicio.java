package com.biblioteca.servicio;

import com.biblioteca.datastructures.PilaHistorial;
import com.biblioteca.modelo.AccionHistorial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio que expone la PilaHistorial a los controladores.
 * La pila es un @Bean singleton definido en PilaHistorialBean,
 * así todos los servicios la inyectan directamente sin ciclos.
 */
@Service
public class HistorialServicio {

    /** Pila singleton inyectada desde PilaHistorialBean */
    @Autowired
    private PilaHistorial<AccionHistorial> pilaHistorial;

    public PilaHistorial<AccionHistorial> getPilaHistorial() { return pilaHistorial; }

    public Object[] obtenerHistorial()               { return pilaHistorial.aArreglo(); }

    public AccionHistorial desapilarUltimaAccion()   { return pilaHistorial.desapilar(); }

    public AccionHistorial verUltimaAccion()         { return pilaHistorial.verCima(); }

    public int contarAcciones()                      { return pilaHistorial.getTamanio(); }
}
