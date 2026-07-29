package com.biblioteca;

import com.biblioteca.datastructures.ListaEnlazada;
import com.biblioteca.datastructures.PilaHistorial;
import com.biblioteca.excepcion.OperacionInvalidaException;
import com.biblioteca.excepcion.RecursoNoEncontradoException;
import com.biblioteca.modelo.AccionHistorial;
import com.biblioteca.modelo.Libro;
import com.biblioteca.modelo.Prestamo;
import com.biblioteca.modelo.Usuario;
import com.biblioteca.servicio.HistorialServicio;
import com.biblioteca.servicio.LibroServicio;
import com.biblioteca.servicio.PrestamoServicio;
import com.biblioteca.servicio.UsuarioServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para PrestamoServicio.
 * Cubre la lógica de negocio más crítica del sistema:
 * préstamos activos, cola de espera FIFO y devoluciones.
 *
 * Se inyectan dependencias manualmente (sin Spring context) para
 * mayor velocidad y control en los tests.
 */
class PrestamoServicioTest {

    private PrestamoServicio prestamoServicio;
    private LibroServicio    libroServicio;
    private UsuarioServicio  usuarioServicio;

    private Libro   libroConCopias;      // 2 copias disponibles
    private Libro   libroSinCopias;      // 0 copias disponibles
    private Usuario usuario1;
    private Usuario usuario2;
    private Usuario usuario3;

    /**
     * Configura el entorno completo antes de cada test:
     * inyecta servicios manualmente y prepara datos de prueba.
     */
    @BeforeEach
    void setUp() throws Exception {
        libroServicio   = new LibroServicio();
        usuarioServicio = new UsuarioServicio();
        prestamoServicio = new PrestamoServicio();

        // Pila de historial compartida
        PilaHistorial<AccionHistorial> pila = new PilaHistorial<>();
        libroServicio.setPilaHistorial(pila);
        usuarioServicio.setPilaHistorial(pila);

        // HistorialServicio mínimo: solo provee la pila
        HistorialServicio historial = new HistorialServicio();
        inyectarCampo(historial, "libroServicio",   libroServicio);
        inyectarCampo(historial, "usuarioServicio",  usuarioServicio);
        inyectarCampo(historial, "prestamoServicio", prestamoServicio);

        // No hay PersistenciaServicio en test: pasamos null para evitar carga de disco
        inyectarCampo(historial, "persistenciaServicio", null);

        // Inyectar dependencias en PrestamoServicio
        inyectarCampo(prestamoServicio, "libroServicio",   libroServicio);
        inyectarCampo(prestamoServicio, "usuarioServicio", usuarioServicio);
        inyectarCampo(prestamoServicio, "historialServicio", historial);

        // Inyectar la pila en historialServicio también (sin @PostConstruct)
        inyectarCampo(historial, "pilaHistorial", pila);

        // Datos de prueba: libros
        libroConCopias = new Libro("LIB-001", "Estructura de Datos", "Autor A",
                "ISBN-001", "Ciencias", 2, "DISPONIBLE");

        libroSinCopias = new Libro("LIB-002", "Algoritmos", "Autor B",
                "ISBN-002", "Ciencias", 1, "AGOTADO");
        libroSinCopias.setCopiasDisponibles(0); // Simular agotado

        // Insertar libros directamente en la lista enlazada del catálogo
        libroServicio.getCatalogo().insertarAlFinal(libroConCopias);
        libroServicio.getCatalogo().insertarAlFinal(libroSinCopias);

        // Datos de prueba: usuarios
        usuario1 = new Usuario("USR-001", "Ana García",  "ID-001", "ana@test.com", "", "ACTIVO");
        usuario2 = new Usuario("USR-002", "Luis Pérez",  "ID-002", "luis@test.com", "", "ACTIVO");
        usuario3 = new Usuario("USR-003", "María López", "ID-003", "maria@test.com", "", "ACTIVO");

        // Insertar usuarios en la lista
        ListaEnlazada<Usuario> listaUsr = obtenerListaUsuarios();
        listaUsr.insertarAlFinal(usuario1);
        listaUsr.insertarAlFinal(usuario2);
        listaUsr.insertarAlFinal(usuario3);
    }

    // ════════════════════════════════════════════
    // PRÉSTAMO ACTIVO
    // ════════════════════════════════════════════

    @Test
    @DisplayName("Préstamo: si hay copias disponibles → estado ACTIVO")
    void prestamoConCopiasDisponibles() {
        Prestamo p = prestamoServicio.registrarPrestamo("LIB-001", "USR-001");

        assertEquals("ACTIVO", p.getEstado());
        assertEquals("LIB-001", p.getLibroId());
        assertEquals("USR-001", p.getUsuarioId());
        // La copia debe haberse decrementado
        assertEquals(1, libroConCopias.getCopiasDisponibles());
    }

    @Test
    @DisplayName("Préstamo: sin copias disponibles → estado EN_ESPERA (encolado)")
    void prestamoSinCopiasVaAColaDEspera() {
        Prestamo p = prestamoServicio.registrarPrestamo("LIB-002", "USR-001");

        assertEquals("EN_ESPERA", p.getEstado());
        // Las copias no deben cambiar (ya estaban en 0)
        assertEquals(0, libroSinCopias.getCopiasDisponibles());
        // Debe estar en la cola
        Object[] cola = prestamoServicio.obtenerColaPorLibro("LIB-002");
        assertEquals(1, cola.length);
    }

