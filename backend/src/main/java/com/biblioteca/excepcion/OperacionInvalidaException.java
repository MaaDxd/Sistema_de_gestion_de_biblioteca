package com.biblioteca.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando se intenta una operación inválida según las
 * reglas de negocio (ej: eliminar libro con préstamos activos, ISBN duplicado).
 * Spring Boot la convierte automáticamente en HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class OperacionInvalidaException extends RuntimeException {

    /**
     * @param mensaje Descripción clara del motivo del rechazo
     */
    public OperacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
