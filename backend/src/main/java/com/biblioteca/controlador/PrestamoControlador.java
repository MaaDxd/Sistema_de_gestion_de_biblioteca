package com.biblioteca.controlador;

import com.biblioteca.modelo.Prestamo;
import com.biblioteca.servicio.PrestamoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para préstamos y devoluciones.
 * Errores manejados por ManejadorGlobalExcepciones — sin null checks aquí.
 */
@RestController
@RequestMapping("/api/prestamos")
@CrossOrigin(origins = "*")
public class PrestamoControlador {

    @Autowired
    private PrestamoServicio prestamoServicio;

    @GetMapping
    public ResponseEntity<Object[]> listarTodos() {
        return ResponseEntity.ok(prestamoServicio.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prestamo> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(prestamoServicio.obtenerPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Object[]> porUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(prestamoServicio.obtenerHistorialPorUsuario(usuarioId));
    }

    @GetMapping("/cola/{libroId}")
    public ResponseEntity<Object[]> colaPorLibro(@PathVariable String libroId) {
        return ResponseEntity.ok(prestamoServicio.obtenerColaPorLibro(libroId));
    }

    /**
     * Registra un nuevo préstamo.
     * Si hay copias disponibles → 201 CREATED con estado ACTIVO.
     * Si no hay copias → 202 ACCEPTED con estado EN_ESPERA (encolado).
     */
    @PostMapping
    public ResponseEntity<Prestamo> registrar(@RequestBody Map<String, String> body) {
        String libroId   = body.get("libroId");
        String usuarioId = body.get("usuarioId");

        if (libroId == null || libroId.isBlank() || usuarioId == null || usuarioId.isBlank()) {
            throw new IllegalArgumentException("libroId y usuarioId son obligatorios");
        }

        Prestamo prestamo = prestamoServicio.registrarPrestamo(libroId, usuarioId);
        HttpStatus status = prestamo.getEstado().equals("ACTIVO")
                ? HttpStatus.CREATED : HttpStatus.ACCEPTED;
        return ResponseEntity.status(status).body(prestamo);
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<Prestamo> devolver(@PathVariable String id) {
        return ResponseEntity.ok(prestamoServicio.registrarDevolucion(id));
    }
}
