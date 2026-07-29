/**
 * Componente de alerta reutilizable.
 * Muestra errores, éxitos e información con el diseño del sistema.
 *
 * Props:
 * - tipo: 'error' | 'success' | 'info' | 'warning'
 * - mensaje: string
 * - onCerrar: callback opcional para cerrar
 */
export default function Alerta({ tipo = 'error', mensaje, onCerrar }) {
  if (!mensaje) return null

  return (
    <div className={`alert alert-${tipo}`} role="alert" aria-live="polite">
      <span>{mensaje}</span>
      {onCerrar && (
        <button onClick={onCerrar} aria-label="Cerrar alerta">✕</button>
      )}
    </div>
  )
}
