package com.biblioteca.datastructures;

/**
 * Nodo genérico para estructuras de datos enlazadas (Lista, Pila, Cola).
 * Contiene un dato de tipo genérico T y una referencia al siguiente nodo.
 */
public class Nodo<T> {

    /** Dato almacenado en el nodo */
    public T dato;

    /** Referencia al siguiente nodo en la estructura */
    public Nodo<T> siguiente;

    /**
     * Constructor que inicializa el nodo con un dato.
     * @param dato El valor a almacenar
     */
    public Nodo(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }
}
