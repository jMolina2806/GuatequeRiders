import { useCallback, useEffect, useState } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/context/useAuth'
import CartDrawer from '@/features/merch/components/CartDrawer'
import { useCart } from '@/features/merch/context/useCart'
import './Navbar.css'

export default function Navbar() {
  const { user, isAuthenticated, isLoading, isAdmin, logout } = useAuth()
  const { totalUnits } = useCart()
  const { key: locationKey } = useLocation()
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const [isCartOpen, setIsCartOpen] = useState(false)
  const [cartLocationKey, setCartLocationKey] = useState<string | null>(null)

  const closeMenu = () => setIsMenuOpen(false)
  const openCart = () => {
    setCartLocationKey(locationKey)
    setIsCartOpen(true)
  }
  const closeCart = useCallback(() => {
    setCartLocationKey(null)
    setIsCartOpen(false)
  }, [])
  const cartIsOpen = isCartOpen && cartLocationKey === locationKey

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
          <NavLink to="/circuits" onClick={closeMenu}>
            Circuitos
          </NavLink>
          <NavLink to="/collaborators" onClick={closeMenu}>
            Colaboradores
          </NavLink>
          <NavLink to="/merch" onClick={closeMenu}>
            Merch
          </NavLink>
        </nav>

        <div className="navbar__actions">
          <button
            className="navbar__cart"
            type="button"
            onClick={openCart}
            aria-expanded={cartIsOpen}
            aria-controls="cart-drawer"
            aria-label={`Abrir carrito${totalUnits ? `, ${totalUnits} unidades` : ''}`}
          >
            <span aria-hidden="true">🛒</span>
            {totalUnits > 0 && <span className="navbar__cart-badge">{totalUnits}</span>}
          </button>
          {!isLoading &&
            (isAuthenticated ? (
              <>
                <NavLink className="navbar__user" to="/profile" onClick={closeMenu}>
                  <span className="navbar__user-label">Perfil</span>
                  <span className="navbar__username">{user?.username}</span>
                </NavLink>
                {isAdmin && (
                  <NavLink className="navbar__admin" to="/admin" onClick={closeMenu}>
                    Admin
                  </NavLink>
                )}
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
      <CartDrawer isOpen={cartIsOpen} onClose={closeCart} />
    </header>
  )
}
