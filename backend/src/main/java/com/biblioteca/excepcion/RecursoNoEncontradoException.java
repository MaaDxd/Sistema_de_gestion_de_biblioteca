package com.biblioteca.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en la estructura.
 * Spring Boot la convierte automáticamente en HTTP 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNoEncontradoException extends RuntimeException {

    private final String recurso;
    private final String campo;
    private final String valor;

    /**
     * @param recurso Nombre de la entidad (ej: "Libro", "Usuario")
     * @param campo   Campo usado para buscar (ej: "id", "ISBN")
     * @param valor   Valor buscado
     */
    public RecursoNoEncontradoException(String recurso, String campo, String valor) {
        super(recurso + " no encontrado con " + campo + ": " + valor);
        this.recurso = recurso;
        this.campo = campo;
        this.valor = valor;
    }

    public String getRecurso() { return recurso; }
    public String getCampo()   { return campo; }
    public String getValor()   { return valor; }
}
