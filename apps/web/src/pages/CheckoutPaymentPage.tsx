import { useEffect, useState } from 'react'
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom'
import { useAuth } from '@/features/auth/context/useAuth'
import { createCheckoutSession, getOrder } from '@/features/merch/services/orders.service'
import type { Order } from '@/features/merch/types/merch.types'
import './CheckoutPage.css'

const euro = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })

export default function CheckoutPaymentPage() {
  const navigate = useNavigate()
  const { id } = useParams()
  const { isAuthenticated, isLoading: authLoading } = useAuth()
  const [order, setOrder] = useState<Order | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    if (!id || !isAuthenticated) return
    let cancelled = false
    void getOrder(Number(id))
      .then((loadedOrder) => {
        if (!cancelled) setOrder(loadedOrder)
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof Error ? err.message : 'No se pudo cargar el pedido')
      })
    return () => { cancelled = true }
  }, [id, isAuthenticated])

  if (authLoading) {
    return <section className="checkout-page checkout-page--empty"><p role="status">Comprobando sesión...</p></section>
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: `/checkout/payment/${id ?? ''}` }} />
  }

  if (error) {
    return <section className="checkout-page checkout-page--empty"><p role="alert">{error}</p><Link className="btn btn--ghost" to="/profile">Ir a mis pedidos</Link></section>
  }

  if (!order) {
    return <section className="checkout-page checkout-page--empty"><p role="status">Cargando revisión...</p></section>
  }

  if (order.paymentStatus === 'PAID') {
    return <Navigate to={`/checkout/success/${order.id}`} replace />
  }

  const pay = async () => {
    setError(null)
    setIsSubmitting(true)
    try {
      const session = await createCheckoutSession(order.id)
      window.location.assign(session.checkoutUrl)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo iniciar el pago')
      setIsSubmitting(false)
    }
  }

  return (
    <section className="checkout-page">
      <header className="checkout-page__header">
        <span className="checkout-page__eyebrow">Merch / Pago</span>
        <h1>Revisa y paga</h1>
        <p>Stripe Checkout se abrirá en una página segura. GuatequeRiders no recibe ni almacena los datos de tu tarjeta.</p>
      </header>

      <div className="checkout-page__steps" aria-label="Pasos del checkout">
        <span>1. Datos</span>
        <span>2. Revisión</span>
        <span className="is-active">3. Pago seguro</span>
      </div>

      {error && <div className="checkout-page__error" role="alert">{error}</div>}

      <div className="checkout-page__layout">
        <div className="checkout-page__form">
          <h2>Pedido #{order.id}</h2>
          <p className="checkout-page__pending-note">Importe calculado por el servidor: {euro.format(order.total)}</p>
          <p className="checkout-page__security-note">Solo se habilitan pagos con tarjeta en Stripe TEST durante esta primera versión.</p>
          <button className="btn btn--primary checkout-page__submit" type="button" onClick={() => void pay()} disabled={isSubmitting}>
            {isSubmitting ? 'Abriendo Stripe...' : `Pagar ${euro.format(order.total)}`}
          </button>
          <button className="btn btn--ghost checkout-page__submit" type="button" onClick={() => navigate(`/checkout/cancel/${order.id}`)} disabled={isSubmitting}>
            Volver sin pagar
          </button>
        </div>

        <aside className="checkout-page__summary">
          <h2>Resumen</h2>
          {order.items.map((item) => (
            <div className="checkout-page__line" key={`${item.id}-${item.customName ?? ''}`}>
              <span>
                {item.productName} · Talla {item.size} × {item.quantity}
                <small>{item.customName ? `Personalización: ${item.customName}` : 'Sin personalizar'}</small>
              </span>
              <strong>{euro.format(item.subtotal)}</strong>
            </div>
          ))}
          <div className="checkout-page__total"><span>Total</span><strong>{euro.format(order.total)}</strong></div>
        </aside>
      </div>
    </section>
  )
}
