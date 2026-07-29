import { useState, useEffect, useCallback } from 'react'
import { prestamosApi } from '../api/prestamos'
import { librosApi } from '../api/libros'
import { usuariosApi } from '../api/usuarios'
import { useApi } from '../hooks/useApi'
import Alerta from '../components/Alerta'

export default function PaginaPrestamos() {
  const [prestamos, setPrestamos] = useState([])
  const [libros, setLibros] = useState([])
  const [usuarios, setUsuarios] = useState([])
  const [libroSeleccionado, setLibroSeleccionado] = useState('')
  const [usuarioSeleccionado, setUsuarioSeleccionado] = useState('')
  const [libroConsultaCola, setLibroConsultaCola] = useState('')
  const [cola, setCola] = useState([])
  const [exito, setExito] = useState('')

  const { ejecutar, cargando, error, limpiarError } = useApi()

  const cargarDatos = useCallback(async () => {
    const [p, l, u] = await Promise.all([
      ejecutar(() => prestamosApi.listar()).catch(() => null),
      ejecutar(() => librosApi.listar()).catch(() => null),
      ejecutar(() => usuariosApi.listar()).catch(() => null),
    ])
    if (p) setPrestamos(p)
    if (l) setLibros(l)
    if (u) setUsuarios(u)
  }, [ejecutar])

  useEffect(() => { cargarDatos() }, [cargarDatos])

  const handlePrestamo = async (e) => {
    e.preventDefault()
    if (!libroSeleccionado || !usuarioSeleccionado) return
    try {
      const res = await ejecutar(() => prestamosApi.registrar(libroSeleccionado, usuarioSeleccionado))
      setExito(
        res.estado === 'ACTIVO'
          ? `✅ Préstamo activo: "${res.libroTitulo}" → ${res.usuarioNombre}`
          : `⏳ Sin copias. ${res.usuarioNombre} fue añadido a la cola de espera de "${res.libroTitulo}"`
      )
      setLibroSeleccionado('')
      setUsuarioSeleccionado('')
      cargarDatos()
      setTimeout(() => setExito(''), 5000)
    } catch { /* error ya en estado */ }
  }

  const handleDevolucion = async (prestamoId) => {
    try {
      const res = await ejecutar(() => prestamosApi.devolver(prestamoId))
      setExito(`✅ Devolución registrada: "${res.libroTitulo}"`)
      cargarDatos()
      setTimeout(() => setExito(''), 4000)
    } catch { /* error ya en estado */ }
  }

  const handleConsultarCola = async (e) => {
    e.preventDefault()
    if (!libroConsultaCola) return
    const datos = await ejecutar(() => prestamosApi.cola(libroConsultaCola)).catch(() => null)
    if (datos) setCola(datos)
  }

  const prestamosActivos   = prestamos.filter(p => p.estado === 'ACTIVO')
  const prestamosEnEspera  = prestamos.filter(p => p.estado === 'EN_ESPERA')
  const prestamosDevueltos = prestamos.filter(p => p.estado === 'DEVUELTO')

  return (
    <div className="page">
      <div className="page-header"><h1>🔄 Préstamos y Devoluciones</h1></div>

      <Alerta tipo="error"   mensaje={error}  onCerrar={limpiarError} />
      <Alerta tipo="success" mensaje={exito}  onCerrar={() => setExito('')} />

      {/* ── Nuevo Préstamo ── */}
      <section className="card">
        <h2>📋 Registrar Préstamo</h2>
        <form className="form-inline" onSubmit={handlePrestamo}>
          <div className="form-group">
            <label>Libro</label>
            <select value={libroSeleccionado} onChange={e => setLibroSeleccionado(e.target.value)}
              className="form-control">
              <option value="">— Seleccionar libro —</option>
              {libros.map(l => (
                <option key={l.id} value={l.id}>
                  {l.titulo} ({l.autor}) — {l.copiasDisponibles}/{l.cantidadCopias} disp.
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Usuario</label>
            <select value={usuarioSeleccionado} onChange={e => setUsuarioSeleccionado(e.target.value)}
              className="form-control">
              <option value="">— Seleccionar usuario —</option>
              {usuarios.map(u => (
                <option key={u.id} value={u.id}>{u.nombre} ({u.identificacion})</option>
              ))}
            </select>
          </div>
          <button type="submit" className="btn btn-primary"
            disabled={!libroSeleccionado || !usuarioSeleccionado || cargando}>
            Prestar
          </button>
        </form>
      </section>

      {/* ── Cola de Espera ── */}
      <section className="card">
        <h2>⏳ Cola de Espera por Libro</h2>
        <form className="form-inline" onSubmit={handleConsultarCola}>
          <div className="form-group">
            <select value={libroConsultaCola} onChange={e => setLibroConsultaCola(e.target.value)}
              className="form-control">
              <option value="">— Seleccionar libro —</option>
              {libros.map(l => <option key={l.id} value={l.id}>{l.titulo}</option>)}
            </select>
          </div>
          <button type="submit" className="btn btn-secondary">Consultar</button>
        </form>
        {cola.length > 0 ? (
          <div className="cola-lista">
            <p><strong>Posición en espera ({cola.length}):</strong></p>
            {cola.map((p, i) => (
              <div key={p.id} className="cola-item">
                <span className="cola-pos">#{i + 1}</span>
                <span>{p.usuarioNombre}</span>
                <span className="cola-fecha">{p.fechaPrestamo}</span>
              </div>
            ))}
          </div>
        ) : libroConsultaCola ? (
          <p className="empty-state">Sin usuarios en lista de espera.</p>
        ) : null}
      </section>

      {cargando && <div className="loading">Cargando...</div>}

      {/* ── Préstamos Activos ── */}
      <section className="card">
        <h2>✅ Préstamos Activos ({prestamosActivos.length})</h2>
        {prestamosActivos.length === 0 ? (
          <p className="empty-state">No hay préstamos activos.</p>
        ) : (
          <table className="tabla">
            <thead><tr><th>ID</th><th>Libro</th><th>Usuario</th><th>Fecha</th><th>Acción</th></tr></thead>
            <tbody>
              {prestamosActivos.map(p => (
                <tr key={p.id}>
                  <td><code className="id-badge">{p.id}</code></td>
                  <td>{p.libroTitulo}</td>
                  <td>{p.usuarioNombre}</td>
                  <td>{p.fechaPrestamo}</td>
                  <td>
                    <button className="btn btn-sm btn-success" onClick={() => handleDevolucion(p.id)}>
                      Devolver
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {/* ── En Espera ── */}
      {prestamosEnEspera.length > 0 && (
        <section className="card">
          <h2>⏳ En Cola de Espera ({prestamosEnEspera.length})</h2>
          <table className="tabla">
            <thead><tr><th>ID</th><th>Libro</th><th>Usuario</th><th>Desde</th></tr></thead>
            <tbody>
              {prestamosEnEspera.map(p => (
                <tr key={p.id}>
                  <td><code className="id-badge">{p.id}</code></td>
                  <td>{p.libroTitulo}</td><td>{p.usuarioNombre}</td><td>{p.fechaPrestamo}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}

      {/* ── Devoluciones ── */}
      <section className="card">
        <h2>📜 Historial de Devoluciones ({prestamosDevueltos.length})</h2>
        {prestamosDevueltos.length === 0 ? (
          <p className="empty-state">No hay devoluciones registradas.</p>
        ) : (
          <table className="tabla">
            <thead><tr><th>Libro</th><th>Usuario</th><th>Prestado</th><th>Devuelto</th></tr></thead>
            <tbody>
              {prestamosDevueltos.map(p => (
                <tr key={p.id}>
                  <td>{p.libroTitulo}</td><td>{p.usuarioNombre}</td>
                  <td>{p.fechaPrestamo}</td><td>{p.fechaDevolucion}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  )
}
