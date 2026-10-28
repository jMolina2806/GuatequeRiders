import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { findMerchProduct } from '@/data/clubData'
import { useAuth } from '@/features/auth/context/useAuth'
import { useCart } from '@/features/merch/context/useCart'
import { useMerch } from '@/features/merch/context/useMerch'
import { createOrder } from '@/features/merch/services/orders.service'
import { markCartOrder } from '@/features/merch/utils/cartCheckout'
import './CheckoutPage.css'

const euro = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })

export default function CheckoutPage() {
  const navigate = useNavigate()
  const { user, isAuthenticated, isLoading: authLoading } = useAuth()
  const { isOpen, refresh } = useMerch()
  const { lines, subtotal } = useCart()
  const [customerName, setCustomerName] = useState(user?.username ?? '')
  const [customerEmail, setCustomerEmail] = useState(user?.email ?? '')
  const [customerPhone, setCustomerPhone] = useState('')
  const [notes, setNotes] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (authLoading) {
    return <section className="checkout-page"><p role="status">Comprobando sesión...</p></section>
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: '/checkout' }} />
  }

  if (lines.length === 0) {
    return (
      <section className="checkout-page checkout-page--empty">
        <h1>Tu carrito está vacío</h1>
        <p>Añade una pieza del club antes de comenzar el checkout.</p>
        <Link className="btn btn--primary" to="/merch">Volver a Merch</Link>
      </section>
    )
  }

  if (!isOpen) {
    return (
      <section className="checkout-page checkout-page--empty">
        <span className="checkout-page__step">Checkout bloqueado</span>
        <h1>Merch cerrado</h1>
        <p>El carrito se conserva, pero las compras están temporalmente cerradas.</p>
        <Link className="btn btn--ghost" to="/merch">Volver a Merch</Link>
      </section>
    )
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)
    setIsSubmitting(true)
    try {
      const order = await createOrder({
        items: lines,
        customerName,
        customerEmail,
        customerPhone,
        notes: notes || undefined,
      })
      markCartOrder(order.id)
      navigate(`/checkout/payment/${order.id}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo crear el pedido')
      await refresh()
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="checkout-page">
      <header className="checkout-page__header">
        <span className="checkout-page__eyebrow">Merch / Checkout</span>
        <h1>Finalizar pedido</h1>
        <p>Datos del comprador y revisión del pedido. El pago se completará en la página segura de Stripe.</p>
      </header>

      <div className="checkout-page__steps" aria-label="Pasos del checkout">
        <span className="is-active">1. Datos</span>
        <span>2. Revisión</span>
        <span>3. Pago seguro</span>
      </div>

      {error && <div className="checkout-page__error" role="alert">{error}</div>}

      <div className="checkout-page__layout">
        <form className="checkout-page__form" onSubmit={handleSubmit}>
          <div className="checkout-page__form-section">
            <h2>Datos del comprador</h2>
            <label>
              Nombre
              <input value={customerName} onChange={(event) => setCustomerName(event.target.value)} required maxLength={255} />
            </label>
            <label>
              Email
              <input type="email" value={customerEmail} onChange={(event) => setCustomerEmail(event.target.value)} required maxLength={255} />
            </label>
            <label>
              Teléfono
              <input value={customerPhone} onChange={(event) => setCustomerPhone(event.target.value)} required maxLength={50} />
            </label>
            <label>
              Notas opcionales
              <textarea value={notes} onChange={(event) => setNotes(event.target.value)} maxLength={1000} rows={4} />
            </label>
          </div>
          <button className="btn btn--primary checkout-page__submit" type="submit" disabled={isSubmitting}>
             {isSubmitting ? 'Preparando pedido...' : 'Continuar al pago'}
          </button>
        </form>

        <aside className="checkout-page__summary">
          <h2>Revisión</h2>
          {lines.map((line) => {
            const product = findMerchProduct(line.productCode)
            if (!product) return null
            return (
              <div className="checkout-page__line" key={`${line.productCode}-${line.size}-${line.customName ?? ''}`}>
                <span>
                  {product.name} · Talla {line.size} × {line.quantity}
                  <small>Personalización: {line.customName ?? 'Sin personalizar'}</small>
                </span>
                <strong>{euro.format(product.price * line.quantity)}</strong>
              </div>
            )
          })}
          <div className="checkout-page__total"><span>Total</span><strong>{euro.format(subtotal)}</strong></div>
           <p className="checkout-page__pending-note">La orden se guardará como PENDING. El pago se realizará fuera de esta página.</p>
        </aside>
      </div>
    </section>
  )
}
