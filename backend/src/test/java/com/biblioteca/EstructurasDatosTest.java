package com.biblioteca;

import com.biblioteca.datastructures.ColaPrestamos;
import com.biblioteca.datastructures.ListaEnlazada;
import com.biblioteca.datastructures.PilaHistorial;
import com.biblioteca.datastructures.TablaHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para las cuatro estructuras de datos manuales.
 * Evidencia el correcto funcionamiento de inserción, eliminación, búsqueda y recorrido.
 */
class EstructurasDatosTest {

    // ════════════════════════════════════════════
    // TESTS: ListaEnlazada
    // ════════════════════════════════════════════

    private ListaEnlazada<String> lista;

    @BeforeEach
    void setUp() {
        lista = new ListaEnlazada<>();
    }

    @Test
    @DisplayName("Lista: insertar y obtener elementos")
    void listaInsercionYObtencion() {
        lista.insertarAlFinal("Alfa");
        lista.insertarAlFinal("Beta");
        lista.insertarAlFinal("Gamma");

        assertEquals(3, lista.getTamanio());
        assertEquals("Alfa",  lista.obtener(0));
        assertEquals("Beta",  lista.obtener(1));
        assertEquals("Gamma", lista.obtener(2));
    }

    @Test
    @DisplayName("Lista: eliminar por índice")
    void listaEliminarPorIndice() {
        lista.insertarAlFinal("A");
        lista.insertarAlFinal("B");
        lista.insertarAlFinal("C");

        assertTrue(lista.eliminarPorIndice(1));
        assertEquals(2, lista.getTamanio());
        assertEquals("A", lista.obtener(0));
        assertEquals("C", lista.obtener(1));
    }

    @Test
    @DisplayName("Lista: eliminar por condición")
    void listaEliminarPorCondicion() {
        lista.insertarAlFinal("Manzana");
        lista.insertarAlFinal("Pera");
        lista.insertarAlFinal("Uva");

        assertTrue(lista.eliminarPorCondicion(s -> s.equals("Pera")));
        assertEquals(2, lista.getTamanio());
        assertNull(lista.buscar(s -> s.equals("Pera")));
    }

    @Test
    @DisplayName("Lista: búsqueda de elemento existente")
    void listaBuscarExistente() {
        lista.insertarAlFinal("Hola");
        lista.insertarAlFinal("Mundo");
        assertEquals("Mundo", lista.buscar(s -> s.contains("Mun")));
    }

    @Test
    @DisplayName("Lista: búsqueda de elemento inexistente retorna null")
    void listaBuscarInexistente() {
        lista.insertarAlFinal("Solo");
        assertNull(lista.buscar(s -> s.equals("NoExiste")));
    }

    @Test
    @DisplayName("Lista: buscarTodos retorna múltiples resultados")
    void listaBuscarTodos() {
        lista.insertarAlFinal("Java");
        lista.insertarAlFinal("JavaScript");
        lista.insertarAlFinal("Python");

        ListaEnlazada<String> resultados = lista.buscarTodos(s -> s.startsWith("Java"));
        assertEquals(2, resultados.getTamanio());
    }

    @Test
    @DisplayName("Lista: actualizar elemento en índice")
    void listaActualizar() {
        lista.insertarAlFinal("Original");
        lista.actualizar(0, "Actualizado");
        assertEquals("Actualizado", lista.obtener(0));
    }

    @Test
    @DisplayName("Lista: aArreglo retorna todos los elementos en orden")
    void listaAArreglo() {
        lista.insertarAlFinal("X");
        lista.insertarAlFinal("Y");

        Object[] arreglo = lista.aArreglo();
        assertEquals(2, arreglo.length);
        assertEquals("X", arreglo[0]);
        assertEquals("Y", arreglo[1]);
    }

    // ════════════════════════════════════════════
    // TESTS: ColaPrestamos (FIFO)
    // ════════════════════════════════════════════

