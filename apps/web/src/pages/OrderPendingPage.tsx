import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getOrder } from '@/features/merch/services/orders.service'
import type { Order } from '@/features/merch/types/merch.types'
import './CheckoutPage.css'

const euro = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })

export default function OrderPendingPage() {
  const { id } = useParams()
  const [order, setOrder] = useState<Order | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    if (!id) return () => { cancelled = true }

    void getOrder(Number(id))
      .then((loadedOrder) => {
        if (!cancelled) setOrder(loadedOrder)
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof Error ? err.message : 'No se pudo cargar el pedido')
      })

    return () => { cancelled = true }
  }, [id])

  if (error) return <section className="checkout-page checkout-page--empty"><p role="alert">{error}</p><Link className="btn btn--ghost" to="/profile">Ir a mi perfil</Link></section>
  if (!order) return <section className="checkout-page checkout-page--empty"><p role="status">Cargando pedido...</p></section>

  return (
    <section className="checkout-page checkout-page--empty">
      <span className="checkout-page__step">Pedido #{order.id}</span>
      <h1>{order.paymentStatus === 'PAID' ? 'Pago confirmado' : 'Pago pendiente'}</h1>
      <p>
        {order.paymentStatus === 'PAID'
          ? 'El pedido está pagado y aparecerá en el resumen de la campaña.'
          : 'Tu pedido existe, pero todavía no es una compra pagada ni confirmada.'}
      </p>
      <strong className="checkout-page__pending-total">Total: {euro.format(order.total)}</strong>
      {order.paymentStatus === 'PENDING' && (
        <p className="checkout-page__pending-note">El carrito se ha conservado. No se ha realizado ningún cargo.</p>
      )}
      <div className="checkout-page__pending-actions">
        <Link className="btn btn--primary" to="/profile">Ver mis pedidos</Link>
        <Link className="btn btn--ghost" to="/merch">Volver a Merch</Link>
      </div>
    </section>
  )
}
