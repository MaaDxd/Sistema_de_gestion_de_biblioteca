/**
 * Componente de tabla para mostrar el catálogo de libros.
 * Props:
 * - libros: arreglo de objetos Libro
 * - onEditar: callback al hacer clic en Editar
 * - onEliminar: callback al hacer clic en Eliminar
 */
export default function TablaLibros({ libros, onEditar, onEliminar }) {
  if (libros.length === 0) {
    return <p className="empty-state">No hay libros en el catálogo. ¡Registra el primero!</p>
  }

  return (
    <div className="table-container">
      <table className="tabla">
        <thead>
          <tr>
            <th>ID</th>
            <th>Título</th>
            <th>Autor</th>
            <th>ISBN</th>
            <th>Categoría</th>
            <th>Disponibles</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {libros.map(libro => (
            <tr key={libro.id}>
              <td><code className="id-badge">{libro.id}</code></td>
              <td><strong>{libro.titulo}</strong></td>
              <td>{libro.autor}</td>
              <td><code>{libro.isbn}</code></td>
              <td>{libro.categoria || '—'}</td>
              <td>
                <span className={libro.copiasDisponibles === 0 ? 'text-danger' : 'text-success'}>
                  {libro.copiasDisponibles}/{libro.cantidadCopias}
                </span>
              </td>
              <td>
                <span className={`badge ${
                  libro.estado === 'DISPONIBLE' ? 'badge-success' :
                  libro.estado === 'AGOTADO'    ? 'badge-warning' :
                                                  'badge-danger'
                }`}>
                  {libro.estado}
                </span>
              </td>
              <td className="acciones">
                <button className="btn btn-sm btn-secondary" onClick={() => onEditar(libro)}>
                  Editar
                </button>
                <button
                  className="btn btn-sm btn-danger"
                  onClick={() => onEliminar(libro.id)}
                  disabled={libro.copiasDisponibles < libro.cantidadCopias}
                  title={libro.copiasDisponibles < libro.cantidadCopias
                    ? 'No se puede eliminar: tiene copias en préstamo' : ''}
                >
                  Eliminar
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
