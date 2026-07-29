package com.biblioteca.controlador;

import com.biblioteca.modelo.Libro;
import com.biblioteca.servicio.LibroServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión del catálogo de libros.
 * Errores manejados por ManejadorGlobalExcepciones.
 * Documentación disponible en /swagger-ui.html (punto 6).
 */
@Tag(name = "Libros", description = "Gestión del catálogo de libros")
@RestController
@RequestMapping("/api/libros")
@CrossOrigin(origins = "*")
public class LibroControlador {

    @Autowired
    private LibroServicio libroServicio;

    @Operation(summary = "Listar todos los libros del catálogo")
    @GetMapping
    public ResponseEntity<Object[]> listarTodos() {
        return ResponseEntity.ok(libroServicio.listarTodos());
    }

    @Operation(summary = "Buscar libros por título, autor o ISBN")
    @GetMapping("/buscar")
    public ResponseEntity<Object[]> buscar(@RequestParam String termino) {
        if (termino == null || termino.isBlank()) return ResponseEntity.ok(libroServicio.listarTodos());
        return ResponseEntity.ok(libroServicio.buscarLibros(termino).aArreglo());
    }

    @Operation(summary = "Obtener un libro por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(libroServicio.obtenerPorIdOExcepcion(id));
    }

    @Operation(summary = "Registrar un nuevo libro — lanza 409 si ISBN duplicado")
    @PostMapping
    public ResponseEntity<Libro> registrar(@Valid @RequestBody Libro libro) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libroServicio.registrarLibro(libro));
    }

    @Operation(summary = "Editar un libro existente")
    @PutMapping("/{id}")
    public ResponseEntity<Libro> editar(@PathVariable String id, @Valid @RequestBody Libro libro) {
        return ResponseEntity.ok(libroServicio.editarLibro(id, libro));
    }

    @Operation(summary = "Eliminar un libro — lanza 409 si tiene préstamos activos")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        libroServicio.eliminarLibro(id);
        return ResponseEntity.noContent().build();
    }
}
