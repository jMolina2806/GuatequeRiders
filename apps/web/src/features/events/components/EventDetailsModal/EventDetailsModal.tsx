import Modal from '@/components/Modal'
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

const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Publicado',
  CLOSED: 'Cerrado',
  CANCELLED: 'Cancelado',
}

const STATUS_CLASS: Record<string, string> = {
  DRAFT: 'event-details__status--draft',
  PUBLISHED: 'event-details__status--published',
  CLOSED: 'event-details__status--closed',
  CANCELLED: 'event-details__status--cancelled',
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
              className="event-details__btn event-details__btn--publish"
              onClick={() => onPublish?.(event)}
              type="button"
            >
              Publicar
            </button>
          )}
          {canClose && (
            <button
              className="event-details__btn event-details__btn--close"
              onClick={() => onCloseEvent?.(event)}
              type="button"
            >
              Cerrar
            </button>
          )}
          {canCancel && (
            <button
              className="event-details__btn event-details__btn--cancel"
              onClick={() => onCancel?.(event)}
              type="button"
            >
              Cancelar
            </button>
          )}
          <button
            className="event-details__btn event-details__btn--edit"
            onClick={() => onEdit?.(event)}
            type="button"
          >
            Editar
          </button>
          <button
            className="event-details__btn event-details__btn--delete"
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
          className="event-details__btn event-details__btn--unregister"
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
        className="event-details__btn event-details__btn--enroll"
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
          <span className={`event-details__status ${STATUS_CLASS[event.status] ?? ''}`}>
            {STATUS_LABELS[event.status] ?? event.status}
          </span>
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
