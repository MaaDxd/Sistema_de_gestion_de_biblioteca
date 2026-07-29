import api from './axios'

/** Servicios para el catálogo de libros */
export const librosApi = {
  /** Obtiene todos los libros */
  listar: () => api.get('/libros'),

  /** Busca libros por término (título, autor, ISBN) */
  buscar: (termino) => api.get(`/libros/buscar?termino=${encodeURIComponent(termino)}`),

  /** Obtiene un libro por ID */
  obtener: (id) => api.get(`/libros/${id}`),

  /** Registra un nuevo libro */
  registrar: (libro) => api.post('/libros', libro),

  /** Edita un libro existente */
  editar: (id, libro) => api.put(`/libros/${id}`, libro),

  /** Elimina un libro */
  eliminar: (id) => api.delete(`/libros/${id}`),
}
