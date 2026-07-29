package com.biblioteca.servicio;

import com.biblioteca.datastructures.ListaEnlazada;
import com.biblioteca.datastructures.PilaHistorial;
import com.biblioteca.excepcion.OperacionInvalidaException;
import com.biblioteca.excepcion.RecursoNoEncontradoException;
import com.biblioteca.modelo.AccionHistorial;
import com.biblioteca.modelo.Libro;
import com.biblioteca.persistencia.PersistenciaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servicio para la gestión del catálogo de libros.
 *
 * PUNTO 2 — CONCURRENCIA COMPLETA:
 * Todos los métodos que leen/escriben el catálogo o modifican copias son
 * synchronized sobre el mismo monitor (this), incluyendo
 * decrementarCopiaDisponible e incrementarCopiaDisponible que antes
 * quedaban desprotegidos cuando se llamaban desde PrestamoServicio.
 *
 * PUNTO 1 — PERSISTENCIA:
 * cargarDesde(PersistenciaServicio) se llama al arrancar.
 * guardarEn(PersistenciaServicio) se llama al apagar.
 */
@Service
public class LibroServicio {

    /** Catálogo almacenado en lista enlazada manual */
    private final ListaEnlazada<Libro> catalogo = new ListaEnlazada<>();

    /** Pila inyectada por InicializadorServicio vía setter */
    private PilaHistorial<AccionHistorial> pilaHistorial;

    @Autowired
    private PersistenciaServicio persistenciaServicio;

    public void setPilaHistorial(PilaHistorial<AccionHistorial> pila) {
        this.pilaHistorial = pila;
    }

    // ─────────────────────────────────────────────
    // PERSISTENCIA
    // ─────────────────────────────────────────────

    /** Carga el catálogo desde disco al arrancar. */
    public synchronized void cargarDesde(PersistenciaServicio ps) {
        ps.cargarLibros(catalogo);
    }

    /** Persiste el catálogo a disco al apagar. */
    public synchronized void guardarEn(PersistenciaServicio ps) {
        ps.guardarLibros(catalogo);
    }

    // ─────────────────────────────────────────────
    // REGISTRAR
    // ─────────────────────────────────────────────

