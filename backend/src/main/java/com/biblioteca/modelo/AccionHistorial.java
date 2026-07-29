package com.biblioteca.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Registro de una acción almacenada en la PilaHistorial.
 * Cada acción del sistema queda registrada para consulta y "deshacer".
 */
public class AccionHistorial {

    /** Tipos de acción posibles en el sistema */
    public enum Tipo {
        REGISTRO_LIBRO,
        EDICION_LIBRO,
        ELIMINACION_LIBRO,
        REGISTRO_USUARIO,
        PRESTAMO_REGISTRADO,
        DEVOLUCION_REGISTRADA,
        RESERVA_EN_COLA
    }

    /** Tipo de acción realizada */
    private String tipo;

    /** Descripción legible de la acción */
    private String descripcion;

    /** ID del elemento afectado (libro, usuario o préstamo) */
    private String entidadId;

    /** Fecha y hora de la acción */
    private String fechaHora;

    /** Formato de fecha */
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Constructor vacío */
    public AccionHistorial() {}

    /**
     * Constructor.
     * @param tipo        Tipo de acción
     * @param descripcion Descripción legible
     * @param entidadId   ID del elemento afectado
     */
    public AccionHistorial(String tipo, String descripcion, String entidadId) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.entidadId = entidadId;
        this.fechaHora = LocalDateTime.now().format(FORMATO);
    }

    // ─── Getters y Setters ───

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEntidadId() { return entidadId; }
    public void setEntidadId(String entidadId) { this.entidadId = entidadId; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    @Override
    public String toString() {
        return "[" + fechaHora + "] " + tipo + ": " + descripcion;
    }
}
