import { useState, useEffect, useCallback } from 'react'
import { librosApi } from '../api/libros'
import { useApi } from '../hooks/useApi'
import Alerta from '../components/Alerta'
import FormularioLibro from '../components/FormularioLibro'
import TablaLibros from '../components/TablaLibros'

/**
 * Página de gestión del catálogo de libros.
 * Usa useApi() para manejo centralizado de errores 400/404/409/500.
 */
export default function PaginaLibros() {
  const [libros, setLibros] = useState([])
  const [busqueda, setBusqueda] = useState('')
  const [mostrarFormulario, setMostrarFormulario] = useState(false)
  const [libroEditando, setLibroEditando] = useState(null)
  const [exito, setExito] = useState('')

  const { ejecutar, cargando, error, limpiarError } = useApi()

  const cargarLibros = useCallback(async () => {
    const datos = await ejecutar(() => librosApi.listar()).catch(() => null)
    if (datos) setLibros(datos)
  }, [ejecutar])

  useEffect(() => { cargarLibros() }, [cargarLibros])

  const handleBuscar = async (e) => {
    e.preventDefault()
    if (!busqueda.trim()) { cargarLibros(); return }
    const datos = await ejecutar(() => librosApi.buscar(busqueda.trim())).catch(() => null)
    if (datos) setLibros(datos)
  }

  const handleGuardar = async (datos) => {
    try {
      if (libroEditando) {
        await ejecutar(() => librosApi.editar(libroEditando.id, datos))
        setExito('Libro actualizado correctamente')
      } else {
        await ejecutar(() => librosApi.registrar(datos))
        setExito('Libro registrado correctamente')
      }
      setMostrarFormulario(false)
      setLibroEditando(null)
      cargarLibros()
      setTimeout(() => setExito(''), 3000)
    } catch { /* error ya en estado */ }
  }

  const handleEliminar = async (id) => {
    if (!confirm('¿Eliminar este libro del catálogo?')) return
    try {
      await ejecutar(() => librosApi.eliminar(id))
      setExito('Libro eliminado')
      cargarLibros()
      setTimeout(() => setExito(''), 3000)
    } catch { /* error ya en estado */ }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>📖 Catálogo de Libros</h1>
        <button className="btn btn-primary"
          onClick={() => { setLibroEditando(null); limpiarError(); setMostrarFormulario(true) }}>
          + Nuevo Libro
        </button>
      </div>

      <form className="search-bar" onSubmit={handleBuscar}>
        <input type="text" placeholder="Buscar por título, autor o ISBN..."
          value={busqueda} onChange={e => setBusqueda(e.target.value)} className="search-input" />
        <button type="submit" className="btn btn-secondary">Buscar</button>
        {busqueda && (
          <button type="button" className="btn btn-ghost"
            onClick={() => { setBusqueda(''); cargarLibros() }}>Limpiar</button>
        )}
      </form>

      <Alerta tipo="error"   mensaje={error}  onCerrar={limpiarError} />
      <Alerta tipo="success" mensaje={exito}  onCerrar={() => setExito('')} />
      {cargando && <div className="loading">Cargando...</div>}

      {!cargando && (
        <TablaLibros libros={libros}
          onEditar={libro => { setLibroEditando(libro); limpiarError(); setMostrarFormulario(true) }}
          onEliminar={handleEliminar} />
      )}

      {mostrarFormulario && (
        <div className="modal-overlay" onClick={() => setMostrarFormulario(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{libroEditando ? 'Editar Libro' : 'Registrar Libro'}</h2>
              <button className="modal-close" onClick={() => setMostrarFormulario(false)}>✕</button>
            </div>
            <Alerta tipo="error" mensaje={error} onCerrar={limpiarError} />
            <FormularioLibro libroInicial={libroEditando}
              onGuardar={handleGuardar} onCancelar={() => setMostrarFormulario(false)} />
          </div>
        </div>
      )}
    </div>
  )
}
