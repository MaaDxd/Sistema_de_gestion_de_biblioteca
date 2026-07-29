package com.biblioteca.config;

import com.biblioteca.datastructures.PilaHistorial;
import com.biblioteca.modelo.AccionHistorial;
import com.biblioteca.persistencia.PersistenciaServicio;
import com.biblioteca.servicio.LibroServicio;
import com.biblioteca.servicio.PrestamoServicio;
import com.biblioteca.servicio.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Componente que gestiona el ciclo de vida de la aplicación:
 * - @PostConstruct: inyecta la pila en servicios y carga datos desde disco.
 * - @PreDestroy:    persiste datos a disco al apagar.
 *
 * Separado de HistorialServicio para evitar dependencias circulares:
 * este componente conoce a todos los servicios, pero ningún servicio
 * lo conoce a él.
 */
@Component
public class InicializadorServicio {

    @Autowired private LibroServicio          libroServicio;
    @Autowired private UsuarioServicio        usuarioServicio;
    @Autowired private PrestamoServicio       prestamoServicio;
    @Autowired private PersistenciaServicio   persistenciaServicio;
    @Autowired private PilaHistorial<AccionHistorial> pilaHistorial;

    @PostConstruct
    public void inicializar() {
        // 1. Inyectar la pila singleton en los servicios que la necesitan
        libroServicio.setPilaHistorial(pilaHistorial);
        usuarioServicio.setPilaHistorial(pilaHistorial);

        // 2. Cargar datos desde disco (persistencia — punto 1)
        libroServicio.cargarDesde(persistenciaServicio);
        usuarioServicio.cargarDesde(persistenciaServicio);
        prestamoServicio.cargarDesde(persistenciaServicio);

        System.out.println("✔ Sistema inicializado. Datos cargados desde disco.");
    }

    @PreDestroy
    public void alApagar() {
        libroServicio.guardarEn(persistenciaServicio);
        usuarioServicio.guardarEn(persistenciaServicio);
        prestamoServicio.guardarEn(persistenciaServicio);
        System.out.println("✔ Datos guardados en disco correctamente.");
    }
}
