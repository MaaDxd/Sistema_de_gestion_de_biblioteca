package com.biblioteca.persistencia;

import com.biblioteca.datastructures.ListaEnlazada;
import com.biblioteca.modelo.Libro;
import com.biblioteca.modelo.Prestamo;
import com.biblioteca.modelo.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servicio de persistencia simple: serializa y deserializa las estructuras
 * de datos a/desde archivos JSON en disco.
 *
 * Al arrancar la app (@PostConstruct en HistorialServicio) se cargan los datos.
 * Al apagar la app (@PreDestroy) se guardan.
 *
 * Ruta de los archivos: ./data/libros.json, ./data/usuarios.json, ./data/prestamos.json
 */
@Service
public class PersistenciaServicio {

    /** Directorio donde se guardan los archivos JSON */
    private static final Path DIRECTORIO_DATOS = Paths.get("data");

    private static final File ARCHIVO_LIBROS    = DIRECTORIO_DATOS.resolve("libros.json").toFile();
    private static final File ARCHIVO_USUARIOS  = DIRECTORIO_DATOS.resolve("usuarios.json").toFile();
    private static final File ARCHIVO_PRESTAMOS = DIRECTORIO_DATOS.resolve("prestamos.json").toFile();

    /** Jackson mapper para serialización/deserialización */
    private final ObjectMapper mapper;

    public PersistenciaServicio() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Ignorar propiedades desconocidas al leer (compatibilidad hacia adelante)
        this.mapper.configure(
            com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
        );
    }

    // ─────────────────────────────────────────────
    // GUARDAR
    // ─────────────────────────────────────────────

    /**
     * Persiste el catálogo de libros a libros.json.
     * Se llama al apagar la aplicación.
     */
    public void guardarLibros(ListaEnlazada<Libro> catalogo) {
        guardarArreglo(catalogo.aArreglo(), ARCHIVO_LIBROS, "libros");
    }

    /**
     * Persiste la lista de usuarios a usuarios.json.
     */
    public void guardarUsuarios(ListaEnlazada<Usuario> usuarios) {
        guardarArreglo(usuarios.aArreglo(), ARCHIVO_USUARIOS, "usuarios");
    }

    /**
     * Persiste el historial de préstamos a prestamos.json.
     */
    public void guardarPrestamos(ListaEnlazada<Prestamo> prestamos) {
        guardarArreglo(prestamos.aArreglo(), ARCHIVO_PRESTAMOS, "préstamos");
    }

    // ─────────────────────────────────────────────
    // CARGAR
    // ─────────────────────────────────────────────

    /**
     * Carga los libros desde libros.json a la lista enlazada del catálogo.
     * Si el archivo no existe, no hace nada (primera ejecución).
     */
    public void cargarLibros(ListaEnlazada<Libro> catalogo) {
        if (!ARCHIVO_LIBROS.exists()) return;
        try {
            Libro[] libros = mapper.readValue(ARCHIVO_LIBROS, Libro[].class);
            for (Libro l : libros) {
                catalogo.insertarAlFinal(l);
            }
            System.out.println("✔ Cargados " + libros.length + " libros desde disco.");
        } catch (IOException e) {
            System.err.println("⚠ No se pudo cargar libros.json: " + e.getMessage());
        }
    }

    /**
     * Carga los usuarios desde usuarios.json.
     */
    public void cargarUsuarios(ListaEnlazada<Usuario> usuarios) {
        if (!ARCHIVO_USUARIOS.exists()) return;
        try {
            Usuario[] arr = mapper.readValue(ARCHIVO_USUARIOS, Usuario[].class);
            for (Usuario u : arr) {
                usuarios.insertarAlFinal(u);
            }
            System.out.println("✔ Cargados " + arr.length + " usuarios desde disco.");
        } catch (IOException e) {
            System.err.println("⚠ No se pudo cargar usuarios.json: " + e.getMessage());
        }
    }

    /**
     * Carga el historial de préstamos desde prestamos.json.
     */
    public void cargarPrestamos(ListaEnlazada<Prestamo> prestamos) {
        if (!ARCHIVO_PRESTAMOS.exists()) return;
        try {
            Prestamo[] arr = mapper.readValue(ARCHIVO_PRESTAMOS, Prestamo[].class);
            for (Prestamo p : arr) {
                prestamos.insertarAlFinal(p);
            }
            System.out.println("✔ Cargados " + arr.length + " préstamos desde disco.");
        } catch (IOException e) {
            System.err.println("⚠ No se pudo cargar prestamos.json: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // UTILIDADES INTERNAS
    // ─────────────────────────────────────────────

    private void guardarArreglo(Object[] datos, File archivo, String nombre) {
        try {
            // Crear el directorio si no existe
            Files.createDirectories(DIRECTORIO_DATOS);
            mapper.writeValue(archivo, datos);
            System.out.println("✔ Guardados " + datos.length + " " + nombre + " en disco.");
        } catch (IOException e) {
            System.err.println("⚠ No se pudo guardar " + nombre + ": " + e.getMessage());
        }
    }
}
