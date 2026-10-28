import Modal from '@/components/Modal'
import StatusBadge from '@/components/StatusBadge'
import { getGoogleMapsSearchUrl } from '@/utils/googleMaps'
import type { Event } from '../../types/event.types'
import './EventDetailsModal.css'

type EventDetailsModalProps = {
  event: Event | null
  isAdmin?: boolean
  isAuthenticated?: boolean
  isRegistered?: boolean
  isProcessing?: boolean
  onClose: () => void
  onEnroll?: (event: Event) => void
  onUnregister?: (event: Event) => void
  onEdit?: (event: Event) => void
  onDelete?: (eventId: number) => void
  onPublish?: (event: Event) => void
  onCloseEvent?: (event: Event) => void
  onCancel?: (event: Event) => void
}

export default function EventDetailsModal({
  event,
  isAdmin = false,
  isAuthenticated = false,
  isRegistered = false,
  isProcessing = false,
  onClose,
  onEnroll,
  onUnregister,
  onEdit,
  onDelete,
  onPublish,
  onCloseEvent,
  onCancel,
}: EventDetailsModalProps) {
  if (!event) return null

  const limit = event.maxParticipants
  const available = limit !== null ? Math.max(0, limit - event.currentParticipants) : null
  const isFull = limit !== null && event.currentParticipants >= limit
  const searchUrl = getGoogleMapsSearchUrl(event.location)
  const canPublish = event.status === 'DRAFT'
  const canClose = event.status === 'PUBLISHED'
  const canCancel = event.status === 'PUBLISHED' || event.status === 'CLOSED'

  const renderActions = () => {
    if (isAdmin) {
      return (
        <>
          {canPublish && (
            <button
              className="btn btn--primary"
              onClick={() => onPublish?.(event)}
              type="button"
            >
              Publicar
            </button>
          )}
          {canClose && (
            <button
              className="btn btn--ghost"
              onClick={() => onCloseEvent?.(event)}
              type="button"
            >
              Cerrar
            </button>
          )}
          {canCancel && (
            <button
              className="btn btn--outline-danger"
              onClick={() => onCancel?.(event)}
              type="button"
            >
              Cancelar
            </button>
          )}
          <button
            className="btn btn--secondary"
            onClick={() => onEdit?.(event)}
            type="button"
          >
            Editar
          </button>
          <button
            className="btn btn--danger"
            onClick={() => onDelete?.(event.id)}
            type="button"
          >
            Eliminar
          </button>
        </>
      )
    }

    if (event.status !== 'PUBLISHED') return null

    if (isAuthenticated && isRegistered) {
      return (
        <button
          className="btn btn--outline-danger"
          onClick={() => onUnregister?.(event)}
          disabled={isProcessing}
          type="button"
        >
          {isProcessing ? 'Procesando...' : 'Cancelar asistencia'}
        </button>
      )
    }

    return (
      <button
        className="btn btn--primary"
        onClick={() => onEnroll?.(event)}
        disabled={isProcessing || isFull}
        type="button"
      >
        {isProcessing ? 'Procesando...' : isFull ? 'Lleno' : 'Asistiré'}
      </button>
    )
  }

  return (
    <Modal isOpen={event !== null} onClose={onClose} title={event.title}>
      <div className="event-details">
        <div className="event-details__header">
          <StatusBadge status={event.status} />
          <p className="event-details__desc">{event.description}</p>
        </div>

        <div className="event-details__grid">
          <div className="event-details__item">
            <span className="event-details__label">Fecha</span>
            <span className="event-details__value">{event.date}</span>
          </div>
          <div className="event-details__item">
            <span className="event-details__label">Hora inicio</span>
            <span className="event-details__value">{event.startTime}</span>
          </div>
          <div className="event-details__item">
            <span className="event-details__label">Hora fin</span>
            <span className="event-details__value">{event.endTime ?? '—'}</span>
          </div>
          <div className="event-details__item">
            <span className="event-details__label">Ubicación</span>
            <span className="event-details__value">{event.location}</span>
          </div>
          <div className="event-details__item">
            <span className="event-details__label">Asistentes</span>
            <span className="event-details__value">{event.currentParticipants}</span>
          </div>
          <div className="event-details__item">
            <span className="event-details__label">Límite de asistentes</span>
            <span className="event-details__value">{limit ?? 'Sin límite'}</span>
          </div>
          <div className="event-details__item">
            <span className="event-details__label">Plazas disponibles</span>
            <span className="event-details__value">{available ?? 'Sin límite'}</span>
          </div>
        </div>

        <div className="event-details__maps">
          <a
            className="event-details__maps-link"
            href={searchUrl}
            target="_blank"
            rel="noopener noreferrer"
          >
            Ver ubicación en Google Maps
          </a>
        </div>

        <div className="event-details__actions">{renderActions()}</div>
      </div>
    </Modal>
  )
}
