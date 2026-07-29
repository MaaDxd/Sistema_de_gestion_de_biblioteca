package com.biblioteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal del Sistema de Gestión de Biblioteca.
 *
 * Estructuras de datos implementadas manualmente:
 * - ListaEnlazada<T>   → Catálogo de libros y registro de usuarios
 * - ColaPrestamos<T>   → Cola de espera FIFO para préstamos
 * - PilaHistorial<T>   → Historial de acciones LIFO / "deshacer"
 */
@SpringBootApplication
public class BibliotecaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotecaApplication.class, args);
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  Sistema de Gestión de Biblioteca         ║");
        System.out.println("║  API REST corriendo en puerto 8080        ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }
}
