package com.biblioteca.servicio;

import com.biblioteca.datastructures.ColaPrestamos;
import com.biblioteca.datastructures.ListaEnlazada;
import com.biblioteca.datastructures.PilaHistorial;
import com.biblioteca.datastructures.TablaHash;
import com.biblioteca.excepcion.OperacionInvalidaException;
import com.biblioteca.excepcion.RecursoNoEncontradoException;
import com.biblioteca.modelo.AccionHistorial;
import com.biblioteca.modelo.Libro;
import com.biblioteca.modelo.Prestamo;
import com.biblioteca.modelo.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Servicio para la gestión de préstamos y devoluciones.
 *
 * Estructuras de datos utilizadas:
 * - ListaEnlazada<Prestamo>  → historial completo de préstamos
 * - TablaHash<String, Cola>  → colas de espera por libro (acceso O(1) por libroId)
 * - PilaHistorial            → registro de acciones recientes (LIFO)
 *
 * Mejoras aplicadas:
 * 1. TablaHash reemplaza ListaEnlazada<EntradaCola> → búsqueda O(1) vs O(n)
 * 2. Predicados exactos en ColaPrestamos → sin toString().contains()
 * 3. Excepciones controladas → sin retornos null ambiguos
 * 4. synchronized en métodos críticos → seguridad ante concurrencia
 */
@Service
public class PrestamoServicio {

    @Autowired
    private LibroServicio libroServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    /** Pila singleton inyectada directamente — elimina dependencia de HistorialServicio */
    @Autowired
    private PilaHistorial<AccionHistorial> pilaHistorial;

    /** Lista enlazada con todos los préstamos (activos, en espera y devueltos) */
    private final ListaEnlazada<Prestamo> historialPrestamos = new ListaEnlazada<>();

    /**
     * DEFECTO 1 CORREGIDO:
     * Tabla hash de colas de espera por libroId.
     * Acceso O(1) amortizado en lugar de O(n) con lista enlazada.
     * Clave: libroId (String) → Valor: cola de préstamos en espera
     */
    private final TablaHash<String, ColaPrestamos<Prestamo>> colasPorLibro = new TablaHash<>();

    /** Formato de fecha para devoluciones */
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ─────────────────────────────────────────────
    // PERSISTENCIA (punto 1)
    // ─────────────────────────────────────────────

    /** Carga el historial de préstamos desde disco. */
    public synchronized void cargarDesde(com.biblioteca.persistencia.PersistenciaServicio ps) {
        ps.cargarPrestamos(historialPrestamos);
        // Reconstruir colas de espera EN_ESPERA desde los datos cargados
        int n = historialPrestamos.getTamanio();
        for (int i = 0; i < n; i++) {
            Prestamo p = historialPrestamos.obtener(i);
            if (p != null && "EN_ESPERA".equals(p.getEstado())) {
                ColaPrestamos<Prestamo> cola = colasPorLibro.obtenerOCrear(
                    p.getLibroId(), k -> new ColaPrestamos<>());
                cola.encolar(p);
            }
        }
    }

    /** Persiste el historial de préstamos a disco. */
    public synchronized void guardarEn(com.biblioteca.persistencia.PersistenciaServicio ps) {
        ps.guardarPrestamos(historialPrestamos);
    }

    // ─────────────────────────────────────────────
    // REGISTRAR PRÉSTAMO
    // ─────────────────────────────────────────────

