import axios from 'axios'

/**
 * Instancia central de Axios configurada para la API del backend.
 * Todos los servicios importan esta instancia.
 */
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 8000,
  headers: {
    'Content-Type': 'application/json',
  },
})

export default api
