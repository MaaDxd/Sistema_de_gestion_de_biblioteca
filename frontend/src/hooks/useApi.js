import { useState, useCallback } from 'react'

/**
 * Hook genérico para llamadas a la API.
 * Extrae el mensaje de error del cuerpo JSON del backend
 * (campo "mensaje" del ManejadorGlobalExcepciones).
 *
 * Uso:
 *   const { ejecutar, cargando, error, limpiarError } = useApi()
 *   const datos = await ejecutar(() => librosApi.listar())
 */
export function useApi() {
  const [cargando, setCargando] = useState(false)
  const [error,    setError]    = useState('')

  const limpiarError = () => setError('')

  /**
   * Ejecuta una función async y maneja cargando/error automáticamente.
   * @param fn       Función que retorna una promesa (llamada Axios)
   * @param opciones { silencioso: true } → no setea error, lo lanza igual
   * @returns        Los datos de la respuesta, o null si hubo error
   */
  const ejecutar = useCallback(async (fn, opciones = {}) => {
    setCargando(true)
    setError('')
    try {
      const res = await fn()
      return res.data
    } catch (err) {
      // Extraer el mensaje del JSON del backend
      const mensaje = extraerMensaje(err)
      if (!opciones.silencioso) {
        setError(mensaje)
      }
      throw new Error(mensaje) // Re-lanzar para que el llamador pueda reaccionar
    } finally {
      setCargando(false)
    }
  }, [])

  return { ejecutar, cargando, error, limpiarError }
}

/**
 * Extrae el mensaje de error del cuerpo de respuesta del backend.
 * El ManejadorGlobalExcepciones retorna: { mensaje, status, error, timestamp }
 */
function extraerMensaje(err) {
  if (err?.response?.data?.mensaje) return err.response.data.mensaje
  if (err?.response?.data?.error)   return err.response.data.error
  if (err?.response?.status === 404) return 'Recurso no encontrado'
  if (err?.response?.status === 409) return 'Operación no permitida: conflicto con datos existentes'
  if (err?.response?.status === 400) return err.response.data?.mensaje || 'Datos de entrada inválidos'
  if (err?.response?.status >= 500)  return 'Error interno del servidor. Intenta de nuevo.'
  if (!err?.response)                return 'No se pudo conectar con el servidor. ¿Está corriendo el backend?'
  return err.message || 'Error desconocido'
}
