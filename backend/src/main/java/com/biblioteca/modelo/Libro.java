package com.biblioteca.modelo;

import jakarta.validation.constraints.*;

/**
 * Entidad que representa un libro en el catálogo de la biblioteca.
 * Anotaciones @Valid activan Bean Validation en los controladores (punto 3).
 */
public class Libro {

    /** Identificador único generado automáticamente */
    private String id;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 300, message = "El título no puede superar 300 caracteres")
    private String titulo;

    @NotBlank(message = "El autor es obligatorio")
    @Size(max = 200, message = "El autor no puede superar 200 caracteres")
    private String autor;

    @NotBlank(message = "El ISBN es obligatorio")
    @Pattern(regexp = "^[\\w\\-]{5,30}$",
             message = "ISBN inválido: use solo letras, números y guiones (5-30 caracteres)")
    private String isbn;

    @Size(max = 100, message = "La categoría no puede superar 100 caracteres")
    private String categoria;

    @Min(value = 1, message = "Debe haber al menos 1 copia")
    @Max(value = 9999, message = "El máximo de copias es 9999")
    private int cantidadCopias;

    /** Copias actualmente disponibles para préstamo (calculado, no validado en entrada) */
    private int copiasDisponibles;

    /** Estado: DISPONIBLE, AGOTADO, DAÑADO, RETIRADO */
    private String estado;

    public Libro() {}

    public Libro(String id, String titulo, String autor, String isbn,
                 String categoria, int cantidadCopias, String estado) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.categoria = categoria;
        this.cantidadCopias = cantidadCopias;
        this.copiasDisponibles = cantidadCopias;
        this.estado = estado;
    }

    public String getId()                        { return id; }
    public void   setId(String id)               { this.id = id; }

    public String getTitulo()                    { return titulo; }
    public void   setTitulo(String titulo)       { this.titulo = titulo; }

    public String getAutor()                     { return autor; }
    public void   setAutor(String autor)         { this.autor = autor; }

    public String getIsbn()                      { return isbn; }
    public void   setIsbn(String isbn)           { this.isbn = isbn; }

    public String getCategoria()                 { return categoria; }
    public void   setCategoria(String categoria) { this.categoria = categoria; }

    public int  getCantidadCopias()                      { return cantidadCopias; }
    public void setCantidadCopias(int cantidadCopias)    { this.cantidadCopias = cantidadCopias; }

    public int  getCopiasDisponibles()                         { return copiasDisponibles; }
    public void setCopiasDisponibles(int copiasDisponibles)    { this.copiasDisponibles = copiasDisponibles; }

    public String getEstado()                    { return estado; }
    public void   setEstado(String estado)       { this.estado = estado; }

    @Override
    public String toString() {
        return "Libro{id=" + id + ", titulo='" + titulo + "', isbn='" + isbn + "'}";
    }
}
