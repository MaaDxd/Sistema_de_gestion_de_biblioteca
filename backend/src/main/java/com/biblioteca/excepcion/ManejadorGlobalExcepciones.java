package com.biblioteca.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para todos los controladores REST.
 * Convierte excepciones en respuestas JSON estandarizadas con:
 * - status HTTP correcto
 * - mensaje legible
 * - timestamp
 *
 * Esto elimina el retorno de null en los servicios y las respuestas ambiguas.
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ─── 404 Not Found ───────────────────────────
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ─── 409 Conflict ────────────────────────────
    @ExceptionHandler(OperacionInvalidaException.class)
    public ResponseEntity<Map<String, Object>> manejarOperacionInvalida(OperacionInvalidaException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ─── 400 Bad Request (Bean Validation) ──────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));
        return construirRespuesta(HttpStatus.BAD_REQUEST, errores);
    }

    // ─── 400 Bad Request ─────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ─── 500 Internal Server Error (catch-all) ───
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarGeneral(Exception ex) {
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage());
    }

    /** Construye el cuerpo JSON estándar de error */
    private ResponseEntity<Map<String, Object>> construirRespuesta(HttpStatus status, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now().format(FORMATO));
        cuerpo.put("status", status.value());
        cuerpo.put("error", status.getReasonPhrase());
        cuerpo.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(cuerpo);
    }
}
