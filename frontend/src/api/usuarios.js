import api from './axios'

/** Servicios para la gestión de usuarios */
export const usuariosApi = {
  /** Obtiene todos los usuarios */
  listar: () => api.get('/usuarios'),

  /** Busca usuarios por término */
  buscar: (termino) => api.get(`/usuarios/buscar?termino=${encodeURIComponent(termino)}`),

  /** Obtiene un usuario por ID */
  obtener: (id) => api.get(`/usuarios/${id}`),

  /** Registra un nuevo usuario */
  registrar: (usuario) => api.post('/usuarios', usuario),

  /** Edita un usuario existente */
  editar: (id, usuario) => api.put(`/usuarios/${id}`, usuario),
}
