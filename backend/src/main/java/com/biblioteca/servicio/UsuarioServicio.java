package com.biblioteca.servicio;

import com.biblioteca.datastructures.ListaEnlazada;
import com.biblioteca.datastructures.PilaHistorial;
import com.biblioteca.excepcion.OperacionInvalidaException;
import com.biblioteca.excepcion.RecursoNoEncontradoException;
import com.biblioteca.modelo.AccionHistorial;
import com.biblioteca.modelo.Usuario;
import com.biblioteca.persistencia.PersistenciaServicio;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servicio para la gestión de usuarios de la biblioteca.
 *
 * PUNTO 2: todos los métodos son synchronized.
 * PUNTO 1: cargarDesde / guardarEn para persistencia en disco.
 * PUNTO 3: validación delegada a Bean Validation en el controlador.
 */
@Service
public class UsuarioServicio {

    private final ListaEnlazada<Usuario> listaUsuarios = new ListaEnlazada<>();
    private PilaHistorial<AccionHistorial> pilaHistorial;

    public void setPilaHistorial(PilaHistorial<AccionHistorial> pila) {
        this.pilaHistorial = pila;
    }

    // ── Persistencia ──────────────────────────────

    public synchronized void cargarDesde(PersistenciaServicio ps) {
        ps.cargarUsuarios(listaUsuarios);
    }

    public synchronized void guardarEn(PersistenciaServicio ps) {
        ps.guardarUsuarios(listaUsuarios);
    }

    // ── Registrar ─────────────────────────────────

    /**
     * @throws OperacionInvalidaException si la identificación ya existe
     */
    public synchronized Usuario registrarUsuario(Usuario usuario) {
        if (listaUsuarios.buscar(u ->
                u.getIdentificacion().equalsIgnoreCase(usuario.getIdentificacion())) != null) {
            throw new OperacionInvalidaException(
                "Ya existe un usuario con la identificación: " + usuario.getIdentificacion());
        }
        usuario.setId("USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        if (usuario.getEstado() == null || usuario.getEstado().isBlank()) {
            usuario.setEstado("ACTIVO");
        }
        listaUsuarios.insertarAlFinal(usuario);
        registrarAccion("REGISTRO_USUARIO",
            "Usuario registrado: '" + usuario.getNombre() +
            "' (ID: " + usuario.getIdentificacion() + ")", usuario.getId());
        return usuario;
    }

    // ── Búsqueda ──────────────────────────────────

    public synchronized ListaEnlazada<Usuario> buscarUsuarios(String termino) {
        String t = termino.toLowerCase();
        return listaUsuarios.buscarTodos(u ->
            u.getNombre().toLowerCase().contains(t) ||
            u.getIdentificacion().toLowerCase().contains(t) ||
            u.getCorreo().toLowerCase().contains(t));
    }

    /** Retorna null si no existe (uso interno). */
    public synchronized Usuario obtenerPorId(String id) {
        return listaUsuarios.buscar(u -> u.getId().equals(id));
    }

    /** Lanza 404 si no existe (uso en controladores). */
    public synchronized Usuario obtenerPorIdOExcepcion(String id) {
        Usuario u = obtenerPorId(id);
        if (u == null) throw new RecursoNoEncontradoException("Usuario", "id", id);
        return u;
    }

    // ── Listar ────────────────────────────────────

    public synchronized Object[] listarTodos() {
        return listaUsuarios.aArreglo();
    }

    // ── Editar ────────────────────────────────────

    /** @throws RecursoNoEncontradoException si el ID no existe */
    public synchronized Usuario editarUsuario(String id, Usuario nuevoDato) {
        int n = listaUsuarios.getTamanio();
        for (int i = 0; i < n; i++) {
            Usuario actual = listaUsuarios.obtener(i);
            if (actual != null && actual.getId().equals(id)) {
                nuevoDato.setId(id);
                listaUsuarios.actualizar(i, nuevoDato);
                return nuevoDato;
            }
        }
        throw new RecursoNoEncontradoException("Usuario", "id", id);
    }

    // ── Historial ─────────────────────────────────

    private void registrarAccion(String tipo, String descripcion, String entidadId) {
        if (pilaHistorial != null) {
            pilaHistorial.apilar(new AccionHistorial(tipo, descripcion, entidadId));
        }
    }
}
