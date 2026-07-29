package com.biblioteca.modelo;

import jakarta.validation.constraints.*;

/**
 * Entidad que representa un usuario registrado en la biblioteca.
 * Anotaciones Bean Validation para validar entradas en los controladores.
 */
public class Usuario {

    private String id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    private String nombre;

    @NotBlank(message = "La identificación es obligatoria")
    @Size(min = 4, max = 20, message = "La identificación debe tener entre 4 y 20 caracteres")
    private String identificacion;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    private String correo;

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String telefono;

    /** Estado: ACTIVO, SUSPENDIDO */
    private String estado;

    public Usuario() {}

    public Usuario(String id, String nombre, String identificacion,
                   String correo, String telefono, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.identificacion = identificacion;
        this.correo = correo;
        this.telefono = telefono;
        this.estado = estado;
    }

    public String getId()                              { return id; }
    public void   setId(String id)                    { this.id = id; }

    public String getNombre()                         { return nombre; }
    public void   setNombre(String nombre)            { this.nombre = nombre; }

    public String getIdentificacion()                 { return identificacion; }
    public void   setIdentificacion(String id)        { this.identificacion = id; }

    public String getCorreo()                         { return correo; }
    public void   setCorreo(String correo)            { this.correo = correo; }

    public String getTelefono()                       { return telefono; }
    public void   setTelefono(String telefono)        { this.telefono = telefono; }

    public String getEstado()                         { return estado; }
    public void   setEstado(String estado)            { this.estado = estado; }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", nombre='" + nombre + "', identificacion='" + identificacion + "'}";
    }
}
