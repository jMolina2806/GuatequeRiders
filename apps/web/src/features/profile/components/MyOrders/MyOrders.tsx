import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  cancelOrder,
  getDevelopmentPaymentStatus,
  getMyOrders,
  simulateDevelopmentPayment,
} from '@/features/merch/services/orders.service'
import type { Order } from '@/features/merch/types/merch.types'
import './MyOrders.css'

const euro = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })

export default function MyOrders() {
  const [orders, setOrders] = useState<Order[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [devPaymentsEnabled, setDevPaymentsEnabled] = useState(false)
  const [simulatingOrderId, setSimulatingOrderId] = useState<number | null>(null)
  const [simulationFeedback, setSimulationFeedback] = useState<string | null>(null)

  const loadOrders = async () => {
    try {
      setOrders(await getMyOrders())
      setError(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudieron cargar tus pedidos')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void loadOrders()
    void getDevelopmentPaymentStatus().then(setDevPaymentsEnabled)
  }, [])

  const handleCancel = async (id: number) => {
    try {
      await cancelOrder(id)
      await loadOrders()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo cancelar el pedido')
    }
  }

  const handleSimulatePayment = async (id: number) => {
    if (!window.confirm('Esto simulará una confirmación de pago únicamente en desarrollo.')) return
    setSimulatingOrderId(id)
    setError(null)
    setSimulationFeedback(null)
    try {
      const paidOrder = await simulateDevelopmentPayment(id)
      setOrders((currentOrders) => currentOrders.map((order) => order.id === paidOrder.id ? paidOrder : order))
      setSimulationFeedback(`Pedido #${paidOrder.id} actualizado a PAID.`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo simular el pago')
    } finally {
      setSimulatingOrderId(null)
    }
  }

  return (
    <section className="my-orders">
      <div className="my-orders__header">
        <h2>Mis pedidos</h2>
        <Link to="/merch">Ver Merch →</Link>
      </div>
      {error && <p className="my-orders__error" role="alert">{error}</p>}
      {simulationFeedback && <p className="my-orders__feedback" role="status">{simulationFeedback}</p>}
      {isLoading ? (
        <p className="my-orders__empty">Cargando pedidos...</p>
      ) : orders.length === 0 ? (
        <p className="my-orders__empty">Todavía no tienes pedidos.</p>
      ) : (
        <div className="my-orders__list">
          {orders.map((order) => (
            <article className="my-orders__card" key={order.id}>
              <div>
                <strong>Pedido #{order.id}</strong>
                <span>{new Date(order.createdAt).toLocaleDateString('es-ES')}</span>
              </div>
              <div>
                <span>{order.paymentStatus} · {order.status}</span>
                <strong>{euro.format(order.total)}</strong>
              </div>
              <ul>
                {order.items.map((item) => (
                  <li key={item.id}>
                    {item.productName} · {item.size} × {item.quantity}
                    {item.customName && ` · Personalización: ${item.customName}`}
                  </li>
                ))}
              </ul>
              <div className="my-orders__actions">
                {order.status === 'PENDING' && order.paymentStatus === 'PENDING' && (
                  <Link className="btn btn--primary btn--sm" to={`/checkout/payment/${order.id}`}>
                    Continuar pago
                  </Link>
                )}
                {devPaymentsEnabled && order.paymentStatus === 'PENDING' && (
                  <div className="my-orders__dev-payment">
                    <span className="my-orders__dev-badge">MODO DESARROLLO</span>
                    <button
                      className="btn btn--secondary btn--sm"
                      type="button"
                      onClick={() => handleSimulatePayment(order.id)}
                      disabled={simulatingOrderId !== null}
                    >
                      {simulatingOrderId === order.id ? 'Simulando pago...' : 'Simular pago'}
                    </button>
                  </div>
                )}
                {order.status === 'PENDING' && order.paymentStatus === 'PENDING' && (
                  <button className="btn btn--outline-danger btn--sm" type="button" onClick={() => handleCancel(order.id)}>
                    Cancelar pedido
                  </button>
                )}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
