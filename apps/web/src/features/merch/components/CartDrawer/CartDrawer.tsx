import { useEffect, useId, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { findMerchProduct } from '@/data/clubData'
import { useAuth } from '@/features/auth/context/useAuth'
import { useCart } from '../../context/useCart'
import { useMerch } from '../../context/useMerch'
import type { ProductSize } from '../../types/merch.types'
import './CartDrawer.css'

type CartDrawerProps = {
  isOpen: boolean
  onClose: () => void
}

const FOCUSABLE_SELECTOR =
  'a[href], button:not([disabled]), textarea, input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])'
const euro = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })

export default function CartDrawer({ isOpen, onClose }: CartDrawerProps) {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()
  const { isOpen: merchIsOpen } = useMerch()
  const { lines, subtotal, totalUnits, setQuantity, removeItem, clear } = useCart()
  const drawerRef = useRef<HTMLElement>(null)
  const closeButtonRef = useRef<HTMLButtonElement>(null)
  const titleId = useId()

  useEffect(() => {
    if (!isOpen) return

    const previouslyFocused = document.activeElement as HTMLElement | null
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    closeButtonRef.current?.focus()

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        event.preventDefault()
        onClose()
        return
      }
      if (event.key !== 'Tab') return

      const drawer = drawerRef.current
      if (!drawer) return
      const focusable = drawer.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR)
      if (focusable.length === 0) return

      const first = focusable[0]
      const last = focusable[focusable.length - 1]
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault()
        last.focus()
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault()
        first.focus()
      }
    }

    document.addEventListener('keydown', handleKeyDown)
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = previousOverflow
      previouslyFocused?.focus()
    }
  }, [isOpen, onClose])

  const handleViewMerch = () => {
    onClose()
    navigate('/merch')
  }

  const handleCheckout = () => {
    if (!merchIsOpen || lines.length === 0) return
    onClose()
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/checkout' } })
      return
    }
    navigate('/checkout')
  }

  if (!isOpen) return null

  return (
    <div className="cart-drawer-overlay" onClick={onClose}>
      <aside
        id="cart-drawer"
        ref={drawerRef}
        className="cart-drawer"
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        onClick={(event) => event.stopPropagation()}
      >
        <header className="cart-drawer__header">
          <div>
            <span className="cart-drawer__eyebrow">Merch / Pedido</span>
            <h2 id={titleId}>Tu carrito{totalUnits ? ` · ${totalUnits}` : ''}</h2>
          </div>
          <button
            ref={closeButtonRef}
            className="cart-drawer__close"
            type="button"
            aria-label="Cerrar carrito"
            onClick={onClose}
          >
            X
          </button>
        </header>

        <div className="cart-drawer__body">
          {lines.length === 0 ? (
            <div className="cart-drawer__empty">
              <span className="cart-drawer__empty-mark" aria-hidden="true">🛒</span>
              <strong>Tu carrito está vacío</strong>
              <span>Añade una prenda del club para empezar.</span>
              <button className="btn btn--ghost" type="button" onClick={handleViewMerch}>Ver Merch</button>
            </div>
          ) : (
            <div className="cart-drawer__items">
              {lines.map((line) => {
                const product = findMerchProduct(line.productCode)
                if (!product) return null
                const lineTotal = product.price * line.quantity
                return (
                  <article className="cart-drawer__item" key={`${line.productCode}-${line.size}-${line.customName ?? ''}`}>
                    <img src={product.image} alt="" />
                    <div className="cart-drawer__item-copy">
                      <strong>{product.name}</strong>
                      <span>Talla: {line.size}</span>
                      <span>Personalización: {line.customName ?? 'Sin personalizar'}</span>
                      <span>Precio: {euro.format(product.price)} / unidad</span>
                      <strong>Subtotal: {euro.format(lineTotal)}</strong>
                    </div>
                    <div className="cart-drawer__item-actions">
                      <div className="cart-drawer__quantity" aria-label={`Cantidad de ${product.name}`}>
                        <button
                          type="button"
                          aria-label={`Disminuir ${product.name}`}
                          onClick={() => setQuantity(line.productCode, line.size as ProductSize, line.customName, line.quantity - 1)}
                        >
                          −
                        </button>
                        <span aria-live="polite">{line.quantity}</span>
                        <button
                          type="button"
                          aria-label={`Aumentar ${product.name}`}
                          onClick={() => setQuantity(line.productCode, line.size as ProductSize, line.customName, line.quantity + 1)}
                        >
                          +
                        </button>
                      </div>
                      <button className="cart-drawer__remove" type="button" onClick={() => removeItem(line.productCode, line.size, line.customName)}>
                        Eliminar
                      </button>
                    </div>
                  </article>
                )
              })}
            </div>
          )}
        </div>

        {lines.length > 0 && (
          <footer className="cart-drawer__footer">
            <div className="cart-drawer__summary">
              <div><span>Subtotal</span><strong>{euro.format(subtotal)}</strong></div>
              <div><span>Total</span><strong>{euro.format(subtotal)}</strong></div>
            </div>

            {!merchIsOpen && (
              <p className="cart-drawer__closed" role="status">
                El Merch está cerrado actualmente. Tu carrito se conservará hasta que vuelva a abrir.
              </p>
            )}

            <div className="cart-drawer__footer-actions">
              <button className="btn btn--ghost" type="button" onClick={clear}>Vaciar carrito</button>
              <button className="btn btn--primary" type="button" onClick={handleCheckout} disabled={!merchIsOpen}>
                {merchIsOpen ? (isAuthenticated ? 'Finalizar pedido' : 'Iniciar sesión para comprar') : 'Compras temporalmente cerradas'}
              </button>
            </div>
          </footer>
        )}
      </aside>
    </div>
  )
}
