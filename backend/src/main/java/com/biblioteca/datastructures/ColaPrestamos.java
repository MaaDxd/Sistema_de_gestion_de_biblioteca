package com.biblioteca.datastructures;

import java.util.function.Predicate;

/**
 * Cola genérica (FIFO) implementada manualmente con nodos enlazados.
 * Usada para gestionar solicitudes de préstamo en lista de espera:
 * el primer usuario en solicitar es el primero en ser atendido.
 *
 * Operaciones: encolar (enqueue), desencolar (dequeue), ver frente (peek), recorrer.
 */
public class ColaPrestamos<T> {

    /** Frente de la cola (primer elemento en entrar, primero en salir) */
    private Nodo<T> frente;

    /** Final de la cola (donde se insertan nuevos elementos) */
    private Nodo<T> fin;

    /** Cantidad de elementos en la cola */
    private int tamanio;

    /** Constructor: cola vacía */
    public ColaPrestamos() {
        this.frente = null;
        this.fin = null;
        this.tamanio = 0;
    }

    // ─────────────────────────────────────────────
    // INSERCIÓN (ENQUEUE)
    // ─────────────────────────────────────────────

    /**
     * Agrega un elemento al final de la cola.
     * Complejidad O(1) gracias al puntero 'fin'.
     * @param dato Elemento a encolar
     */
    public void encolar(T dato) {
        Nodo<T> nuevoNodo = new Nodo<>(dato);

        if (fin == null) {
            // Cola vacía: frente y fin apuntan al mismo nodo
            frente = nuevoNodo;
            fin = nuevoNodo;
        } else {
            // Agregar al final y mover el puntero fin
            fin.siguiente = nuevoNodo;
            fin = nuevoNodo;
        }
        tamanio++;
    }

    // ─────────────────────────────────────────────
    // ELIMINACIÓN (DEQUEUE)
    // ─────────────────────────────────────────────

    /**
     * Elimina y retorna el elemento del frente de la cola (FIFO).
     * @return El elemento del frente, o null si la cola está vacía
     */
    public T desencolar() {
        if (frente == null) return null;

        T dato = frente.dato;
        frente = frente.siguiente;

        // Si la cola quedó vacía, también limpiar el puntero fin
        if (frente == null) {
            fin = null;
        }
        tamanio--;
        return dato;
    }

    /**
     * Elimina el primer elemento de la cola que cumpla el predicado dado.
     * Comparación exacta sobre campos del objeto — no usa toString().
     * Útil para cancelar una solicitud de espera específica.
     * @param condicion Predicado que identifica el elemento a eliminar
     * @return true si se encontró y eliminó
     */
    public boolean eliminarPorCondicion(Predicate<T> condicion) {
        if (frente == null) return false;

        // Caso: el frente cumple la condición
        if (condicion.test(frente.dato)) {
            desencolar();
            return true;
        }

        // Buscar en el resto de la cola con comparación exacta
        Nodo<T> anterior = frente;
        Nodo<T> actual = frente.siguiente;
        while (actual != null) {
            if (condicion.test(actual.dato)) {
                anterior.siguiente = actual.siguiente;
                if (actual == fin) {
                    fin = anterior; // Actualizar puntero fin si era el último
                }
                tamanio--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // CONSULTA Y RECORRIDO
    // ─────────────────────────────────────────────

    /**
     * Retorna el elemento del frente sin eliminarlo (peek).
     * @return El elemento del frente, o null si la cola está vacía
     */
    public T verFrente() {
        return frente != null ? frente.dato : null;
    }

    /**
     * Convierte la cola a un arreglo para serialización JSON.
     * El orden es frente → fin (orden FIFO).
     * @return Arreglo con los elementos en orden de espera
     */
    public Object[] aArreglo() {
        Object[] arreglo = new Object[tamanio];
        Nodo<T> actual = frente;
        int i = 0;
        while (actual != null) {
            arreglo[i++] = actual.dato;
            actual = actual.siguiente;
        }
        return arreglo;
    }

    /** @return Cantidad de elementos en la cola */
    public int getTamanio() {
        return tamanio;
    }

    /** @return true si la cola está vacía */
    public boolean estaVacia() {
        return tamanio == 0;
    }
}
