import { useState, useEffect, useCallback } from 'react'
import { usuariosApi } from '../api/usuarios'
import { useApi } from '../hooks/useApi'
import Alerta from '../components/Alerta'
import FormularioUsuario from '../components/FormularioUsuario'

export default function PaginaUsuarios() {
  const [usuarios, setUsuarios] = useState([])
  const [busqueda, setBusqueda] = useState('')
  const [mostrarFormulario, setMostrarFormulario] = useState(false)
  const [usuarioEditando, setUsuarioEditando] = useState(null)
  const [exito, setExito] = useState('')

  const { ejecutar, cargando, error, limpiarError } = useApi()

  const cargarUsuarios = useCallback(async () => {
    const datos = await ejecutar(() => usuariosApi.listar()).catch(() => null)
    if (datos) setUsuarios(datos)
  }, [ejecutar])

  useEffect(() => { cargarUsuarios() }, [cargarUsuarios])

  const handleBuscar = async (e) => {
    e.preventDefault()
    if (!busqueda.trim()) { cargarUsuarios(); return }
    const datos = await ejecutar(() => usuariosApi.buscar(busqueda.trim())).catch(() => null)
    if (datos) setUsuarios(datos)
  }

  const handleGuardar = async (datos) => {
    try {
      if (usuarioEditando) {
        await ejecutar(() => usuariosApi.editar(usuarioEditando.id, datos))
        setExito('Usuario actualizado correctamente')
      } else {
        await ejecutar(() => usuariosApi.registrar(datos))
        setExito('Usuario registrado correctamente')
      }
      setMostrarFormulario(false)
      setUsuarioEditando(null)
      cargarUsuarios()
      setTimeout(() => setExito(''), 3000)
    } catch { /* error ya en estado */ }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>👥 Usuarios</h1>
        <button className="btn btn-primary"
          onClick={() => { setUsuarioEditando(null); limpiarError(); setMostrarFormulario(true) }}>
          + Nuevo Usuario
        </button>
      </div>

      <form className="search-bar" onSubmit={handleBuscar}>
        <input type="text" placeholder="Buscar por nombre, identificación o correo..."
          value={busqueda} onChange={e => setBusqueda(e.target.value)} className="search-input" />
        <button type="submit" className="btn btn-secondary">Buscar</button>
        {busqueda && (
          <button type="button" className="btn btn-ghost"
            onClick={() => { setBusqueda(''); cargarUsuarios() }}>Limpiar</button>
        )}
      </form>

      <Alerta tipo="error"   mensaje={error} onCerrar={limpiarError} />
      <Alerta tipo="success" mensaje={exito} onCerrar={() => setExito('')} />
      {cargando && <div className="loading">Cargando...</div>}

      {!cargando && (
        <div className="table-container">
          {usuarios.length === 0 ? (
            <p className="empty-state">No hay usuarios registrados aún.</p>
          ) : (
            <table className="tabla">
              <thead>
                <tr>
                  <th>ID</th><th>Nombre</th><th>Identificación</th>
                  <th>Correo</th><th>Teléfono</th><th>Estado</th><th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {usuarios.map(u => (
                  <tr key={u.id}>
                    <td><code className="id-badge">{u.id}</code></td>
                    <td><strong>{u.nombre}</strong></td>
                    <td>{u.identificacion}</td>
                    <td>{u.correo}</td>
                    <td>{u.telefono || '—'}</td>
                    <td>
                      <span className={`badge ${u.estado === 'ACTIVO' ? 'badge-success' : 'badge-warning'}`}>
                        {u.estado}
                      </span>
                    </td>
                    <td>
                      <button className="btn btn-sm btn-secondary"
                        onClick={() => { setUsuarioEditando(u); limpiarError(); setMostrarFormulario(true) }}>
                        Editar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {mostrarFormulario && (
        <div className="modal-overlay" onClick={() => setMostrarFormulario(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{usuarioEditando ? 'Editar Usuario' : 'Registrar Usuario'}</h2>
              <button className="modal-close" onClick={() => setMostrarFormulario(false)}>✕</button>
            </div>
            <Alerta tipo="error" mensaje={error} onCerrar={limpiarError} />
            <FormularioUsuario usuarioInicial={usuarioEditando}
              onGuardar={handleGuardar} onCancelar={() => setMostrarFormulario(false)} />
          </div>
        </div>
      )}
    </div>
  )
}
