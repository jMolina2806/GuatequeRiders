import { Link } from 'react-router-dom'
import { merchProducts } from '@/data/clubData'
import { useAuth } from '@/features/auth/context/useAuth'
import MerchProductCard from '@/features/merch/components/MerchProductCard'
import { useMerch } from '@/features/merch/context/useMerch'
import type { CampaignStatus } from '@/features/merch/types/merch.types'
import './MerchPage.css'

const statusLabels: Record<CampaignStatus, string> = {
  OPEN: 'CAMPAÑA EN CIERRE',
  CLOSED: 'CAMPAÑA FINALIZADA',
  ORDERED: 'PEDIDO AL PROVEEDOR',
}

export default function MerchPage() {
  const { isAdmin } = useAuth()
  const { campaign, isOpen, isLoading, error } = useMerch()

  const status = campaign?.status
  const statusLabel = isLoading ? 'COMPROBANDO...' : isOpen ? 'CAMPAÑA ABIERTA' : status ? statusLabels[status] : 'SIN VENTAS ACTIVAS'

  return (
    <section className="merch-page">
      <header className="merch-page__header">
        <div className="merch-page__eyebrow">
          <span>Guateque Riders</span>
          <span>{isOpen ? 'Campaña / En curso' : 'Catálogo / Archivo'}</span>
        </div>

        <div className="merch-page__title-row">
          <h1 className="merch-page__title">Merchandising</h1>
          <span className={`merch-page__state ${status ? `is-${status.toLowerCase()}` : 'is-closed'}`}>
            {statusLabel}
          </span>
        </div>

        <p className="merch-page__subtitle">
          {isOpen
            ? 'La campaña de Merch está abierta. Elige tus prendas y realiza tu pedido.'
            : 'Conoce los diseños de merchandising de Guateque Riders, expuestos como catálogo de referencia.'}
        </p>

        <p className="merch-page__closed-message">
          {isOpen
            ? 'Los pedidos y pagos están disponibles durante esta campaña, hasta que el club anuncie su cierre.'
            : 'No hay ventas disponibles. Los productos se muestran únicamente como referencia y no se admiten pedidos, reservas ni pagos.'}
        </p>

        {campaign && (
          <p className="merch-page__campaign-meta">
            Campaña #{campaign.id} · estado: {statusLabel.toLowerCase()}
          </p>
        )}

        {error && <p className="merch-page__error" role="alert">{error}</p>}

        <div className="merch-page__highlights" aria-label="Información de la colección">
          <span>Diseños de Guateque Riders</span>
          <span>{isOpen ? 'Pedidos disponibles' : 'Catálogo informativo'}</span>
          <span>{isOpen ? 'Campaña abierta' : 'Sin venta activa'}</span>
        </div>

        {isAdmin && status === 'OPEN' && campaign && (
          <Link className="btn btn--ghost merch-page__admin-toggle" to="/admin/orders">
            Gestionar cierre de campaña #{campaign.id}
          </Link>
        )}
      </header>

      <div className="merch-page__grid">
        {merchProducts.map((product) => (
          <MerchProductCard key={product.id} product={product} isOpen={isOpen} />
        ))}
      </div>

      <footer className="merch-page__footer">
        <div>
          <span className="merch-page__footer-label">{isOpen ? 'Campaña abierta' : 'Catálogo de referencia'}</span>
          <p>
            {isOpen
              ? 'Las prendas de esta colección pueden pedirse hasta el cierre de la campaña.'
              : 'Estas prendas se muestran para conocer la colección. No existe una campaña de venta abierta.'}
          </p>
        </div>

        <Link to="/routes" className="btn btn--ghost">
          Ver próximas rutas
        </Link>
      </footer>
    </section>
  )
}
