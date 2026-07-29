package com.biblioteca.datastructures;

import java.util.function.Predicate;

/**
 * Lista enlazada simple genérica implementada manualmente.
 * Usada para el catálogo de libros y el registro de usuarios.
 *
 * Operaciones: insertar al final, eliminar por índice, buscar, recorrer, obtener por índice.
 */
public class ListaEnlazada<T> {

    /** Cabeza (primer nodo) de la lista */
    private Nodo<T> cabeza;

    /** Cantidad de elementos en la lista */
    private int tamanio;

    /** Constructor: lista vacía */
    public ListaEnlazada() {
        this.cabeza = null;
        this.tamanio = 0;
    }

    // ─────────────────────────────────────────────
    // INSERCIÓN
    // ─────────────────────────────────────────────

    /**
     * Inserta un nuevo elemento al final de la lista.
     * @param dato El elemento a insertar
     */
    public void insertarAlFinal(T dato) {
        Nodo<T> nuevoNodo = new Nodo<>(dato);

        if (cabeza == null) {
            // Lista vacía: el nuevo nodo es la cabeza
            cabeza = nuevoNodo;
        } else {
            // Recorrer hasta el último nodo
            Nodo<T> actual = cabeza;
            while (actual.siguiente != null) {
                actual = actual.siguiente;
            }
            actual.siguiente = nuevoNodo;
        }
        tamanio++;
    }

    // ─────────────────────────────────────────────
    // ELIMINACIÓN
    // ─────────────────────────────────────────────

    /**
     * Elimina el elemento en la posición dada (índice base 0).
     * @param indice Posición del elemento a eliminar
     * @return true si se eliminó, false si el índice es inválido
     */
    public boolean eliminarPorIndice(int indice) {
        if (indice < 0 || indice >= tamanio) return false;

        if (indice == 0) {
            // Eliminar la cabeza
            cabeza = cabeza.siguiente;
        } else {
            // Buscar el nodo anterior al índice
            Nodo<T> anterior = cabeza;
            for (int i = 0; i < indice - 1; i++) {
                anterior = anterior.siguiente;
            }
            anterior.siguiente = anterior.siguiente.siguiente;
        }
        tamanio--;
        return true;
    }

    /**
     * Elimina el primer elemento que cumpla el predicado dado.
     * @param condicion Predicado para identificar el elemento
     * @return true si se encontró y eliminó, false si no
     */
    public boolean eliminarPorCondicion(Predicate<T> condicion) {
        if (cabeza == null) return false;

        // Caso: la cabeza cumple la condición
        if (condicion.test(cabeza.dato)) {
            cabeza = cabeza.siguiente;
            tamanio--;
            return true;
        }

        // Buscar en el resto de la lista
        Nodo<T> anterior = cabeza;
        Nodo<T> actual = cabeza.siguiente;
        while (actual != null) {
            if (condicion.test(actual.dato)) {
                anterior.siguiente = actual.siguiente;
                tamanio--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // BÚSQUEDA
    // ─────────────────────────────────────────────

    /**
     * Busca el primer elemento que cumpla el predicado.
     * @param condicion Criterio de búsqueda
     * @return El elemento encontrado, o null si no existe
     */
    public T buscar(Predicate<T> condicion) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (condicion.test(actual.dato)) {
                return actual.dato;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    /**
     * Busca todos los elementos que cumplan el predicado.
     * Retorna una nueva lista con los resultados.
     * @param condicion Criterio de búsqueda
     * @return Lista con los elementos coincidentes
     */
    public ListaEnlazada<T> buscarTodos(Predicate<T> condicion) {
        ListaEnlazada<T> resultados = new ListaEnlazada<>();
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (condicion.test(actual.dato)) {
                resultados.insertarAlFinal(actual.dato);
            }
            actual = actual.siguiente;
        }
        return resultados;
    }

    // ─────────────────────────────────────────────
    // ACCESO Y RECORRIDO
    // ─────────────────────────────────────────────

    /**
     * Obtiene el elemento en el índice dado (base 0).
     * @param indice Posición
     * @return El elemento, o null si el índice es inválido
     */
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamanio) return null;
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }

    /**
     * Actualiza el elemento en el índice dado.
     * @param indice Posición
     * @param nuevoDato Nuevo valor
     * @return true si se actualizó, false si el índice es inválido
     */
    public boolean actualizar(int indice, T nuevoDato) {
        if (indice < 0 || indice >= tamanio) return false;
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        actual.dato = nuevoDato;
        return true;
    }

    /**
     * Convierte la lista a un arreglo de Object para serialización.
     * @return Arreglo con todos los elementos
     */
    public Object[] aArreglo() {
        Object[] arreglo = new Object[tamanio];
        Nodo<T> actual = cabeza;
        int i = 0;
        while (actual != null) {
            arreglo[i++] = actual.dato;
            actual = actual.siguiente;
        }
        return arreglo;
    }

    /** @return Cantidad de elementos en la lista */
    public int getTamanio() {
        return tamanio;
    }

    /** @return true si la lista está vacía */
    public boolean estaVacia() {
        return tamanio == 0;
    }
}
