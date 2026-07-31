import { Routes, Route, NavLink } from 'react-router-dom'
import { useEffect, useState } from 'react'
import PaginaLibros from './pages/PaginaLibros'
import PaginaUsuarios from './pages/PaginaUsuarios'
import PaginaPrestamos from './pages/PaginaPrestamos'
import PaginaHistorial from './pages/PaginaHistorial'
import './App.css'

/**
 * Componente raíz de la aplicación.
 * Define la navegación principal y el enrutamiento de páginas.
 */
export default function App() {
  const [theme, setTheme] = useState(() => localStorage.getItem('theme') || 'light')

  useEffect(() => {
    let t
    try {
      document.documentElement.classList.add('theme-transition')
      document.documentElement.setAttribute('data-theme', theme)
      localStorage.setItem('theme', theme)
      t = setTimeout(() => {
        document.documentElement.classList.remove('theme-transition')
      }, 300)
    } catch (e) {
      // ignore
    }
    return () => { if (t) clearTimeout(t) }
  }, [theme])

  return (
    <div className="app">
      {/* ── Barra de navegación ── */}
      <header className="navbar">
        <div className="navbar-brand">
          <span className="brand-icon">📚</span>
          <span className="brand-text">Biblioteca N.N</span>
        </div>
        <nav className="navbar-links">
          <NavLink to="/" end className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            📖 Libros
          </NavLink>
          <NavLink to="/usuarios" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            👥 Usuarios
          </NavLink>
          <NavLink to="/prestamos" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            🔄 Préstamos
          </NavLink>
          <NavLink to="/historial" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            📋 Historial
          </NavLink>
        </nav>
        <div className="navbar-actions">
          <button
            type="button"
            className="btn btn-ghost theme-toggle"
            aria-label="Cambiar tema claro/oscuro"
            onClick={() => setTheme(prev => (prev === 'light' ? 'dark' : 'light'))}
          >
            {theme === 'light' ? '🌙' : '☀️'}
          </button>
        </div>
      </header>

      {/* ── Contenido de la página activa ── */}
      <main className="main-content">
        <Routes>
          <Route path="/" element={<PaginaLibros />} />
          <Route path="/usuarios" element={<PaginaUsuarios />} />
          <Route path="/prestamos" element={<PaginaPrestamos />} />
          <Route path="/historial" element={<PaginaHistorial />} />
        </Routes>
      </main>
    </div>
  )
}
