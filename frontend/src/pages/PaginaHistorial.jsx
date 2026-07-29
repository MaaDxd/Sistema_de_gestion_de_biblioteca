import { useState, useEffect, useCallback } from 'react'
import { historialApi } from '../api/prestamos'
import { useApi } from '../hooks/useApi'
import Alerta from '../components/Alerta'

/**
 * Página del historial de acciones (PilaHistorial).
 * Usa useApi() + <Alerta> igual que las otras 3 páginas — patrón consistente.
 */
export default function PaginaHistorial() {
  const [acciones, setAcciones] = useState([])
  const [mensaje, setMensaje] = useState('')

  const { ejecutar, cargando, error, limpiarError } = useApi()

  const cargarHistorial = useCallback(async () => {
    const datos = await ejecutar(() => historialApi.obtener()).catch(() => null)
    if (datos) setAcciones(datos)
  }, [ejecutar])

  useEffect(() => { cargarHistorial() }, [cargarHistorial])

  const handleDeshacer = async () => {
    if (!confirm('¿Desapilar la última acción del historial?')) return
    try {
      const res = await ejecutar(() => historialApi.deshacer())
      setMensaje(res.mensaje
        ? res.mensaje
        : `🗑 Acción desapilada: ${res.descripcion}`)
      cargarHistorial()
      setTimeout(() => setMensaje(''), 4000)
    } catch { /* error ya en estado via useApi */ }
  }

  const iconoPorTipo = (tipo) => ({
    REGISTRO_LIBRO:       '📗',
    EDICION_LIBRO:        '✏️',
    ELIMINACION_LIBRO:    '🗑',
    REGISTRO_USUARIO:     '👤',
    PRESTAMO_REGISTRADO:  '📤',
    DEVOLUCION_REGISTRADA:'📥',
    RESERVA_EN_COLA:      '⏳',
  }[tipo] ?? '📌')

  const clasePorTipo = (tipo) => {
    if (!tipo) return ''
    if (tipo.includes('PRESTAMO') || tipo === 'DEVOLUCION_REGISTRADA') return 'accion-prestamo'
    if (tipo.includes('LIBRO'))   return 'accion-libro'
    if (tipo.includes('USUARIO')) return 'accion-usuario'
    if (tipo === 'RESERVA_EN_COLA') return 'accion-cola'
    return ''
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>📋 Historial de Acciones</h1>
        <div className="header-actions">
          <span className="badge badge-info">{acciones.length} acciones (LIFO)</span>
          <button className="btn btn-secondary" onClick={cargarHistorial}>↻ Actualizar</button>
          {acciones.length > 0 && (
            <button className="btn btn-danger" onClick={handleDeshacer}>
              ↩ Desapilar última
            </button>
          )}
        </div>
      </div>

      <div className="info-box">
        <strong>🏗 Estructura: Pila (Stack) — LIFO</strong>
        <p>
          Las acciones se apilan sobre la anterior. La más reciente aparece primero (cima).
          "Desapilar" extrae la última acción registrada, como un operador de deshacer.
        </p>
      </div>

      {/* Mismo patrón que las otras 3 páginas */}
      <Alerta tipo="error"   mensaje={error}   onCerrar={limpiarError} />
      <Alerta tipo="success" mensaje={mensaje} onCerrar={() => setMensaje('')} />

      {cargando && <div className="loading">Cargando historial...</div>}

      {!cargando && acciones.length === 0 && (
        <p className="empty-state">
          No hay acciones registradas aún. Empieza registrando libros o usuarios.
        </p>
      )}

      {!cargando && acciones.length > 0 && (
        <div className="historial-lista">
          {acciones.map((accion, index) => (
            <div key={index} className={`historial-item ${clasePorTipo(accion.tipo)}`}>
              <div className="historial-icono">{iconoPorTipo(accion.tipo)}</div>
              <div className="historial-contenido">
                <div className="historial-descripcion">{accion.descripcion}</div>
                <div className="historial-meta">
                  <span className="historial-tipo">{accion.tipo}</span>
                  <span className="historial-fecha">{accion.fechaHora}</span>
                  {index === 0 && <span className="badge badge-success">← CIMA</span>}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
