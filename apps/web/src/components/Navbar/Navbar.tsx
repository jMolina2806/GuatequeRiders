import { NavLink } from 'react-router-dom'
import { useAuth } from '@/features/auth/context/useAuth'
import './Navbar.css'

export default function Navbar() {
  const { user, isAuthenticated, isLoading, logout } = useAuth()

  return (
    <header className="navbar">
      <div className="navbar__logo">Guateque Riders</div>

      <nav className="navbar__nav">
        <NavLink to="/" end>Inicio</NavLink>
        <NavLink to="/routes">Rutas</NavLink>
        <NavLink to="/events">Eventos</NavLink>
        <NavLink to="/merch">Merch</NavLink>
        <NavLink to="/profile">Perfil</NavLink>
      </nav>

      <div className="navbar__actions">
        {!isLoading && (
          isAuthenticated ? (
            <>
              <NavLink className="navbar__user" to="/profile">
                {user?.username}
              </NavLink>
              <button className="navbar__logout" onClick={logout} type="button">
                Cerrar sesión
              </button>
            </>
          ) : (
            <NavLink className="navbar__login" to="/login">
              Iniciar sesión
            </NavLink>
          )
        )}
      </div>
    </header>
  )
}
