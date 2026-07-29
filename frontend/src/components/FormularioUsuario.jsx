import { useState, useEffect } from 'react'

/**
 * Formulario para registrar o editar un usuario.
 * Props:
 * - usuarioInicial: objeto Usuario para edición (null para registro)
 * - onGuardar: callback con los datos del formulario
 * - onCancelar: callback para cerrar el formulario
 */
export default function FormularioUsuario({ usuarioInicial, onGuardar, onCancelar }) {
  const [form, setForm] = useState({
    nombre: '',
    identificacion: '',
    correo: '',
    telefono: '',
    estado: 'ACTIVO',
  })

  useEffect(() => {
    if (usuarioInicial) {
      setForm({
        nombre: usuarioInicial.nombre || '',
        identificacion: usuarioInicial.identificacion || '',
        correo: usuarioInicial.correo || '',
        telefono: usuarioInicial.telefono || '',
        estado: usuarioInicial.estado || 'ACTIVO',
      })
    }
  }, [usuarioInicial])

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    onGuardar(form)
  }

  return (
    <form className="formulario" onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="nombre">Nombre completo *</label>
        <input id="nombre" name="nombre" type="text" value={form.nombre}
          onChange={handleChange} required className="form-control"
          placeholder="Ej: Ana García López" />
      </div>

      <div className="form-group">
        <label htmlFor="identificacion">Identificación *</label>
        <input id="identificacion" name="identificacion" type="text"
          value={form.identificacion} onChange={handleChange}
          required className="form-control"
          placeholder="Ej: 1234567890"
          disabled={!!usuarioInicial}
        />
        {usuarioInicial && <small className="form-hint">La identificación no se puede cambiar.</small>}
      </div>

      <div className="form-group">
        <label htmlFor="correo">Correo electrónico *</label>
        <input id="correo" name="correo" type="email" value={form.correo}
          onChange={handleChange} required className="form-control"
          placeholder="Ej: ana@correo.com" />
      </div>

      <div className="form-row">
        <div className="form-group">
          <label htmlFor="telefono">Teléfono</label>
          <input id="telefono" name="telefono" type="tel" value={form.telefono}
            onChange={handleChange} className="form-control"
            placeholder="Ej: 3001234567" />
        </div>

        <div className="form-group">
          <label htmlFor="estado">Estado</label>
          <select id="estado" name="estado" value={form.estado}
            onChange={handleChange} className="form-control">
            <option value="ACTIVO">ACTIVO</option>
            <option value="SUSPENDIDO">SUSPENDIDO</option>
          </select>
        </div>
      </div>

      <div className="form-actions">
        <button type="button" className="btn btn-ghost" onClick={onCancelar}>Cancelar</button>
        <button type="submit" className="btn btn-primary">
          {usuarioInicial ? 'Guardar Cambios' : 'Registrar Usuario'}
        </button>
      </div>
    </form>
  )
}
