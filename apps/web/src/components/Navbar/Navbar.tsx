import { NavLink } from 'react-router-dom'
import './Navbar.css'

export default function Navbar() {
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
    </header>
  )
}