    /**
     * Registra un préstamo de libro a un usuario.
     *
     * DEFECTO 4 CORREGIDO: synchronized garantiza que dos hilos simultáneos
     * no puedan decrementar la misma copia dos veces.
     *
     * Lógica:
     * 1. Valida libro y usuario — lanza excepciones si no existen (DEFECTO 3).
     * 2. Si hay copias: registra ACTIVO, decrementa.
     * 3. Si no hay copias: encola como EN_ESPERA (cola O(1) — DEFECTO 1).
     * 4. Apila la acción en el historial.
     *
     * @param libroId   ID del libro solicitado
     * @param usuarioId ID del usuario solicitante
     * @return El préstamo creado (ACTIVO o EN_ESPERA)
     * @throws RecursoNoEncontradoException si el libro o usuario no existe
     * @throws OperacionInvalidaException   si el usuario ya tiene ese libro activo
     */
    public synchronized Prestamo registrarPrestamo(String libroId, String usuarioId) {

        // DEFECTO 3: lanzar excepción en vez de retornar null
        Libro libro = libroServicio.obtenerPorId(libroId);
        if (libro == null) {
            throw new RecursoNoEncontradoException("Libro", "id", libroId);
        }

        Usuario usuario = usuarioServicio.obtenerPorId(usuarioId);
        if (usuario == null) {
            throw new RecursoNoEncontradoException("Usuario", "id", usuarioId);
        }

        // Validar préstamo duplicado activo
        boolean yaActivo = historialPrestamos.buscar(p ->
                p.getLibroId().equals(libroId) &&
                p.getUsuarioId().equals(usuarioId) &&
                p.getEstado().equals("ACTIVO")
        ) != null;

        if (yaActivo) {
            throw new OperacionInvalidaException(
                "El usuario '" + usuario.getNombre() +
                "' ya tiene un préstamo activo de '" + libro.getTitulo() + "'"
            );
        }

        // También verificar que no esté ya en la cola de espera del mismo libro
        ColaPrestamos<Prestamo> colaExistente = colasPorLibro.obtener(libroId);
        if (colaExistente != null) {
            Object[] enEspera = colaExistente.aArreglo();
            for (Object obj : enEspera) {
                Prestamo p = (Prestamo) obj;
                if (p.getUsuarioId().equals(usuarioId)) {
                    throw new OperacionInvalidaException(
                        "El usuario '" + usuario.getNombre() +
                        "' ya está en la lista de espera de '" + libro.getTitulo() + "'"
                    );
                }
            }
        }

        String estado;
        String tipoAccion;

        // DEFECTO 4: la verificación y el decremento son atómicos gracias a synchronized
        if (libro.getCopiasDisponibles() > 0) {
            estado = "ACTIVO";
            tipoAccion = "PRESTAMO_REGISTRADO";
            libroServicio.decrementarCopiaDisponible(libroId);
        } else {
            estado = "EN_ESPERA";
            tipoAccion = "RESERVA_EN_COLA";
        }

        // Crear el préstamo
        String prestamoId = "PRS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Prestamo prestamo = new Prestamo(
                prestamoId, libroId, libro.getTitulo(),
                usuarioId, usuario.getNombre(),
                estado, tipoAccion
        );

        historialPrestamos.insertarAlFinal(prestamo);

        // DEFECTO 1: encolar usando TablaHash — acceso O(1)
        if (estado.equals("EN_ESPERA")) {
            ColaPrestamos<Prestamo> cola = colasPorLibro.obtenerOCrear(
                libroId, k -> new ColaPrestamos<>()
            );
            cola.encolar(prestamo);
        }

        // Apilar acción
        String descripcion = estado.equals("ACTIVO")
                ? "Préstamo activo: '" + libro.getTitulo() + "' → " + usuario.getNombre()
                : "En cola de espera: '" + libro.getTitulo() + "' → " + usuario.getNombre();

        pilaHistorial.apilar(
                new AccionHistorial(tipoAccion, descripcion, prestamoId)
        );

