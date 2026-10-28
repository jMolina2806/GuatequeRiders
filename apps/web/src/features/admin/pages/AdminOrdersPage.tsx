import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Modal from '@/components/Modal'
import { useMerch } from '@/features/merch/context/useMerch'
import * as merchService from '@/features/merch/services/merch.service'
import type {
  Campaign,
  CampaignDashboard,
  CampaignHistoryEntry,
  CampaignStatus,
  Order,
  OrderStatus,
  PreparationOrders,
  SupplierOrderBatch,
} from '@/features/merch/types/merch.types'
import './AdminOrdersPage.css'

const euro = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })
const dateFormatter = new Intl.DateTimeFormat('es-ES', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

const statusLabels: Record<CampaignStatus, string> = {
  OPEN: 'Abierta',
  CLOSED: 'Cerrada',
  ORDERED: 'Pedido al proveedor',
}

const orderStatusLabels: Record<OrderStatus, string> = {
  PENDING: 'Pendiente',
  CONFIRMED: 'Confirmado',
  IN_PRODUCTION: 'En producción',
  READY: 'Listo para entregar',
  DELIVERED: 'Entregado',
  CANCELLED: 'Cancelado',
}

const nextOrderActionLabels: Partial<Record<OrderStatus, string>> = {
  CONFIRMED: 'Marcar en producción',
  IN_PRODUCTION: 'Marcar como listo',
  READY: 'Marcar como entregado',
}

function formatDate(value: string | null) {
  return value ? dateFormatter.format(new Date(value)) : 'Pendiente'
}

function CampaignStatusBadge({ status }: { status: CampaignStatus }) {
  return <span className={`admin-campaign-status admin-campaign-status--${status.toLowerCase()}`}>{statusLabels[status]}</span>
}

function PreparationSummary({ preparation }: { preparation: PreparationOrders }) {
  return (
    <>
      <div className="admin-campaign-product-summary">
        {preparation.productSummary.map((product) => (
          <article className="admin-campaign-product-summary__card" key={product.productCode}>
            <span>{product.productName}</span>
            <strong>{product.quantity}</strong>
            <small>prendas</small>
          </article>
        ))}
      </div>
      <div className="admin-campaign-size-summary" aria-label="Resumen por producto y talla">
        {preparation.summary.map((line) => (
          <div className="admin-campaign-size-summary__line" key={`${line.productCode}-${line.size}`}>
            <span>{line.productName} · {line.size}</span>
            <strong>{line.quantity}</strong>
          </div>
        ))}
      </div>
      <p className="admin-campaign-section__note">
        {preparation.orders.length} pedidos pagados · {preparation.totalUnits} prendas incluidas.
      </p>
    </>
  )
}

function OrderCard({
  order,
  excluded = false,
  canAdvance = false,
  advancing = false,
  onAdvance,
}: {
  order: Order
  excluded?: boolean
  canAdvance?: boolean
  advancing?: boolean
  onAdvance?: (order: Order) => void
}) {
  const nextActionLabel = nextOrderActionLabels[order.status]

  return (
    <article className={`admin-order-card${excluded ? ' admin-order-card--excluded' : ''}`}>
      <div className="admin-order-card__header">
        <div>
          <span className="admin-order-card__number">Pedido #{order.id}</span>
          <h3>{order.customerName}</h3>
          <div className="admin-order-card__contact">
            <a href={`mailto:${order.customerEmail}`}>{order.customerEmail}</a>
            {order.customerPhone && <a href={`tel:${order.customerPhone}`}>{order.customerPhone}</a>}
          </div>
        </div>
        <div className="admin-order-card__statuses">
          <span className={order.latePayment
            ? 'admin-order-card__late-status'
            : excluded
              ? 'admin-order-card__excluded-status'
              : 'admin-order-card__paid'}>
            {order.latePayment ? 'PAGO POSTERIOR AL CIERRE' : excluded ? order.paymentStatus : 'PAID'}
          </span>
          {!excluded && (
            <span className={`admin-order-card__fulfillment admin-order-card__fulfillment--${order.status.toLowerCase().replace('_', '-')}`}>
              {orderStatusLabels[order.status]}
            </span>
          )}
        </div>
      </div>
      <div className="admin-order-card__items">
        {order.items.map((item) => (
          <div className="admin-order-card__item" key={item.id}>
            <div>
              <strong>{item.productName}</strong>
              <span>Talla: {item.size} · Cantidad: {item.quantity}</span>
              <span>Personalización: {item.customName ?? 'Sin personalizar'}</span>
            </div>
            <div className="admin-order-card__item-prices">
              <span>{euro.format(item.unitPrice)} / unidad</span>
              <strong>{euro.format(item.subtotal)}</strong>
            </div>
          </div>
        ))}
      </div>
      {order.notes && <p className="admin-order-card__notes">Notas: {order.notes}</p>}
      <div className="admin-order-card__footer">
        <span>{formatDate(order.createdAt)}</span>
        <div className="admin-order-card__footer-actions">
          <strong>Total: {euro.format(order.total)}</strong>
          {canAdvance && nextActionLabel && onAdvance && (
            <button
              className="btn btn--secondary btn--sm"
              type="button"
              onClick={() => onAdvance(order)}
              disabled={advancing}
            >
              {advancing ? 'Actualizando...' : nextActionLabel}
            </button>
          )}
        </div>
      </div>
    </article>
  )
}

function SupplierOrderSummary({ supplierOrder }: { supplierOrder: SupplierOrderBatch }) {
  return (
    <section className="admin-campaign-panel admin-campaign-panel--final" aria-labelledby="supplier-order-title">
      <div className="admin-campaign-panel__heading">
        <div>
          <span className="admin-orders-page__eyebrow">Documento congelado</span>
          <h3 id="supplier-order-title">Pedido definitivo al proveedor</h3>
        </div>
        <span className="admin-campaign-panel__label">Campaña #{supplierOrder.campaignNumber}</span>
      </div>
      <div className="admin-campaign-final-summary">
        <div><span>Campaña</span><strong>#{supplierOrder.campaignNumber}</strong></div>
        <div><span>Pedidos</span><strong>{supplierOrder.orders.length}</strong></div>
        <div><span>Prendas</span><strong>{supplierOrder.totalUnits}</strong></div>
        <div><span>Importe</span><strong>{euro.format(supplierOrder.totalAmount)}</strong></div>
      </div>
      <p className="admin-campaign-section__note">Cerrada el {formatDate(supplierOrder.closedAt)}. La hoja usa este pedido definitivo.</p>
      {supplierOrder.personalizations.length > 0 && (
        <div className="admin-campaign-personalizations">
          <div className="admin-campaign-panel__subheading">
            <h4>Personalizaciones</h4>
            <span>{supplierOrder.personalizations.length}</span>
          </div>
          <div className="admin-campaign-personalizations__list">
            {supplierOrder.personalizations.map((personalization) => (
              <div
                className="admin-campaign-personalizations__line"
                key={`${personalization.orderId}-${personalization.productCode}-${personalization.size}-${personalization.customName}`}
              >
                <span>{personalization.productName} / {personalization.size} / {personalization.customName}</span>
                <strong>{personalization.quantity}</strong>
              </div>
            ))}
          </div>
        </div>
      )}
    </section>
  )
}

export default function AdminOrdersPage() {
  const { refresh: refreshMerch } = useMerch()
  const [campaigns, setCampaigns] = useState<CampaignHistoryEntry[]>([])
  const [selectedCampaignId, setSelectedCampaignId] = useState<number | null>(null)
  const [dashboard, setDashboard] = useState<CampaignDashboard | null>(null)
  const [isHistoryLoading, setIsHistoryLoading] = useState(true)
  const [isDashboardLoading, setIsDashboardLoading] = useState(false)
  const [historyError, setHistoryError] = useState<string | null>(null)
  const [dashboardError, setDashboardError] = useState<string | null>(null)
  const [reloadToken, setReloadToken] = useState(0)
  const [isCloseModalOpen, setIsCloseModalOpen] = useState(false)
  const [action, setAction] = useState<'close' | 'ordered' | null>(null)
  const [advancingOrderId, setAdvancingOrderId] = useState<number | null>(null)
  const [feedback, setFeedback] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setIsHistoryLoading(true)
    setHistoryError(null)

    merchService.getCampaigns()
      .then((history) => {
        if (cancelled) return
        const sortedHistory = [...history].sort((left, right) => (
          new Date(right.campaign.openedAt).getTime() - new Date(left.campaign.openedAt).getTime()
        ))
        setCampaigns(sortedHistory)
        setSelectedCampaignId((currentId) => {
          if (currentId !== null && sortedHistory.some((entry) => entry.campaign.id === currentId)) return currentId
          return sortedHistory.find((entry) => entry.campaign.status === 'OPEN')?.campaign.id
            ?? sortedHistory[0]?.campaign.id
            ?? null
        })
      })
      .catch((err) => {
        if (!cancelled) setHistoryError(err instanceof Error ? err.message : 'No se pudo cargar el historial de campañas')
      })
      .finally(() => {
        if (!cancelled) setIsHistoryLoading(false)
      })

    return () => { cancelled = true }
  }, [reloadToken])

  useEffect(() => {
    let cancelled = false

    if (selectedCampaignId === null) {
      setIsDashboardLoading(false)
      return () => { cancelled = true }
    }

    const loadDashboard = async (initialLoad: boolean) => {
      if (initialLoad) {
        setDashboard(null)
        setDashboardError(null)
        setIsDashboardLoading(true)
      }
      try {
        const campaignDashboard = await merchService.getCampaignDashboard(selectedCampaignId)
        if (!cancelled) {
          setDashboard(campaignDashboard)
          setDashboardError(null)
        }
      } catch (err) {
        if (!cancelled && initialLoad) {
          setDashboardError(err instanceof Error ? err.message : 'No se pudo cargar la campaña')
        }
      } finally {
        if (!cancelled && initialLoad) setIsDashboardLoading(false)
      }
    }

    void loadDashboard(true)
    const refreshInterval = window.setInterval(() => {
      void loadDashboard(false)
    }, 5000)

    return () => {
      cancelled = true
      window.clearInterval(refreshInterval)
    }
  }, [selectedCampaignId, reloadToken])

  const selectedHistoryEntry = campaigns.find((entry) => entry.campaign.id === selectedCampaignId) ?? null
  const selectedCampaign: Campaign | null = dashboard?.campaign.id === selectedCampaignId
    ? dashboard.campaign
    : selectedHistoryEntry?.campaign ?? null
  const selectedDashboard = dashboard?.campaign.id === selectedCampaignId ? dashboard : null

  const handleSelectCampaign = (id: number) => {
    setFeedback(null)
    setDashboard(null)
    setDashboardError(null)
    setSelectedCampaignId(id)
  }

  const handleReload = () => {
    setFeedback(null)
    setDashboard(null)
    setReloadToken((value) => value + 1)
  }

  const handleCloseCampaign = async () => {
    if (selectedCampaignId === null) return
    setAction('close')
    setFeedback(null)
    try {
      await merchService.closeCampaign(selectedCampaignId)
      await refreshMerch()
      setIsCloseModalOpen(false)
      setDashboard(null)
      setFeedback('Pedido Merch cerrado y resumen definitivo generado correctamente.')
      setReloadToken((value) => value + 1)
    } catch (err) {
      setFeedback(err instanceof Error ? err.message : 'No se pudo cerrar el pedido Merch')
    } finally {
      setAction(null)
    }
  }

  const handleMarkOrdered = async () => {
    if (selectedCampaignId === null) return
    setAction('ordered')
    setFeedback(null)
    try {
      await merchService.markCampaignOrdered(selectedCampaignId)
      await refreshMerch()
      setDashboard(null)
      setFeedback('Campaña marcada como pedido al proveedor.')
      setReloadToken((value) => value + 1)
    } catch (err) {
      setFeedback(err instanceof Error ? err.message : 'No se pudo marcar la campaña como pedida')
    } finally {
      setAction(null)
    }
  }

  const handleAdvanceOrder = async (order: Order) => {
    setAdvancingOrderId(order.id)
    setFeedback(null)
    try {
      const updatedOrder = await merchService.advanceOrderStatus(order.id)
      setDashboard((current) => current ? {
        ...current,
        fulfillmentOrders: current.fulfillmentOrders.map((item) => (
          item.id === updatedOrder.id ? updatedOrder : item
        )),
      } : current)
      setFeedback(`Pedido #${updatedOrder.id}: ${orderStatusLabels[updatedOrder.status]}.`)
    } catch (err) {
      setFeedback(err instanceof Error ? err.message : 'No se pudo actualizar el estado del pedido')
    } finally {
      setAdvancingOrderId(null)
    }
  }

  return (
    <section className="admin-orders-page">
      <header className="admin-orders-page__header">
        <div>
          <span className="admin-orders-page__eyebrow">Admin / Merch</span>
          <h1>Gestión de campañas</h1>
          <p>Controla cada campaña sin mezclar pedidos de periodos distintos.</p>
        </div>
        <div className="admin-orders-page__actions">
          <button className="btn btn--ghost" type="button" onClick={handleReload} disabled={isHistoryLoading || action !== null}>
            Actualizar historial
          </button>
        </div>
      </header>

      {historyError && <p className="admin-orders-page__error" role="alert">{historyError}</p>}
      {feedback && <p className="admin-orders-page__feedback" role="status">{feedback}</p>}

      {isHistoryLoading && campaigns.length === 0 ? (
        <p className="admin-orders-page__empty" role="status">Cargando historial de campañas...</p>
      ) : campaigns.length === 0 ? (
        <p className="admin-orders-page__empty">Todavía no hay campañas Merch.</p>
      ) : (
        <div className="admin-campaign-layout">
          <aside className="admin-campaign-history" aria-label="Historial de campañas">
            <div className="admin-campaign-history__heading">
              <div>
                <span className="admin-orders-page__eyebrow">Historial</span>
                <h2>Campañas Merch</h2>
              </div>
              <span>{campaigns.length}</span>
            </div>
            <div className="admin-campaign-history__list">
              {campaigns.map((entry) => (
                <button
                  className={`admin-campaign-history__item${entry.campaign.id === selectedCampaignId ? ' is-selected' : ''}`}
                  key={entry.campaign.id}
                  type="button"
                  onClick={() => handleSelectCampaign(entry.campaign.id)}
                >
                  <span className="admin-campaign-history__item-topline">
                    <strong>Campaña #{entry.campaign.id}</strong>
                    <CampaignStatusBadge status={entry.campaign.status} />
                  </span>
                  <span className="admin-campaign-history__item-date">Abierta {formatDate(entry.campaign.openedAt)}</span>
                  <span className="admin-campaign-history__item-stats">
                    {entry.paidOrders}/{entry.totalOrders} pagados · {entry.totalUnits} prendas
                  </span>
                </button>
              ))}
            </div>
          </aside>

          <div className="admin-campaign-detail">
            {dashboardError ? (
              <div className="admin-campaign-detail__status">
                <p className="admin-orders-page__error" role="alert">{dashboardError}</p>
                <button className="btn btn--ghost" type="button" onClick={handleReload}>Reintentar</button>
              </div>
            ) : isDashboardLoading || !selectedDashboard || !selectedCampaign ? (
              <p className="admin-orders-page__empty" role="status">Cargando datos de la campaña...</p>
            ) : (
              <>
                <header className="admin-campaign-detail__header">
                  <div>
                    <span className="admin-orders-page__eyebrow">Campaña #{selectedCampaign.id}</span>
                    <h2>{statusLabels[selectedCampaign.status]}</h2>
                    <p>
                      Abierta {formatDate(selectedCampaign.openedAt)} · Cerrada {formatDate(selectedCampaign.closedAt)}
                    </p>
                  </div>
                  <div className="admin-campaign-detail__header-actions">
                    <CampaignStatusBadge status={selectedCampaign.status} />
                    {selectedCampaign.status === 'OPEN' && (
                      <button className="btn btn--primary" type="button" onClick={() => setIsCloseModalOpen(true)}>
                        Cerrar pedido Merch
                      </button>
                    )}
                    {selectedCampaign.status !== 'OPEN' && (
                      <Link className="btn btn--primary" to={`/admin/orders/preparation-sheet/${selectedCampaign.id}`}>
                        Ver hoja definitiva
                      </Link>
                    )}
                    {selectedCampaign.status === 'CLOSED' && (
                      <button className="btn btn--secondary" type="button" onClick={handleMarkOrdered} disabled={action !== null}>
                        {action === 'ordered' ? 'Guardando...' : 'Marcar como pedido al proveedor'}
                      </button>
                    )}
                  </div>
                </header>

                <section className="admin-campaign-stats" aria-label="Estadísticas de la campaña">
                  <div><span>Total pedidos</span><strong>{selectedDashboard.stats.totalOrders}</strong></div>
                  <div><span>Pagados</span><strong>{selectedDashboard.stats.paidOrders}</strong></div>
                  <div><span>Pendientes</span><strong>{selectedDashboard.stats.pendingOrders}</strong></div>
                  <div><span>Prendas pagadas</span><strong>{selectedDashboard.stats.paidUnits}</strong></div>
                </section>

                {selectedCampaign.status === 'OPEN' && selectedDashboard.provisionalPreparation && (
                  <section className="admin-campaign-panel admin-campaign-panel--provisional" aria-labelledby="provisional-title">
                    <div className="admin-campaign-panel__heading">
                      <div>
                        <span className="admin-orders-page__eyebrow">Antes de cerrar</span>
                        <h3 id="provisional-title">Resumen provisional</h3>
                      </div>
                      <span className="admin-campaign-panel__label">Solo pedidos pagados</span>
                    </div>
                    <PreparationSummary preparation={selectedDashboard.provisionalPreparation} />
                    <div className="admin-campaign-orders">
                      <div className="admin-campaign-panel__subheading">
                        <h4>Pedidos incluidos</h4>
                        <span>{selectedDashboard.provisionalPreparation.orders.length}</span>
                      </div>
                      {selectedDashboard.provisionalPreparation.orders.length === 0 ? (
                        <p className="admin-campaign-section__note">No hay pedidos pagados para incluir.</p>
                      ) : (
                        <div className="admin-orders-list">
                          {selectedDashboard.provisionalPreparation.orders.map((order) => <OrderCard key={order.id} order={order} />)}
                        </div>
                      )}
                    </div>
                  </section>
                )}

                {selectedCampaign.status === 'OPEN' && !selectedDashboard.provisionalPreparation && (
                  <section className="admin-campaign-panel admin-campaign-panel--provisional">
                    <div className="admin-campaign-panel__heading">
                      <div>
                        <span className="admin-orders-page__eyebrow">Antes de cerrar</span>
                        <h3>Resumen provisional</h3>
                      </div>
                    </div>
                    <p className="admin-campaign-section__note">Todavía no hay pedidos pagados para preparar.</p>
                  </section>
                )}

                {selectedDashboard.supplierOrder && selectedCampaign.status !== 'OPEN' && (
                  <SupplierOrderSummary supplierOrder={selectedDashboard.supplierOrder} />
                )}

                {selectedCampaign.status !== 'OPEN' && selectedDashboard.fulfillmentOrders.length > 0 && (
                  <section className="admin-campaign-panel admin-campaign-panel--fulfillment" aria-labelledby="fulfillment-orders-title">
                    <div className="admin-campaign-panel__heading">
                      <div>
                        <span className="admin-orders-page__eyebrow">Seguimiento</span>
                        <h3 id="fulfillment-orders-title">Estado de los pedidos</h3>
                      </div>
                      <span className="admin-campaign-panel__label">{selectedDashboard.fulfillmentOrders.length} pedidos</span>
                    </div>
                    <p className="admin-campaign-section__note">
                      {selectedCampaign.status === 'ORDERED'
                        ? 'Actualiza cada pedido a medida que avance la fabricación y la entrega.'
                        : 'Primero marca la campaña como pedido al proveedor para iniciar el seguimiento.'}
                    </p>
                    <div className="admin-orders-list admin-orders-list--fulfillment">
                      {selectedDashboard.fulfillmentOrders.map((order) => (
                        <OrderCard
                          key={order.id}
                          order={order}
                          canAdvance={selectedCampaign.status === 'ORDERED'}
                          advancing={advancingOrderId === order.id}
                          onAdvance={handleAdvanceOrder}
                        />
                      ))}
                    </div>
                  </section>
                )}

                {selectedDashboard.excludedOrders.length > 0 && (
                  <section className="admin-campaign-panel admin-campaign-panel--excluded" aria-labelledby="excluded-orders-title">
                    <div className="admin-campaign-panel__heading">
                      <div>
                        <span className="admin-orders-page__eyebrow">No incluidos</span>
                        <h3 id="excluded-orders-title">Pedidos no pagados excluidos</h3>
                      </div>
                      <span className="admin-campaign-panel__label">{selectedDashboard.excludedOrders.length} pedidos</span>
                    </div>
                    <p className="admin-campaign-section__note">Estos pedidos no forman parte del pedido al proveedor mientras su pago no esté confirmado.</p>
                    <div className="admin-orders-list">
                      {selectedDashboard.excludedOrders.map((order) => <OrderCard key={order.id} order={order} excluded />)}
                    </div>
                  </section>
                )}

                {selectedDashboard.latePaymentOrders.length > 0 && (
                  <section className="admin-campaign-panel admin-campaign-panel--late" aria-labelledby="late-payments-title">
                    <div className="admin-campaign-panel__heading">
                      <div>
                        <span className="admin-orders-page__eyebrow">Revisión administrativa</span>
                        <h3 id="late-payments-title">Pagos recibidos después del cierre</h3>
                      </div>
                      <span className="admin-campaign-panel__label">No incluidos en el lote</span>
                    </div>
                    <p className="admin-campaign-section__note">
                      El pago refleja la realidad del proveedor, pero el pedido al proveedor permanece congelado.
                    </p>
                    <div className="admin-orders-list">
                      {selectedDashboard.latePaymentOrders.map((order) => <OrderCard key={order.id} order={order} excluded />)}
                    </div>
                  </section>
                )}

                {selectedDashboard.stats.failedOrders + selectedDashboard.stats.cancelledOrders > 0 && (
                  <p className="admin-campaign-detail__footnote">
                    También constan {selectedDashboard.stats.failedOrders} pagos fallidos y {selectedDashboard.stats.cancelledOrders} pedidos cancelados.
                  </p>
                )}
              </>
            )}
          </div>
        </div>
      )}

      {selectedDashboard && selectedCampaign?.status === 'OPEN' && (
        <Modal
          isOpen={isCloseModalOpen}
          onClose={() => {
            if (action === null) setIsCloseModalOpen(false)
          }}
          title="Cerrar pedido Merch"
        >
          <div className="admin-close-confirmation">
            <p>Vas a cerrar la campaña #{selectedCampaign.id}. Revisa el corte antes de generar el pedido definitivo.</p>
            <div className="admin-close-confirmation__stats">
              <div><strong>{selectedDashboard.stats.totalOrders}</strong><span>pedidos totales</span></div>
              <div><strong>{selectedDashboard.stats.paidOrders}</strong><span>pedidos pagados</span></div>
              <div><strong>{selectedDashboard.stats.pendingOrders}</strong><span>pedidos pendientes</span></div>
            </div>
            <p className="admin-close-confirmation__approved">
              Se generará el pedido al proveedor únicamente con los pedidos pagados: {selectedDashboard.stats.paidUnits} prendas. Los pedidos no pagados quedarán excluidos.
            </p>
            <div className="admin-close-confirmation__actions">
              <button className="btn btn--ghost" type="button" onClick={() => setIsCloseModalOpen(false)} disabled={action !== null}>
                Cancelar
              </button>
              <button className="btn btn--primary" type="button" onClick={handleCloseCampaign} disabled={action !== null}>
                {action === 'close' ? 'Cerrando...' : 'Cerrar pedido Merch'}
              </button>
            </div>
          </div>
        </Modal>
      )}
    </section>
  )
}
