package com.biblioteca.controlador;

import com.biblioteca.modelo.Usuario;
import com.biblioteca.servicio.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para usuarios.
 * @Valid activa Bean Validation en los cuerpos de entrada.
 * Errores manejados por ManejadorGlobalExcepciones.
 */
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @GetMapping
    public ResponseEntity<Object[]> listarTodos() {
        return ResponseEntity.ok(usuarioServicio.listarTodos());
    }

    @GetMapping("/buscar")
    public ResponseEntity<Object[]> buscar(@RequestParam String termino) {
        if (termino == null || termino.isBlank()) return ResponseEntity.ok(usuarioServicio.listarTodos());
        return ResponseEntity.ok(usuarioServicio.buscarUsuarios(termino).aArreglo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(usuarioServicio.obtenerPorIdOExcepcion(id));
    }

    @PostMapping
    public ResponseEntity<Usuario> registrar(@Valid @RequestBody Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioServicio.registrarUsuario(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> editar(@PathVariable String id, @Valid @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioServicio.editarUsuario(id, usuario));
    }
}
