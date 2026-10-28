import { useState } from 'react'
import { Link } from 'react-router-dom'
import Modal from '@/components/Modal'
import { merchProducts } from '@/data/clubData'
import { useAuth } from '@/features/auth/context/useAuth'
import MerchProductCard from '@/features/merch/components/MerchProductCard'
import { useMerch } from '@/features/merch/context/useMerch'
import type { CampaignStatus } from '@/features/merch/types/merch.types'
import './MerchPage.css'

const statusLabels: Record<CampaignStatus, string> = {
  OPEN: 'MERCH ABIERTO',
  CLOSED: 'CAMPAÑA CERRADA',
  ORDERED: 'PEDIDO AL PROVEEDOR',
}

export default function MerchPage() {
  const { isAdmin } = useAuth()
  const { campaign, isOpen, isLoading, error, openCampaign } = useMerch()
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [isOpening, setIsOpening] = useState(false)
  const [feedback, setFeedback] = useState<string | null>(null)

  const canOpenNewCampaign = !isLoading && (!campaign || campaign.status === 'CLOSED' || campaign.status === 'ORDERED')
  const status = campaign?.status
  const statusLabel = isLoading ? 'COMPROBANDO...' : status ? statusLabels[status] : 'SIN CAMPAÑA'

  const handleOpenCampaign = async () => {
    setIsOpening(true)
    setFeedback(null)
    try {
      const newCampaign = await openCampaign()
      setFeedback(`Campaña #${newCampaign.id} abierta correctamente.`)
      setConfirmOpen(false)
    } catch (err) {
      setFeedback(err instanceof Error ? err.message : 'No se pudo abrir una nueva campaña')
    } finally {
      setIsOpening(false)
    }
  }

  return (
    <section className="merch-page">
      <header className="merch-page__header">
        <div className="merch-page__eyebrow">
          <span>Guateque Riders</span>
          <span>Drop 01 / 03 piezas</span>
        </div>
        <div className="merch-page__title-row">
          <h1 className="merch-page__title">Merchandising</h1>
          <span className={`merch-page__state ${status ? `is-${status.toLowerCase()}` : 'is-closed'}`}>
            {statusLabel}
          </span>
        </div>
        <p className="merch-page__subtitle">
          Una colección corta para llevar nuestra energía neón dentro y fuera de la carretera.
        </p>
        {status === 'OPEN' && campaign && (
          <p className="merch-page__campaign-meta">
            Campaña #{campaign.id} · abierta desde {new Date(campaign.openedAt).toLocaleDateString('es-ES')}
          </p>
        )}
        {!isOpen && !isLoading && (
          <p className="merch-page__closed-message">
            {status === 'ORDERED'
              ? 'Esta campaña ya se ha pedido al proveedor. La próxima apertura creará una campaña independiente.'
              : status === 'CLOSED'
                ? 'Esta campaña está cerrada. Las compras volverán a estar disponibles al abrir una nueva campaña.'
                : 'Todavía no hay una campaña abierta para realizar compras.'}
          </p>
        )}
        {error && <p className="merch-page__error" role="alert">{error}</p>}
        {feedback && <p className="merch-page__feedback" role="status">{feedback}</p>}
        <div className="merch-page__highlights" aria-label="Detalles de la colección">
          <span>Diseño oficial</span>
          <span>Fabricación bajo pedido</span>
          <span>Hecho para rodar</span>
        </div>
        {isAdmin && !isLoading && status === 'OPEN' && campaign && (
          <Link className="btn btn--ghost merch-page__admin-toggle" to="/admin/orders">
            Gestionar campaña #{campaign.id}
          </Link>
        )}
        {isAdmin && canOpenNewCampaign && (
          <button className="btn btn--ghost merch-page__admin-toggle" type="button" onClick={() => setConfirmOpen(true)}>
            Abrir nueva campaña
          </button>
        )}
      </header>

      <div className="merch-page__grid">
        {merchProducts.map((product) => (
          <MerchProductCard key={product.id} product={product} isOpen={isOpen && !isLoading} />
        ))}
      </div>

      <footer className="merch-page__footer">
        <div>
          <span className="merch-page__footer-label">Colección en lanzamiento</span>
          <p>Fabricamos cada pieza bajo pedido después de confirmar el pago.</p>
        </div>
        <Link to="/routes" className="btn btn--ghost">
          Ver próximas rutas
        </Link>
      </footer>

      <Modal
        isOpen={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        title="Abrir nueva campaña"
      >
        <div className="merch-page__confirm">
          <p>
            Se creará una campaña OPEN nueva. La campaña anterior permanecerá {status === 'ORDERED' ? 'marcada como pedida al proveedor' : 'cerrada'} y no se reabrirá.
          </p>
          <div className="merch-page__confirm-actions">
            <button className="btn btn--ghost" type="button" onClick={() => setConfirmOpen(false)} disabled={isOpening}>
              Cancelar
            </button>
            <button className="btn btn--primary" type="button" onClick={handleOpenCampaign} disabled={isOpening}>
              {isOpening ? 'Abriendo...' : 'Abrir nueva campaña'}
            </button>
          </div>
        </div>
      </Modal>
    </section>
  )
}
