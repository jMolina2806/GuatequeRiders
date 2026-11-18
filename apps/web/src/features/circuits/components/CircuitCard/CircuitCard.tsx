import type { Circuit } from '@/features/circuits/types/circuit.types'
import StatusBadge from '@/components/StatusBadge'
import './CircuitCard.css'

type CircuitCardProps = {
  circuit: Circuit
  isAdmin?: boolean
  onDetails?: (circuit: Circuit) => void
  onEdit?: (circuit: Circuit) => void
  onDelete?: (circuitId: number) => void
  onPublish?: (circuitId: number) => void
  onComplete?: (circuitId: number) => void
  onCancel?: (circuitId: number) => void
}

function formatDate(date: string) {
  return new Intl.DateTimeFormat('es-ES', {
    dateStyle: 'medium',
  }).format(new Date(`${date}T00:00:00`))
}

function formatPrice(price: number | null) {
  if (price === null) return null
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
  }).format(price)
}

export default function CircuitCard({
  circuit,
  isAdmin = false,
  onDetails,
  onEdit,
  onDelete,
  onPublish,
  onComplete,
  onCancel,
}: CircuitCardProps) {
  const timeRange = circuit.endTime
    ? `${circuit.startTime} – ${circuit.endTime}`
    : circuit.startTime
  const price = formatPrice(circuit.price)
  const canEdit = circuit.status === 'DRAFT' || circuit.status === 'PUBLISHED'

  const stopPropagation = (event: React.MouseEvent) => event.stopPropagation()

  return (
    <article className="circuit-card" onClick={() => onDetails?.(circuit)}>
      <div className="circuit-card__image-wrap">
        {circuit.imageUrl ? (
          <img className="circuit-card__image" src={circuit.imageUrl} alt="" />
        ) : (
          <span className="circuit-card__image-fallback" aria-hidden="true">
            🏁
          </span>
        )}
        <StatusBadge status={circuit.status} />
      </div>

      <div className="circuit-card__body">
        <div className="circuit-card__heading">
          <div>
            <span className="circuit-card__eyebrow">{circuit.circuitName}</span>
            <h2 className="circuit-card__title">{circuit.title}</h2>
          </div>
          <span className="circuit-card__location-mark" aria-hidden="true">⌁</span>
        </div>

        <div className="circuit-card__meta">
          <span>📍 {circuit.location}</span>
          <span>📅 {formatDate(circuit.date)}</span>
          <span>🕒 {timeRange}</span>
          {price && <span>💳 {price}</span>}
        </div>

        <p className="circuit-card__description">{circuit.description}</p>

        {(circuit.discount || circuit.benefit) && (
          <div className="circuit-card__benefits">
            {circuit.discount && <span>Descuento: {circuit.discount}</span>}
            {circuit.benefit && <span>Beneficio: {circuit.benefit}</span>}
          </div>
        )}

        <div className="circuit-card__actions" onClick={stopPropagation}>
          <button className="btn btn--ghost btn--sm" onClick={() => onDetails?.(circuit)} type="button">
            Ver detalles
          </button>
          <a
            className="btn btn--primary btn--sm"
            href={circuit.registrationUrl}
            target="_blank"
            rel="noopener noreferrer"
          >
            Inscribirme
          </a>
          {circuit.informationUrl && (
            <a
              className="btn btn--ghost btn--sm"
              href={circuit.informationUrl}
              target="_blank"
              rel="noopener noreferrer"
            >
              Más información
            </a>
          )}
        </div>
      </div>

      {isAdmin && (
        <div className="circuit-card__admin" onClick={stopPropagation}>
          {circuit.status === 'DRAFT' && (
            <button className="btn btn--primary btn--sm" onClick={() => onPublish?.(circuit.id)} type="button">
              Publicar
            </button>
          )}
          {circuit.status === 'PUBLISHED' && (
            <button className="btn btn--ghost btn--sm" onClick={() => onComplete?.(circuit.id)} type="button">
              Cerrar
            </button>
          )}
          {(circuit.status === 'DRAFT' || circuit.status === 'PUBLISHED') && (
            <button className="btn btn--outline-danger btn--sm" onClick={() => onCancel?.(circuit.id)} type="button">
              Cancelar
            </button>
          )}
          {canEdit && (
            <button className="btn btn--secondary btn--sm" onClick={() => onEdit?.(circuit)} type="button">
              Editar
            </button>
          )}
          <button className="btn btn--danger btn--sm" onClick={() => onDelete?.(circuit.id)} type="button">
            Eliminar
          </button>
        </div>
      )}
    </article>
  )
}