    @Test
    @DisplayName("Cola: encolar y desencolar en orden FIFO")
    void colaFifoOrden() {
        ColaPrestamos<String> cola = new ColaPrestamos<>();
        cola.encolar("Primero");
        cola.encolar("Segundo");
        cola.encolar("Tercero");

        assertEquals(3, cola.getTamanio());
        assertEquals("Primero", cola.desencolar());
        assertEquals("Segundo", cola.desencolar());
        assertEquals("Tercero", cola.desencolar());
        assertTrue(cola.estaVacia());
    }

    @Test
    @DisplayName("Cola: verFrente no modifica la cola (peek)")
    void colaVerFrentePeek() {
        ColaPrestamos<Integer> cola = new ColaPrestamos<>();
        cola.encolar(1);
        cola.encolar(2);

        assertEquals(1, cola.verFrente());
        assertEquals(2, cola.getTamanio()); // No se modificó
    }

    @Test
    @DisplayName("Cola: desencolar en cola vacía retorna null")
    void colaDesencolarlVacia() {
        ColaPrestamos<String> cola = new ColaPrestamos<>();
        assertNull(cola.desencolar());
    }

    @Test
    @DisplayName("Cola: aArreglo respeta orden FIFO")
    void colaAArregloOrdenFifo() {
        ColaPrestamos<String> cola = new ColaPrestamos<>();
        cola.encolar("Uno");
        cola.encolar("Dos");
        cola.encolar("Tres");

        Object[] arreglo = cola.aArreglo();
        assertEquals("Uno",  arreglo[0]);
        assertEquals("Dos",  arreglo[1]);
        assertEquals("Tres", arreglo[2]);
    }

    @Test
    @DisplayName("Cola: eliminarPorCondicion usa comparación exacta (no toString)")
    void colaEliminarPorCondicionExacto() {
        ColaPrestamos<String> cola = new ColaPrestamos<>();
        cola.encolar("PRS-001");
        cola.encolar("PRS-002");
        cola.encolar("PRS-003");

        // Predicado exacto — no usa toString().contains()
        assertTrue(cola.eliminarPorCondicion(s -> s.equals("PRS-002")));
        assertEquals(2, cola.getTamanio());

        Object[] arreglo = cola.aArreglo();
        assertEquals("PRS-001", arreglo[0]);
        assertEquals("PRS-003", arreglo[1]);
    }

    @Test
    @DisplayName("Cola: eliminarPorCondicion actualiza puntero fin al eliminar el último")
    void colaEliminarUltimo() {
        ColaPrestamos<String> cola = new ColaPrestamos<>();
        cola.encolar("A");
        cola.encolar("B");

        assertTrue(cola.eliminarPorCondicion(s -> s.equals("B")));
        assertEquals(1, cola.getTamanio());
        assertEquals("A", cola.verFrente());
        // Encolar de nuevo para verificar que fin está correcto
        cola.encolar("C");
        assertEquals(2, cola.getTamanio());
    }

    // ════════════════════════════════════════════
    // TESTS: PilaHistorial (LIFO)
    // ════════════════════════════════════════════

    @Test
    @DisplayName("Pila: apilar y desapilar en orden LIFO")
    void pilaLifoOrden() {
        PilaHistorial<String> pila = new PilaHistorial<>();
        pila.apilar("Primera acción");
        pila.apilar("Segunda acción");
        pila.apilar("Tercera acción");

        assertEquals(3, pila.getTamanio());
        assertEquals("Tercera acción", pila.desapilar());
        assertEquals("Segunda acción", pila.desapilar());
        assertEquals("Primera acción", pila.desapilar());
        assertTrue(pila.estaVacia());
    }