        return prestamo;
    }

    // ─────────────────────────────────────────────
    // REGISTRAR DEVOLUCIÓN
    // ─────────────────────────────────────────────

    /**
     * Registra la devolución de un préstamo activo.
     *
     * DEFECTO 4: synchronized evita que dos devoluciones del mismo préstamo
     * procesen la cola dos veces simultáneamente.
     *
     * @param prestamoId ID del préstamo a devolver
     * @return El préstamo actualizado con estado DEVUELTO
     * @throws RecursoNoEncontradoException si no existe un préstamo activo con ese ID
     */
    public synchronized Prestamo registrarDevolucion(String prestamoId) {
        // Buscar el préstamo ACTIVO — DEFECTO 3: lanzar excepción si no existe
        Prestamo prestamo = null;
        int tamanio = historialPrestamos.getTamanio();
        for (int i = 0; i < tamanio; i++) {
            Prestamo p = historialPrestamos.obtener(i);
            if (p != null && p.getId().equals(prestamoId) && p.getEstado().equals("ACTIVO")) {
                prestamo = p;
                break;
            }
        }

        if (prestamo == null) {
            throw new RecursoNoEncontradoException(
                "Préstamo activo", "id", prestamoId
            );
        }

        // Actualizar estado
        prestamo.setEstado("DEVUELTO");
        prestamo.setFechaDevolucion(LocalDateTime.now().format(FORMATO_FECHA));
        prestamo.setTipoAccion("DEVOLUCION_REGISTRADA");

        libroServicio.incrementarCopiaDisponible(prestamo.getLibroId());

        pilaHistorial.apilar(
                new AccionHistorial("DEVOLUCION_REGISTRADA",
                        "Devolución: '" + prestamo.getLibroTitulo() + "' por " + prestamo.getUsuarioNombre(),
                        prestamoId)
        );

        // Procesar siguiente en cola (O(1) con TablaHash)
        procesarSiguienteEnCola(prestamo.getLibroId());

        return prestamo;
    }

    // ─────────────────────────────────────────────
    // GESTIÓN DE COLA DE ESPERA
    // ─────────────────────────────────────────────

    /**
     * Desencola la primera solicitud en espera para el libro (FIFO)
     * y la activa cuando hay copia disponible tras una devolución.
     *
     * DEFECTO 1: colasPorLibro.obtener(libroId) es O(1) con TablaHash.
     * DEFECTO 2: ya no se usa eliminarPorId con toString() — se desencola directamente.
     *
     * @param libroId ID del libro recién devuelto
     */
    private void procesarSiguienteEnCola(String libroId) {
        // O(1) — acceso directo por hash
        ColaPrestamos<Prestamo> cola = colasPorLibro.obtener(libroId);
        if (cola == null || cola.estaVacia()) return;

        Libro libro = libroServicio.obtenerPorId(libroId);
        if (libro == null || libro.getCopiasDisponibles() <= 0) return;

        // Desencolar el primero (FIFO)
        Prestamo siguiente = cola.desencolar();
        if (siguiente == null) return;

        siguiente.setEstado("ACTIVO");
        siguiente.setTipoAccion("PRESTAMO_REGISTRADO");
        libroServicio.decrementarCopiaDisponible(libroId);

        pilaHistorial.apilar(
                new AccionHistorial("PRESTAMO_REGISTRADO",
                        "Préstamo activado desde cola: '" + libro.getTitulo() +
                        "' → " + siguiente.getUsuarioNombre(),
                        siguiente.getId())
        );
    }

    // ─────────────────────────────────────────────
    // CONSULTAS
    // ─────────────────────────────────────────────

    /** Retorna todos los préstamos del historial */
    public Object[] listarTodos() {
        return historialPrestamos.aArreglo();
    }

    /** Retorna todos los préstamos de un usuario */
    public Object[] obtenerHistorialPorUsuario(String usuarioId) {
        return historialPrestamos.buscarTodos(p ->
                p.getUsuarioId().equals(usuarioId)
        ).aArreglo();
    }

    /**
     * Retorna la cola de espera de un libro.
     * DEFECTO 1: acceso O(1) con TablaHash.
     */
    public Object[] obtenerColaPorLibro(String libroId) {
        ColaPrestamos<Prestamo> cola = colasPorLibro.obtener(libroId);
        if (cola == null) return new Object[0];
        return cola.aArreglo();
    }

    /**
     * Retorna un préstamo por su ID.
     * DEFECTO 3: lanza excepción si no existe.
     */
    public Prestamo obtenerPorId(String prestamoId) {
        Prestamo p = historialPrestamos.buscar(x -> x.getId().equals(prestamoId));
        if (p == null) {
            throw new RecursoNoEncontradoException("Préstamo", "id", prestamoId);
        }
        return p;
    }
}
