import { useEffect, useState } from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '@/features/auth/context/useAuth'
import './Navbar.css'

export default function Navbar() {
  const { user, isAuthenticated, isLoading, isAdmin, logout } = useAuth()
  const [isMenuOpen, setIsMenuOpen] = useState(false)

  const closeMenu = () => setIsMenuOpen(false)

  useEffect(() => {
    if (!isMenuOpen) return

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setIsMenuOpen(false)
      }
    }

    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [isMenuOpen])

  return (
    <header className="navbar">
      <div className="navbar__logo">Guateque Riders</div>

      <button
        className="navbar__toggle"
        type="button"
        aria-expanded={isMenuOpen}
        aria-controls="navbar-menu"
        aria-label={isMenuOpen ? 'Cerrar menú' : 'Abrir menú'}
        onClick={() => setIsMenuOpen((open) => !open)}
      >
        <span className="navbar__toggle-bar" />
        <span className="navbar__toggle-bar" />
        <span className="navbar__toggle-bar" />
      </button>

      <div className={`navbar__menu ${isMenuOpen ? 'navbar__menu--open' : ''}`} id="navbar-menu">
        <nav className="navbar__nav" aria-label="Navegación principal">
          <NavLink to="/" end onClick={closeMenu}>
            Inicio
          </NavLink>
          <NavLink to="/routes" onClick={closeMenu}>
            Rutas
          </NavLink>
          <NavLink to="/events" onClick={closeMenu}>
            Eventos
          </NavLink>
          <NavLink to="/merch" onClick={closeMenu}>
            Merch
          </NavLink>
          <NavLink to="/profile" onClick={closeMenu}>
            Perfil
          </NavLink>
          {isAdmin && (
            <NavLink to="/admin" onClick={closeMenu}>
              Admin
            </NavLink>
          )}
        </nav>

        <div className="navbar__actions">
          {!isLoading &&
            (isAuthenticated ? (
              <>
                <NavLink className="navbar__user" to="/profile" onClick={closeMenu}>
                  {user?.username}
                </NavLink>
                <button
                  className="btn btn--ghost btn--sm navbar__logout"
                  onClick={() => {
                    closeMenu()
                    logout()
                  }}
                  type="button"
                >
                  Cerrar sesión
                </button>
              </>
            ) : (
              <NavLink className="navbar__login" to="/login" onClick={closeMenu}>
                Iniciar sesión
              </NavLink>
            ))}
        </div>
      </div>
    </header>
  )
}