    @Test
    @DisplayName("Préstamo duplicado activo mismo usuario/libro → excepción")
    void prestamoActivoDuplicadoLanzaExcepcion() {
        prestamoServicio.registrarPrestamo("LIB-001", "USR-001");
        assertThrows(OperacionInvalidaException.class,
            () -> prestamoServicio.registrarPrestamo("LIB-001", "USR-001"));
    }

    @Test
    @DisplayName("Préstamo: libro inexistente → RecursoNoEncontradoException")
    void prestamoLibroInexistenteLanzaExcepcion() {
        assertThrows(RecursoNoEncontradoException.class,
            () -> prestamoServicio.registrarPrestamo("LIB-999", "USR-001"));
    }

    @Test
    @DisplayName("Préstamo: usuario inexistente → RecursoNoEncontradoException")
    void prestamoUsuarioInexistenteLanzaExcepcion() {
        assertThrows(RecursoNoEncontradoException.class,
            () -> prestamoServicio.registrarPrestamo("LIB-001", "USR-999"));
    }

    // ════════════════════════════════════════════
    // COLA DE ESPERA (FIFO)
    // ════════════════════════════════════════════

    @Test
    @DisplayName("Cola: múltiples usuarios en espera mantienen orden FIFO")
    void colaEsperaOrdenFifo() {
        // Los 3 usuarios solicitan el libro agotado
        prestamoServicio.registrarPrestamo("LIB-002", "USR-001");
        prestamoServicio.registrarPrestamo("LIB-002", "USR-002");
        prestamoServicio.registrarPrestamo("LIB-002", "USR-003");

        Object[] cola = prestamoServicio.obtenerColaPorLibro("LIB-002");
        assertEquals(3, cola.length);

        // Verificar orden FIFO
        assertEquals("USR-001", ((Prestamo) cola[0]).getUsuarioId());
        assertEquals("USR-002", ((Prestamo) cola[1]).getUsuarioId());
        assertEquals("USR-003", ((Prestamo) cola[2]).getUsuarioId());
    }

    @Test
    @DisplayName("Cola: usuario ya en espera no puede volver a encolar el mismo libro")
    void usuarioNoEncolaDobleVez() {
        prestamoServicio.registrarPrestamo("LIB-002", "USR-001");
        assertThrows(OperacionInvalidaException.class,
            () -> prestamoServicio.registrarPrestamo("LIB-002", "USR-001"));
    }

    // ════════════════════════════════════════════
    // DEVOLUCIÓN Y ACTIVACIÓN DE COLA
    // ════════════════════════════════════════════

    @Test
    @DisplayName("Devolución: cambia estado a DEVUELTO y registra fecha")
    void devolucionCambiaEstado() {
        Prestamo prestamo = prestamoServicio.registrarPrestamo("LIB-001", "USR-001");
        Prestamo devuelto = prestamoServicio.registrarDevolucion(prestamo.getId());

        assertEquals("DEVUELTO", devuelto.getEstado());
        assertNotNull(devuelto.getFechaDevolucion());
    }

    @Test
    @DisplayName("Devolución: incrementa copias disponibles del libro")
    void devolucionIncrementaCopias() {
        Prestamo p = prestamoServicio.registrarPrestamo("LIB-001", "USR-001");
        assertEquals(1, libroConCopias.getCopiasDisponibles());

        prestamoServicio.registrarDevolucion(p.getId());
        assertEquals(2, libroConCopias.getCopiasDisponibles());
    }

    @Test
    @DisplayName("Devolución: activa al primer usuario en cola (FIFO) → pasa a ACTIVO")
    void devolucionActivaSiguienteEnCola() {
        // Agotar las 2 copias
        Prestamo p1 = prestamoServicio.registrarPrestamo("LIB-001", "USR-001");
        Prestamo p2 = prestamoServicio.registrarPrestamo("LIB-001", "USR-002");
        assertEquals(0, libroConCopias.getCopiasDisponibles());

        // Usuario3 va a la cola
        Prestamo espera = prestamoServicio.registrarPrestamo("LIB-001", "USR-003");
        assertEquals("EN_ESPERA", espera.getEstado());

        // Devolver uno → usuario3 debe activarse (FIFO)
        prestamoServicio.registrarDevolucion(p1.getId());

        assertEquals("ACTIVO", espera.getEstado());
        assertEquals(0, libroConCopias.getCopiasDisponibles()); // La copia fue tomada por cola
        // La cola debe estar vacía
        assertEquals(0, prestamoServicio.obtenerColaPorLibro("LIB-001").length);
    }

    @Test
    @DisplayName("Devolución de préstamo inexistente → RecursoNoEncontradoException")
    void devolucionInexistenteLanzaExcepcion() {
        assertThrows(RecursoNoEncontradoException.class,
            () -> prestamoServicio.registrarDevolucion("PRS-NOEXISTE"));
    }

    // ════════════════════════════════════════════
    // UTILIDADES DE TEST
    // ════════════════════════════════════════════

    /** Inyecta un campo privado por reflexión (evita @Autowired en tests sin Spring) */
    @SuppressWarnings("unchecked")
    private ListaEnlazada<Usuario> obtenerListaUsuarios() throws Exception {
        Field f = UsuarioServicio.class.getDeclaredField("listaUsuarios");
        f.setAccessible(true);
        return (ListaEnlazada<Usuario>) f.get(usuarioServicio);
    }

    private void inyectarCampo(Object target, String nombre, Object valor) throws Exception {
        // Buscar en la clase y sus superclases
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(nombre);
                f.setAccessible(true);
                f.set(target, valor);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Campo '" + nombre + "' no encontrado en " + target.getClass());
    }
}
