import api from './axios'

/** Servicios para préstamos y devoluciones */
export const prestamosApi = {
  /** Lista todos los préstamos */
  listar: () => api.get('/prestamos'),

  /** Historial de préstamos de un usuario */
  porUsuario: (usuarioId) => api.get(`/prestamos/usuario/${usuarioId}`),

  /** Cola de espera de un libro */
  cola: (libroId) => api.get(`/prestamos/cola/${libroId}`),

  /** Registra un nuevo préstamo */
  registrar: (libroId, usuarioId) => api.post('/prestamos', { libroId, usuarioId }),

  /** Registra la devolución de un préstamo */
  devolver: (prestamoId) => api.put(`/prestamos/${prestamoId}/devolver`),
}

/** Servicios para el historial (pila) */
export const historialApi = {
  /** Obtiene todas las acciones del historial */
  obtener: () => api.get('/historial'),

  /** Desapila la última acción */
  deshacer: () => api.delete('/historial/deshacer'),
}
