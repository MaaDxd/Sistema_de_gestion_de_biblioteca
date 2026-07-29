package com.biblioteca.controlador;

import com.biblioteca.modelo.AccionHistorial;
import com.biblioteca.servicio.HistorialServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para consultar el historial de acciones (PilaHistorial).
 * Expone endpoints bajo /api/historial.
 */
@RestController
@RequestMapping("/api/historial")
@CrossOrigin(origins = "*")
public class HistorialControlador {

    @Autowired
    private HistorialServicio historialServicio;

    // ─── GET /api/historial ─────────────────────
    /**
     * Retorna todas las acciones en la pila (orden LIFO: más reciente primero).
     * Útil para mostrar el registro de actividad reciente.
     */
    @GetMapping
    public ResponseEntity<Object[]> obtenerHistorial() {
        return ResponseEntity.ok(historialServicio.obtenerHistorial());
    }

    // ─── GET /api/historial/cima ────────────────
    /** Muestra la última acción sin eliminarla (peek de la pila) */
    @GetMapping("/cima")
    public ResponseEntity<?> verCima() {
        AccionHistorial accion = historialServicio.verUltimaAccion();
        if (accion == null) {
            Map<String, String> res = new HashMap<>();
            res.put("mensaje", "El historial está vacío");
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.ok(accion);
    }

    // ─── DELETE /api/historial/deshacer ─────────
    /**
     * Desapila la última acción (funcionalidad "deshacer" / auditoría).
     * Retorna la acción eliminada de la pila.
     */
    @DeleteMapping("/deshacer")
    public ResponseEntity<?> deshacer() {
        AccionHistorial accion = historialServicio.desapilarUltimaAccion();
        if (accion == null) {
            Map<String, String> res = new HashMap<>();
            res.put("mensaje", "No hay acciones para deshacer");
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.ok(accion);
    }

    // ─── GET /api/historial/count ───────────────
    /** Retorna la cantidad de acciones registradas en la pila */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> contarAcciones() {
        Map<String, Integer> res = new HashMap<>();
        res.put("total", historialServicio.contarAcciones());
        return ResponseEntity.ok(res);
    }
}
