package com.biblioteca.datastructures;

import java.util.function.Function;

/**
 * Tabla Hash de encadenamiento separado implementada manualmente.
 *
 * Reemplaza el uso de ListaEnlazada para buscar colas por libroId,
 * reduciendo la búsqueda de O(n) a O(1) amortizado.
 *
 * Estructura interna: arreglo de listas enlazadas (cada celda es una cadena
 * de pares clave-valor que colisionan en el mismo bucket).
 *
 * @param <K> Tipo de la clave (debe tener hashCode() confiable, ej: String)
 * @param <V> Tipo del valor almacenado
 */
public class TablaHash<K, V> {

    /** Número de buckets (primo para distribuir mejor los hashes) */
    private static final int CAPACIDAD_INICIAL = 16;

    /** Factor de carga máximo antes de hacer rehashing */
    private static final float FACTOR_CARGA = 0.75f;

    /** Par clave-valor almacenado en cada nodo de la cadena */
    private static class Entrada<K, V> {
        K clave;
        V valor;
        Entrada<K, V> siguiente;

        Entrada(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = null;
        }
    }

    /** Arreglo de buckets; cada bucket es la cabeza de una lista enlazada */
    @SuppressWarnings("unchecked")
    private Entrada<K, V>[] buckets = new Entrada[CAPACIDAD_INICIAL];

    /** Capacidad actual del arreglo */
    private int capacidad = CAPACIDAD_INICIAL;

    /** Cantidad total de pares almacenados */
    private int tamanio = 0;

    // ─────────────────────────────────────────────
    // FUNCIÓN DE HASH
    // ─────────────────────────────────────────────

    /**
     * Calcula el índice de bucket para una clave dada.
     * Usa el hashCode de la clave con módulo sobre la capacidad.
     * El operador & 0x7FFFFFFF asegura que el resultado sea positivo.
     * @param clave La clave a hashear
     * @return Índice entre 0 y capacidad-1
     */
    private int calcularIndice(K clave) {
        return (clave.hashCode() & 0x7FFFFFFF) % capacidad;
    }

    // ─────────────────────────────────────────────
    // INSERCIÓN / ACTUALIZACIÓN
    // ─────────────────────────────────────────────

    /**
     * Inserta o actualiza el valor asociado a la clave.
     * Si la clave ya existe, sobreescribe el valor.
     * Si el factor de carga se supera, realiza rehashing.
     * @param clave La clave
     * @param valor El valor a asociar
     */
    public void poner(K clave, V valor) {
        int indice = calcularIndice(clave);
        Entrada<K, V> actual = buckets[indice];

        // Recorrer la cadena: si la clave ya existe, actualizar
        while (actual != null) {
            if (actual.clave.equals(clave)) {
                actual.valor = valor;
                return;
            }
            actual = actual.siguiente;
        }

        // Clave nueva: insertar al frente de la cadena (O(1))
        Entrada<K, V> nueva = new Entrada<>(clave, valor);
        nueva.siguiente = buckets[indice];
        buckets[indice] = nueva;
        tamanio++;

        // Rehashing si se supera el factor de carga
        if (tamanio > capacidad * FACTOR_CARGA) {
            rehash();
        }
    }

    // ─────────────────────────────────────────────
    // BÚSQUEDA
    // ─────────────────────────────────────────────

    /**
     * Retorna el valor asociado a la clave, o null si no existe.
     * Complejidad amortizada O(1).
     * @param clave La clave a buscar
     * @return El valor o null
     */
    public V obtener(K clave) {
        int indice = calcularIndice(clave);
        Entrada<K, V> actual = buckets[indice];
        while (actual != null) {
            if (actual.clave.equals(clave)) return actual.valor;
            actual = actual.siguiente;
        }
        return null;
    }

    /**
     * Retorna el valor si existe; si no, lo crea con la función proveedora y lo inserta.
     * Equivalente a computeIfAbsent pero sin usar Map.
     * @param clave     La clave
     * @param proveedor Función que genera el valor si no existe
     * @return El valor existente o el recién creado
     */
    public V obtenerOCrear(K clave, Function<K, V> proveedor) {
        V existente = obtener(clave);
        if (existente != null) return existente;
        V nuevo = proveedor.apply(clave);
        poner(clave, nuevo);
        return nuevo;
    }

    // ─────────────────────────────────────────────
    // ELIMINACIÓN
    // ─────────────────────────────────────────────

    /**
     * Elimina la entrada asociada a la clave.
     * @param clave La clave a eliminar
     * @return true si existía y se eliminó
     */
    public boolean eliminar(K clave) {
        int indice = calcularIndice(clave);
        Entrada<K, V> actual = buckets[indice];
        Entrada<K, V> anterior = null;

        while (actual != null) {
            if (actual.clave.equals(clave)) {
                if (anterior == null) {
                    buckets[indice] = actual.siguiente; // Era la cabeza
                } else {
                    anterior.siguiente = actual.siguiente;
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
    // REHASHING
    // ─────────────────────────────────────────────

    /**
     * Duplica la capacidad y redistribuye todas las entradas.
     * Se llama automáticamente cuando se supera el factor de carga.
     */
    @SuppressWarnings("unchecked")
    private void rehash() {
        capacidad = capacidad * 2;
        Entrada<K, V>[] nuevosBuckets = new Entrada[capacidad];

        // Reinsertar todas las entradas con el nuevo tamaño
        for (Entrada<K, V> cabeza : buckets) {
            Entrada<K, V> actual = cabeza;
            while (actual != null) {
                Entrada<K, V> siguiente = actual.siguiente;
                int nuevoIndice = (actual.clave.hashCode() & 0x7FFFFFFF) % capacidad;
                actual.siguiente = nuevosBuckets[nuevoIndice];
                nuevosBuckets[nuevoIndice] = actual;
                actual = siguiente;
            }
        }
        buckets = nuevosBuckets;
    }

    // ─────────────────────────────────────────────
    // UTILIDADES
    // ─────────────────────────────────────────────

    /** @return true si la clave existe en la tabla */
    public boolean contiene(K clave) {
        return obtener(clave) != null;
    }

    /** @return Cantidad de pares almacenados */
    public int getTamanio() {
        return tamanio;
    }

    /** @return true si la tabla está vacía */
    public boolean estaVacia() {
        return tamanio == 0;
    }
}