    @Test
    @DisplayName("Pila: verCima no modifica la pila (peek)")
    void pilaVerCimaPeek() {
        PilaHistorial<String> pila = new PilaHistorial<>();
        pila.apilar("Acción A");
        pila.apilar("Acción B");

        assertEquals("Acción B", pila.verCima());
        assertEquals(2, pila.getTamanio()); // No se modificó
    }

    @Test
    @DisplayName("Pila: desapilar en pila vacía retorna null")
    void pilaDesapilarVacia() {
        PilaHistorial<String> pila = new PilaHistorial<>();
        assertNull(pila.desapilar());
    }

    @Test
    @DisplayName("Pila: aArreglo respeta orden LIFO (cima primero)")
    void pilaAArregloOrdenLifo() {
        PilaHistorial<String> pila = new PilaHistorial<>();
        pila.apilar("Vieja");
        pila.apilar("Reciente");

        Object[] arreglo = pila.aArreglo();
        assertEquals("Reciente", arreglo[0]); // La más reciente primero
        assertEquals("Vieja",    arreglo[1]);
    }

    // ════════════════════════════════════════════
    // TESTS: TablaHash (acceso O(1))
    // ════════════════════════════════════════════

    @Test
    @DisplayName("TablaHash: poner y obtener valor O(1)")
    void tablaHashPonerYObtener() {
        TablaHash<String, Integer> tabla = new TablaHash<>();
        tabla.poner("java",   1);
        tabla.poner("python", 2);
        tabla.poner("go",     3);

        assertEquals(3, tabla.getTamanio());
        assertEquals(1, tabla.obtener("java"));
        assertEquals(2, tabla.obtener("python"));
        assertEquals(3, tabla.obtener("go"));
    }

    @Test
    @DisplayName("TablaHash: actualizar clave existente no aumenta tamaño")
    void tablaHashActualizar() {
        TablaHash<String, String> tabla = new TablaHash<>();
        tabla.poner("clave", "original");
        tabla.poner("clave", "actualizado");

        assertEquals(1, tabla.getTamanio());
        assertEquals("actualizado", tabla.obtener("clave"));
    }

    @Test
    @DisplayName("TablaHash: eliminar clave existente")
    void tablaHashEliminar() {
        TablaHash<String, Integer> tabla = new TablaHash<>();
        tabla.poner("a", 1);
        tabla.poner("b", 2);

        assertTrue(tabla.eliminar("a"));
        assertNull(tabla.obtener("a"));
        assertEquals(1, tabla.getTamanio());
    }

    @Test
    @DisplayName("TablaHash: obtenerOCrear — crea solo si no existe")
    void tablaHashObtenerOCrear() {
        TablaHash<String, String> tabla = new TablaHash<>();
        String v1 = tabla.obtenerOCrear("nueva", k -> "creado-" + k);
        assertEquals("creado-nueva", v1);
        assertEquals(1, tabla.getTamanio());

        // Segunda llamada reutiliza el existente sin volver a crear
        String v2 = tabla.obtenerOCrear("nueva", k -> "no-deberia-crearse");
        assertEquals("creado-nueva", v2);
        assertEquals(1, tabla.getTamanio());
    }

    @Test
    @DisplayName("TablaHash: rehashing automático — todos los valores accesibles")
    void tablaHashRehashing() {
        TablaHash<String, Integer> tabla = new TablaHash<>();
        // 20 inserciones superan la capacidad inicial (16 * 0.75 = 12) → fuerza rehash
        for (int i = 0; i < 20; i++) {
            tabla.poner("key-" + i, i);
        }
        assertEquals(20, tabla.getTamanio());
        for (int i = 0; i < 20; i++) {
            assertEquals(i, tabla.obtener("key-" + i));
        }
    }

    @Test
    @DisplayName("TablaHash: clave inexistente retorna null")
    void tablaHashClaveInexistente() {
        TablaHash<String, String> tabla = new TablaHash<>();
        assertNull(tabla.obtener("no-existe"));
        assertFalse(tabla.eliminar("no-existe"));
    }
}
