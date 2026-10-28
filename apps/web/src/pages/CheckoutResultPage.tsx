import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getOrder } from '@/features/merch/services/orders.service'
import type { Order } from '@/features/merch/types/merch.types'
import './CheckoutPage.css'

const euro = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })

type CheckoutResultPageProps = {
  result: 'success' | 'cancel'
}

export default function CheckoutResultPage({ result }: CheckoutResultPageProps) {
  const { id } = useParams()
  const [order, setOrder] = useState<Order | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isChecking, setIsChecking] = useState(result === 'success')

  useEffect(() => {
    if (!id) return
    let cancelled = false
    let timeout: number | undefined
    let attempts = 0

    const load = async () => {
      try {
        const loadedOrder = await getOrder(Number(id))
        if (cancelled) return
        setOrder(loadedOrder)
        if (result === 'success' && loadedOrder.paymentStatus === 'PENDING' && attempts < 7) {
          attempts += 1
          timeout = window.setTimeout(() => void load(), Math.min(1000 * 2 ** attempts, 8000))
        } else {
          setIsChecking(false)
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'No se pudo consultar el pedido')
          setIsChecking(false)
        }
      }
    }

    void load()
    return () => {
      cancelled = true
      if (timeout !== undefined) window.clearTimeout(timeout)
    }
  }, [id, result])

  if (error) {
    return <section className="checkout-page checkout-page--empty"><p role="alert">{error}</p><Link className="btn btn--ghost" to="/profile">Ir a mis pedidos</Link></section>
  }

  if (!order) {
    return <section className="checkout-page checkout-page--empty"><p role="status">{result === 'success' ? 'Verificando pago...' : 'Cargando pedido...'}</p></section>
  }

  const paid = order.paymentStatus === 'PAID'
  const cancelled = result === 'cancel'

  return (
    <section className="checkout-page checkout-page--empty">
      <span className="checkout-page__step">Pedido #{order.id}</span>
      <h1>{cancelled ? 'Pago no completado' : paid ? 'Pago confirmado' : isChecking ? 'Verificando pago...' : 'Pago pendiente'}</h1>
      <p>
        {cancelled
          ? 'No se ha confirmado ningún pago. El pedido permanece pendiente y puedes intentarlo de nuevo.'
          : paid
            ? 'El webhook firmado de Stripe ha confirmado el pago.'
            : 'Stripe ha vuelto a la aplicación, pero el backend todavía no ha confirmado el pago.'}
      </p>
      <strong className="checkout-page__pending-total">Total: {euro.format(order.total)}</strong>
      {!paid && !cancelled && <p className="checkout-page__pending-note">Verificando pago... Esta página no cambia el estado del pedido.</p>}
      <div className="checkout-page__pending-actions">
        {!paid && order.paymentStatus === 'PENDING' && <Link className="btn btn--primary" to={`/checkout/payment/${order.id}`}>Intentar pagar</Link>}
        <Link className="btn btn--ghost" to="/profile">Ver mis pedidos</Link>
        <Link className="btn btn--ghost" to="/merch">Volver a Merch</Link>
      </div>
    </section>
  )
}
