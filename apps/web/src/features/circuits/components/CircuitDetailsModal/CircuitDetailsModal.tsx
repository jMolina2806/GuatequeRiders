import Modal from '@/components/Modal'
import StatusBadge from '@/components/StatusBadge'
import type { Circuit } from '@/features/circuits/types/circuit.types'
import './CircuitDetailsModal.css'

type CircuitDetailsModalProps = {
  circuit: Circuit | null
  isAdmin?: boolean
  onClose: () => void
  onEdit?: (circuit: Circuit) => void
  onDelete?: (circuitId: number) => void
  onPublish?: (circuitId: number) => void
  onComplete?: (circuitId: number) => void
  onCancel?: (circuitId: number) => void
}

function formatDate(date: string) {
  return new Intl.DateTimeFormat('es-ES', { dateStyle: 'long' })
    .format(new Date(`${date}T00:00:00`))
}

function formatPrice(price: number | null) {
  if (price === null) return 'No indicado'
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
  }).format(price)
}

export default function CircuitDetailsModal({
  circuit,
  isAdmin = false,
  onClose,
  onEdit,
  onDelete,
  onPublish,
  onComplete,
  onCancel,
}: CircuitDetailsModalProps) {
  if (!circuit) return null

  const canEdit = circuit.status === 'DRAFT' || circuit.status === 'PUBLISHED'
  const canCancel = circuit.status === 'DRAFT' || circuit.status === 'PUBLISHED'

  const renderAdminActions = () => {
    if (!isAdmin) return null

    return (
      <>
        {circuit.status === 'DRAFT' && (
          <button className="btn btn--primary" onClick={() => onPublish?.(circuit.id)} type="button">
            Publicar
          </button>
        )}
        {circuit.status === 'PUBLISHED' && (
          <button className="btn btn--ghost" onClick={() => onComplete?.(circuit.id)} type="button">
            Cerrar actividad
          </button>
        )}
        {canCancel && (
          <button className="btn btn--outline-danger" onClick={() => onCancel?.(circuit.id)} type="button">
            Cancelar
          </button>
        )}
        {canEdit && (
          <button className="btn btn--secondary" onClick={() => onEdit?.(circuit)} type="button">
            Editar
          </button>
        )}
        <button className="btn btn--danger" onClick={() => onDelete?.(circuit.id)} type="button">
          Eliminar
        </button>
      </>
    )
  }

  return (
    <Modal isOpen={circuit !== null} onClose={onClose} title={circuit.title}>
      <div className="circuit-details">
        {circuit.imageUrl && (
          <img className="circuit-details__image" src={circuit.imageUrl} alt="" />
        )}

        <div className="circuit-details__header">
          <StatusBadge status={circuit.status} />
          <span className="circuit-details__circuit-name">{circuit.circuitName}</span>
          <p className="circuit-details__description">{circuit.description}</p>
        </div>

        <div className="circuit-details__grid">
          <div className="circuit-details__item">
            <span className="circuit-details__label">Ubicación</span>
            <span className="circuit-details__value">{circuit.location}</span>
          </div>
          <div className="circuit-details__item">
            <span className="circuit-details__label">Fecha</span>
            <span className="circuit-details__value">{formatDate(circuit.date)}</span>
          </div>
          <div className="circuit-details__item">
            <span className="circuit-details__label">Horario</span>
            <span className="circuit-details__value">
              {circuit.startTime}{circuit.endTime ? ` – ${circuit.endTime}` : ''}
            </span>
          </div>
          <div className="circuit-details__item">
            <span className="circuit-details__label">Precio</span>
            <span className="circuit-details__value">{formatPrice(circuit.price)}</span>
          </div>
          {circuit.discount && (
            <div className="circuit-details__item">
              <span className="circuit-details__label">Descuento Guateque Riders</span>
              <span className="circuit-details__value">{circuit.discount}</span>
            </div>
          )}
          {circuit.benefit && (
            <div className="circuit-details__item">
              <span className="circuit-details__label">Beneficio</span>
              <span className="circuit-details__value">{circuit.benefit}</span>
            </div>
          )}
        </div>

        <div className="circuit-details__links">
          <a
            className="btn btn--primary"
            href={circuit.registrationUrl}
            target="_blank"
            rel="noopener noreferrer"
          >
            Inscribirme
          </a>
          {circuit.informationUrl && (
            <a
              className="btn btn--ghost"
              href={circuit.informationUrl}
              target="_blank"
              rel="noopener noreferrer"
            >
              Más información
            </a>
          )}
        </div>

        <div className="circuit-details__actions">{renderAdminActions()}</div>
      </div>
    </Modal>
  )
}
