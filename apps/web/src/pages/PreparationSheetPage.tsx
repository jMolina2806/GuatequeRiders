import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import logoNeon from '@/assets/branding/logo-neon.jpeg'
import * as merchService from '@/features/merch/services/merch.service'
import type { SupplierOrderBatch } from '@/features/merch/types/merch.types'
import './PreparationSheetPage.css'

const euro = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })
const dateFormatter = new Intl.DateTimeFormat('es-ES', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
})
const dateTimeFormatter = new Intl.DateTimeFormat('es-ES', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

export default function PreparationSheetPage() {
  const { campaignId } = useParams()
  const hasInvalidCampaignId = !campaignId || Number.isNaN(Number(campaignId))
  const [data, setData] = useState<SupplierOrderBatch | null>(null)
  const [isLoading, setIsLoading] = useState(!hasInvalidCampaignId)
  const [error, setError] = useState<string | null>(hasInvalidCampaignId ? 'La campaña indicada no es válida' : null)

  useEffect(() => {
    let cancelled = false
    const id = Number(campaignId)

    if (hasInvalidCampaignId) {
      return () => { cancelled = true }
    }

    const loadSupplierOrder = async () => {
      setIsLoading(true)
      setError(null)
      try {
        const supplierOrder = await merchService.getSupplierOrder(id)
        if (!cancelled) setData(supplierOrder)
      } catch (err) {
        if (!cancelled) setError(err instanceof Error ? err.message : 'No se pudo cargar la hoja definitiva')
      } finally {
        if (!cancelled) setIsLoading(false)
      }
    }

    void loadSupplierOrder()

    return () => { cancelled = true }
  }, [campaignId, hasInvalidCampaignId])

  const handlePrint = () => {
    window.print()
  }

  if (hasInvalidCampaignId) {
    return (
      <section className="preparation-sheet-page preparation-sheet-page--status">
        <p className="preparation-sheet-page__error" role="alert">La campaña indicada no es válida</p>
        <Link className="btn btn--ghost" to="/admin/orders">Volver a campañas</Link>
      </section>
    )
  }

  if (error) {
    return (
      <section className="preparation-sheet-page preparation-sheet-page--status">
        <p className="preparation-sheet-page__error" role="alert">{error}</p>
        <Link className="btn btn--ghost" to="/admin/orders">Volver a campañas</Link>
      </section>
    )
  }

  const requestedCampaignId = Number(campaignId)
  if (isLoading || !data || data.campaignId !== requestedCampaignId) {
    return <section className="preparation-sheet-page preparation-sheet-page--status"><p role="status">Cargando hoja definitiva...</p></section>
  }

  return (
    <section className="preparation-sheet-page">
      <div className="preparation-sheet-toolbar" aria-label="Acciones de la hoja definitiva">
        <Link className="btn btn--ghost" to="/admin/orders">Volver a campañas</Link>
        <button className="btn btn--primary" type="button" onClick={handlePrint}>
          Imprimir / Guardar como PDF
        </button>
      </div>

      <article className="preparation-sheet">
        <header className="preparation-sheet__header">
          <div className="preparation-sheet__brand">
            <img src={logoNeon} alt="Guateque Riders" />
            <div>
              <p className="preparation-sheet__kicker">Campaña Merch #{data.campaignNumber}</p>
              <h1>Pedido de Merch</h1>
            </div>
          </div>
          <div className="preparation-sheet__date">
            <span>Campaña</span>
            <strong>#{data.campaignNumber}</strong>
            <span>Fecha de cierre</span>
            <strong>{dateFormatter.format(new Date(data.closedAt))}</strong>
          </div>
        </header>

        <section className="preparation-sheet__section" aria-labelledby="sheet-summary-title">
          <div className="preparation-sheet__section-heading">
            <div>
              <p className="preparation-sheet__eyebrow">Pedido definitivo</p>
              <h2 id="sheet-summary-title">Resumen por producto y talla</h2>
            </div>
            <strong>{data.totalUnits} prendas · {euro.format(data.totalAmount)}</strong>
          </div>

          <div className="preparation-sheet__products">
            {data.productSummary.map((product) => {
              const productLines = data.summary.filter((line) => line.productCode === product.productCode)
              return (
                <section className="preparation-sheet__product" key={product.productCode}>
                  <h3>{product.productName}</h3>
                  <div className="preparation-sheet__sizes">
                    {productLines.map((line) => (
                      <div key={`${line.productCode}-${line.size}`}>
                        <span>{line.size}</span>
                        <strong>{line.quantity}</strong>
                      </div>
                    ))}
                  </div>
                  <p>Total {product.productName}: <strong>{product.quantity}</strong></p>
                </section>
              )
            })}
          </div>

          <div className="preparation-sheet__grand-total">
            <span>Total prendas</span>
            <strong>{data.totalUnits}</strong>
          </div>
        </section>

        {data.personalizations.length > 0 && (
          <section className="preparation-sheet__section" aria-labelledby="sheet-personalizations-title">
            <div className="preparation-sheet__section-heading">
              <div>
                <p className="preparation-sheet__eyebrow">Fabricación</p>
                <h2 id="sheet-personalizations-title">Personalizaciones</h2>
              </div>
              <strong>{data.personalizations.length} líneas</strong>
            </div>
            <div className="preparation-sheet__personalizations">
              {data.personalizations.map((personalization) => (
                <div
                  className="preparation-sheet__personalization"
                  key={`${personalization.orderId}-${personalization.productCode}-${personalization.size}-${personalization.customName}`}
                >
                  <div>
                    <strong>{personalization.productName} / {personalization.size} / {personalization.customName}</strong>
                    <span>Pedido #{personalization.orderId}</span>
                  </div>
                  <strong>{personalization.quantity}</strong>
                </div>
              ))}
            </div>
          </section>
        )}

        <section className="preparation-sheet__section preparation-sheet__details" aria-labelledby="sheet-details-title">
          <div className="preparation-sheet__section-heading">
            <div>
              <p className="preparation-sheet__eyebrow">Pedidos incluidos</p>
              <h2 id="sheet-details-title">Detalle</h2>
            </div>
            <strong>{data.orders.length} pedidos</strong>
          </div>

          {data.orders.length === 0 ? (
            <p className="preparation-sheet__empty">No hay pedidos pagados en este pedido definitivo.</p>
          ) : (
            <div className="preparation-sheet__orders">
              {data.orders.map((order) => (
                <article className="preparation-sheet__order" key={order.orderId}>
                  <header>
                    <div>
                      <h3>Pedido #{order.orderId}</h3>
                      <p>Cliente: <strong>{order.customerName}</strong></p>
                    </div>
                    <time dateTime={order.orderCreatedAt}>{dateTimeFormatter.format(new Date(order.orderCreatedAt))}</time>
                  </header>
                  <div className="preparation-sheet__order-items">
                    {order.items.map((item) => (
                      <div className="preparation-sheet__order-item" key={`${order.orderId}-${item.productCode}-${item.size}-${item.customName ?? ''}`}>
                        <div>
                          <strong>{item.productName}</strong>
                          <span>Talla: {item.size} · Cantidad: {item.quantity}</span>
                          {item.customName && <span>Personalización: {item.customName}</span>}
                        </div>
                        <div>
                          <span>{euro.format(item.unitPrice)} / unidad</span>
                          <strong>{euro.format(item.subtotal)}</strong>
                        </div>
                      </div>
                    ))}
                  </div>
                  <p className="preparation-sheet__order-total">Total pedido: <strong>{euro.format(order.total)}</strong></p>
                </article>
              ))}
            </div>
          )}
        </section>

        <footer className="preparation-sheet__footer">
          <span>Documento generado desde el pedido definitivo de la campaña.</span>
          <span>Guateque Riders · Fabricación bajo pedido</span>
        </footer>
      </article>
    </section>
  )
}
