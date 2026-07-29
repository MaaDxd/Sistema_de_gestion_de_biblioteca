import { useState, useEffect } from 'react'

/**
 * Formulario para registrar o editar un libro.
 * Props:
 * - libroInicial: objeto Libro para edición (null para registro)
 * - onGuardar: callback con los datos del formulario
 * - onCancelar: callback para cerrar el formulario
 */
export default function FormularioLibro({ libroInicial, onGuardar, onCancelar }) {
  const [form, setForm] = useState({
    titulo: '',
    autor: '',
    isbn: '',
    categoria: '',
    cantidadCopias: 1,
    estado: 'DISPONIBLE',
  })

  // Si se está editando, prellenar el formulario
  useEffect(() => {
    if (libroInicial) {
      setForm({
        titulo: libroInicial.titulo || '',
        autor: libroInicial.autor || '',
        isbn: libroInicial.isbn || '',
        categoria: libroInicial.categoria || '',
        cantidadCopias: libroInicial.cantidadCopias || 1,
        estado: libroInicial.estado || 'DISPONIBLE',
      })
    }
  }, [libroInicial])

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: name === 'cantidadCopias' ? parseInt(value) || 1 : value }))
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    onGuardar(form)
  }

  return (
    <form className="formulario" onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="titulo">Título *</label>
        <input id="titulo" name="titulo" type="text" value={form.titulo}
          onChange={handleChange} required className="form-control"
          placeholder="Ej: El Quijote" />
      </div>

      <div className="form-group">
        <label htmlFor="autor">Autor *</label>
        <input id="autor" name="autor" type="text" value={form.autor}
          onChange={handleChange} required className="form-control"
          placeholder="Ej: Miguel de Cervantes" />
      </div>

      <div className="form-group">
        <label htmlFor="isbn">ISBN *</label>
        <input id="isbn" name="isbn" type="text" value={form.isbn}
          onChange={handleChange} required className="form-control"
          placeholder="Ej: 978-3-16-148410-0"
          disabled={!!libroInicial} // No editar ISBN en modo edición
        />
        {libroInicial && <small className="form-hint">El ISBN no se puede cambiar tras el registro.</small>}
      </div>

      <div className="form-row">
        <div className="form-group">
          <label htmlFor="categoria">Categoría</label>
          <input id="categoria" name="categoria" type="text" value={form.categoria}
            onChange={handleChange} className="form-control"
            placeholder="Ej: Ficción, Historia, Ciencias" />
        </div>

        <div className="form-group">
          <label htmlFor="cantidadCopias">Copias</label>
          <input id="cantidadCopias" name="cantidadCopias" type="number"
            value={form.cantidadCopias} onChange={handleChange}
            min="1" max="100" className="form-control" />
        </div>
      </div>

      <div className="form-group">
        <label htmlFor="estado">Estado</label>
        <select id="estado" name="estado" value={form.estado}
          onChange={handleChange} className="form-control">
          <option value="DISPONIBLE">DISPONIBLE</option>
          <option value="AGOTADO">AGOTADO</option>
          <option value="DAÑADO">DAÑADO</option>
          <option value="RETIRADO">RETIRADO</option>
        </select>
      </div>

      <div className="form-actions">
        <button type="button" className="btn btn-ghost" onClick={onCancelar}>Cancelar</button>
        <button type="submit" className="btn btn-primary">
          {libroInicial ? 'Guardar Cambios' : 'Registrar Libro'}
        </button>
      </div>
    </form>
  )
}