    /**
     * Registra un nuevo libro. Synchronized: evita duplicados ISBN bajo concurrencia.
     * @throws OperacionInvalidaException si el ISBN ya existe
     */
    public synchronized Libro registrarLibro(Libro libro) {
        if (catalogo.buscar(l -> l.getIsbn().equalsIgnoreCase(libro.getIsbn())) != null) {
            throw new OperacionInvalidaException(
                "Ya existe un libro con el ISBN: " + libro.getIsbn());
        }
        libro.setId("LIB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        libro.setCopiasDisponibles(libro.getCantidadCopias());
        if (libro.getEstado() == null || libro.getEstado().isBlank()) {
            libro.setEstado("DISPONIBLE");
        }
        catalogo.insertarAlFinal(libro);
        registrarAccion("REGISTRO_LIBRO",
            "Libro registrado: '" + libro.getTitulo() + "' (ISBN: " + libro.getIsbn() + ")",
            libro.getId());
        return libro;
    }

    // ─────────────────────────────────────────────
    // BÚSQUEDA
    // ─────────────────────────────────────────────

    public synchronized ListaEnlazada<Libro> buscarLibros(String termino) {
        String t = termino.toLowerCase();
        return catalogo.buscarTodos(l ->
            l.getTitulo().toLowerCase().contains(t) ||
            l.getAutor().toLowerCase().contains(t)  ||
            l.getIsbn().toLowerCase().contains(t));
    }

    /** Retorna el libro o null (uso interno en PrestamoServicio). */
    public synchronized Libro obtenerPorId(String id) {
        return catalogo.buscar(l -> l.getId().equals(id));
    }

    /** Retorna el libro o lanza 404 (uso en controladores). */
    public synchronized Libro obtenerPorIdOExcepcion(String id) {
        Libro libro = obtenerPorId(id);
        if (libro == null) throw new RecursoNoEncontradoException("Libro", "id", id);
        return libro;
    }

    // ─────────────────────────────────────────────
    // LISTAR
    // ─────────────────────────────────────────────

    public synchronized Object[] listarTodos() {
        return catalogo.aArreglo();
    }

    public ListaEnlazada<Libro> getCatalogo() {
        return catalogo;
    }

    // ─────────────────────────────────────────────
    // EDITAR
    // ─────────────────────────────────────────────

    /** @throws RecursoNoEncontradoException si no existe */
    public synchronized Libro editarLibro(String id, Libro nuevoDato) {
        int n = catalogo.getTamanio();
        for (int i = 0; i < n; i++) {
            Libro actual = catalogo.obtener(i);
            if (actual != null && actual.getId().equals(id)) {
                nuevoDato.setId(id);
                int enPrestamo = actual.getCantidadCopias() - actual.getCopiasDisponibles();
                nuevoDato.setCopiasDisponibles(Math.max(0, nuevoDato.getCantidadCopias() - enPrestamo));
                catalogo.actualizar(i, nuevoDato);
                registrarAccion("EDICION_LIBRO", "Libro editado: '" + nuevoDato.getTitulo() + "'", id);
                return nuevoDato;
            }
        }
        throw new RecursoNoEncontradoException("Libro", "id", id);
    }

    // ─────────────────────────────────────────────
    // ELIMINAR
    // ─────────────────────────────────────────────

    /**
     * @throws RecursoNoEncontradoException si no existe
     * @throws OperacionInvalidaException   si tiene copias en préstamo
     */
    public synchronized void eliminarLibro(String id) {
        Libro libro = obtenerPorId(id);
        if (libro == null) throw new RecursoNoEncontradoException("Libro", "id", id);
        if (libro.getCopiasDisponibles() < libro.getCantidadCopias()) {
            throw new OperacionInvalidaException(
                "No se puede eliminar '" + libro.getTitulo() + "': tiene copias en préstamo activo");
        }
        catalogo.eliminarPorCondicion(l -> l.getId().equals(id));
        registrarAccion("ELIMINACION_LIBRO",
            "Libro eliminado: '" + libro.getTitulo() + "' (ISBN: " + libro.getIsbn() + ")", id);
    }

    // ─────────────────────────────────────────────
    // OPERACIONES DE INVENTARIO  (llamadas desde PrestamoServicio)
    // synchronized aquí cierra el hueco del punto 2: ya no importa desde
    // dónde se llamen, el monitor de LibroServicio los serializa todos.
    // ─────────────────────────────────────────────

    /**
     * Decrementa copias disponibles. Synchronized — punto 2.
     * @return true si había copia disponible
     */
    public synchronized boolean decrementarCopiaDisponible(String libroId) {
        int n = catalogo.getTamanio();
        for (int i = 0; i < n; i++) {
            Libro l = catalogo.obtener(i);
            if (l != null && l.getId().equals(libroId)) {
                if (l.getCopiasDisponibles() > 0) {
                    l.setCopiasDisponibles(l.getCopiasDisponibles() - 1);
                    if (l.getCopiasDisponibles() == 0) l.setEstado("AGOTADO");
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Incrementa copias disponibles. Synchronized — punto 2.
     */
    public synchronized void incrementarCopiaDisponible(String libroId) {
        int n = catalogo.getTamanio();
        for (int i = 0; i < n; i++) {
            Libro l = catalogo.obtener(i);
            if (l != null && l.getId().equals(libroId)) {
                l.setCopiasDisponibles(l.getCopiasDisponibles() + 1);
                if ("AGOTADO".equals(l.getEstado()) && l.getCopiasDisponibles() > 0) {
                    l.setEstado("DISPONIBLE");
                }
                return;
            }
        }
    }

    // ─────────────────────────────────────────────
    // HISTORIAL
    // ─────────────────────────────────────────────

    private void registrarAccion(String tipo, String descripcion, String entidadId) {
        if (pilaHistorial != null) {
            pilaHistorial.apilar(new AccionHistorial(tipo, descripcion, entidadId));
        }
    }
}
