package com.biblioteca.datastructures;

/**
 * Pila genérica (LIFO) implementada manualmente con nodos enlazados.
 * Usada para el historial de acciones recientes (devoluciones, préstamos).
 * La última acción registrada es la primera en consultarse ("deshacer").
 *
 * Operaciones: apilar (push), desapilar (pop), ver cima (peek), recorrer.
 */
public class PilaHistorial<T> {

    /** Cima de la pila (último elemento insertado) */
    private Nodo<T> cima;

    /** Cantidad de elementos en la pila */
    private int tamanio;

    /** Capacidad máxima para evitar crecimiento descontrolado (historial últimas N acciones) */
    private static final int CAPACIDAD_MAXIMA = 100;

    /** Constructor: pila vacía */
    public PilaHistorial() {
        this.cima = null;
        this.tamanio = 0;
    }

    // ─────────────────────────────────────────────
    // INSERCIÓN (PUSH)
    // ─────────────────────────────────────────────

    /**
     * Apila un nuevo elemento en la cima.
     * Si se alcanza la capacidad máxima, elimina el elemento más antiguo (base).
     * Complejidad O(1).
     * @param dato Elemento a apilar
     */
    public void apilar(T dato) {
        Nodo<T> nuevoNodo = new Nodo<>(dato);
        nuevoNodo.siguiente = cima;
        cima = nuevoNodo;
        tamanio++;

        // Si se supera la capacidad, eliminar el más antiguo (fondo de la pila)
        if (tamanio > CAPACIDAD_MAXIMA) {
            eliminarFondo();
        }
    }

    /**
     * Elimina el elemento del fondo de la pila (el más antiguo).
     * Solo se llama internamente cuando se supera la capacidad máxima.
     */
    private void eliminarFondo() {
        if (cima == null) return;
        if (cima.siguiente == null) {
            cima = null;
            tamanio = 0;
            return;
        }
        Nodo<T> actual = cima;
        while (actual.siguiente.siguiente != null) {
            actual = actual.siguiente;
        }
        actual.siguiente = null;
        tamanio--;
    }

    // ─────────────────────────────────────────────
    // ELIMINACIÓN (POP)
    // ─────────────────────────────────────────────

    /**
     * Desapila y retorna el elemento de la cima (LIFO).
     * Permite funcionalidad "deshacer" la última acción.
     * @return El elemento de la cima, o null si la pila está vacía
     */
    public T desapilar() {
        if (cima == null) return null;

        T dato = cima.dato;
        cima = cima.siguiente;
        tamanio--;
        return dato;
    }

    // ─────────────────────────────────────────────
    // CONSULTA Y RECORRIDO
    // ─────────────────────────────────────────────

    /**
     * Retorna el elemento de la cima sin eliminarlo (peek).
     * @return El elemento más reciente, o null si la pila está vacía
     */
    public T verCima() {
        return cima != null ? cima.dato : null;
    }

    /**
     * Convierte la pila a un arreglo para serialización JSON.
     * El orden es cima → fondo (de más reciente a más antiguo).
     * @return Arreglo con los elementos en orden LIFO
     */
    public Object[] aArreglo() {
        Object[] arreglo = new Object[tamanio];
        Nodo<T> actual = cima;
        int i = 0;
        while (actual != null) {
            arreglo[i++] = actual.dato;
            actual = actual.siguiente;
        }
        return arreglo;
    }

    /** @return Cantidad de elementos en la pila */
    public int getTamanio() {
        return tamanio;
    }

    /** @return true si la pila está vacía */
    public boolean estaVacia() {
        return tamanio == 0;
    }
}
