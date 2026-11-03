import { useCallback, useEffect, useState } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/context/useAuth'
import CartDrawer from '@/features/merch/components/CartDrawer'
import { useCart } from '@/features/merch/context/useCart'
import { getCommunityLinks } from '@/features/community/services/community.service'
import './Navbar.css'

const INSTAGRAM_URL = 'https://www.instagram.com/guateque_riders/'

export default function Navbar() {
  const { user, isAuthenticated, isLoading, isAdmin, isMember, logout } = useAuth()
  const { totalUnits } = useCart()
  const { key: locationKey } = useLocation()
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const [isCartOpen, setIsCartOpen] = useState(false)
  const [cartLocationKey, setCartLocationKey] = useState<string | null>(null)
  const [whatsappGroupUrl, setWhatsappGroupUrl] = useState<string | null>(null)

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
    let cancelled = false

    if (!isAuthenticated) {
      setWhatsappGroupUrl(null)
      return
    }

    getCommunityLinks()
      .then((links) => {
        if (!cancelled) {
          setWhatsappGroupUrl(links.whatsappGroupUrl)
        }
      })
      .catch(() => {
        if (!cancelled) {
          setWhatsappGroupUrl(null)
        }
      })

    return () => {
      cancelled = true
    }
  }, [isAuthenticated])

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
          <div className="navbar__socials" aria-label="Redes sociales">
            {isAuthenticated && whatsappGroupUrl && (
              <a
                className="navbar__social navbar__social--whatsapp"
                href={whatsappGroupUrl}
                target="_blank"
                rel="noopener noreferrer"
                aria-label="Entrar al grupo de WhatsApp de Guateque Riders"
                title="Entrar al grupo de WhatsApp"
                onClick={closeMenu}
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M20.52 3.48A11.82 11.82 0 0 0 12.1 0C5.54 0 .2 5.34.2 11.9c0 2.1.55 4.15 1.6 5.95L.1 24l6.3-1.65a11.9 11.9 0 0 0 5.69 1.45h.01c6.56 0 11.9-5.34 11.9-11.9 0-3.18-1.24-6.17-3.48-8.42ZM12.1 21.8h-.01a9.88 9.88 0 0 1-5.04-1.38l-.36-.22-3.74.98 1-3.64-.24-.37A9.84 9.84 0 0 1 2.2 11.9C2.2 6.44 6.64 2 12.1 2c2.64 0 5.13 1.03 7 2.9a9.84 9.84 0 0 1 2.9 7c0 5.46-4.44 9.9-9.9 9.9Zm5.43-7.42c-.3-.15-1.76-.87-2.03-.97-.27-.1-.47-.15-.67.15-.2.3-.77.97-.94 1.17-.17.2-.35.22-.65.07-.3-.15-1.25-.46-2.38-1.47a8.9 8.9 0 0 1-1.65-2.05c-.17-.3-.02-.46.13-.61.13-.13.3-.35.45-.52.15-.17.2-.3.3-.5.1-.2.05-.37-.02-.52-.08-.15-.67-1.61-.92-2.21-.24-.58-.49-.5-.67-.51h-.57c-.2 0-.52.07-.79.37-.27.3-1.04 1.02-1.04 2.48s1.07 2.88 1.22 3.08c.15.2 2.1 3.2 5.08 4.49.71.3 1.27.49 1.7.63.71.23 1.36.19 1.87.12.57-.09 1.76-.72 2.01-1.41.25-.7.25-1.3.17-1.42-.07-.13-.27-.2-.57-.35Z" />
                </svg>
              </a>
            )}
            <a
              className="navbar__social navbar__social--instagram"
              href={INSTAGRAM_URL}
              target="_blank"
              rel="noopener noreferrer"
              aria-label="Abrir Instagram de Guateque Riders"
              title="Instagram @guateque_riders"
              onClick={closeMenu}
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M7.8 2h8.4A5.8 5.8 0 0 1 22 7.8v8.4a5.8 5.8 0 0 1-5.8 5.8H7.8A5.8 5.8 0 0 1 2 16.2V7.8A5.8 5.8 0 0 1 7.8 2Zm0 2A3.8 3.8 0 0 0 4 7.8v8.4A3.8 3.8 0 0 0 7.8 20h8.4a3.8 3.8 0 0 0 3.8-3.8V7.8A3.8 3.8 0 0 0 16.2 4H7.8Zm8.95 1.5a1.25 1.25 0 1 1 0 2.5 1.25 1.25 0 0 1 0-2.5ZM12 7a5 5 0 1 1 0 10 5 5 0 0 1 0-10Zm0 2a3 3 0 1 0 0 6 3 3 0 0 0 0-6Z" />
              </svg>
            </a>
          </div>
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
                {isMember && (
                  <NavLink className="navbar__admin" to="/member-card" onClick={closeMenu}>
                    Carnet
                  </NavLink>
                )}
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
