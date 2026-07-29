package com.biblioteca.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entidad que representa un préstamo de libro a un usuario.
 * Se apila en la PilaHistorial para el registro de acciones recientes
 * y se encola en ColaPrestamos cuando no hay copias disponibles.
 */
public class Prestamo {

    /** Identificador único del préstamo */
    private String id;

    /** ID del libro prestado */
    private String libroId;

    /** Título del libro (denormalizado para mostrar sin joins) */
    private String libroTitulo;

    /** ID del usuario que solicita el préstamo */
    private String usuarioId;

    /** Nombre del usuario (denormalizado) */
    private String usuarioNombre;

    /** Fecha y hora en que se registró el préstamo */
    private String fechaPrestamo;

    /** Fecha y hora en que se registró la devolución (null si sigue prestado) */
    private String fechaDevolucion;

    /**
     * Estado del préstamo:
     * - ACTIVO: libro está en manos del usuario
     * - DEVUELTO: libro fue regresado
     * - EN_ESPERA: el usuario espera en la cola (no hay copias disponibles)
     */
    private String estado;

    /** Tipo de acción para el historial de la pila: PRESTAMO, DEVOLUCION, RESERVA */
    private String tipoAccion;

    /** Formato estándar para fechas */
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Constructor vacío */
    public Prestamo() {}

    /**
     * Constructor para registrar un nuevo préstamo.
     * @param id           ID único
     * @param libroId      ID del libro
     * @param libroTitulo  Título del libro
     * @param usuarioId    ID del usuario
     * @param usuarioNombre Nombre del usuario
     * @param estado       Estado inicial (ACTIVO o EN_ESPERA)
     * @param tipoAccion   Tipo de acción para la pila
     */
    public Prestamo(String id, String libroId, String libroTitulo,
                    String usuarioId, String usuarioNombre,
                    String estado, String tipoAccion) {
        this.id = id;
        this.libroId = libroId;
        this.libroTitulo = libroTitulo;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.fechaPrestamo = LocalDateTime.now().format(FORMATO_FECHA);
        this.fechaDevolucion = null;
        this.estado = estado;
        this.tipoAccion = tipoAccion;
    }

    // ─── Getters y Setters ───

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLibroId() { return libroId; }
    public void setLibroId(String libroId) { this.libroId = libroId; }

    public String getLibroTitulo() { return libroTitulo; }
    public void setLibroTitulo(String libroTitulo) { this.libroTitulo = libroTitulo; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(String fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public String getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(String fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipoAccion() { return tipoAccion; }
    public void setTipoAccion(String tipoAccion) { this.tipoAccion = tipoAccion; }

    @Override
    public String toString() {
        return "Prestamo{id=" + id + ", libroId=" + libroId + ", usuarioId=" + usuarioId + ", estado=" + estado + "}";
    }
}
