import type { Event } from '@/features/events/types/event.types'
import './EventCard.css'

type EventCardProps = {
  event: Event
  isAdmin?: boolean
  isAuthenticated?: boolean
  isRegistered?: boolean
  isProcessing?: boolean
  onEdit?: (event: Event) => void
  onDelete?: (eventId: number) => void
  onPublish?: (event: Event) => void
  onClose?: (event: Event) => void
  onCancel?: (event: Event) => void
  onEnroll?: (event: Event) => void
  onUnregister?: (event: Event) => void
}

const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Publicado',
  CLOSED: 'Cerrado',
  CANCELLED: 'Cancelado',
}

const STATUS_CLASS: Record<string, string> = {
  DRAFT: 'event-card__status--draft',
  PUBLISHED: 'event-card__status--published',
  CLOSED: 'event-card__status--closed',
  CANCELLED: 'event-card__status--cancelled',
}

function formatTime(time: string | null): string | null {
  return time ?? null
}

export default function EventCard({
  event,
  isAdmin = false,
  isAuthenticated = false,
  isRegistered = false,
  isProcessing = false,
  onEdit,
  onDelete,
  onPublish,
  onClose,
  onCancel,
  onEnroll,
  onUnregister,
}: EventCardProps) {
  const canPublish = event.status === 'DRAFT'
  const canClose = event.status === 'PUBLISHED'
  const canCancel = event.status === 'PUBLISHED' || event.status === 'CLOSED'
  const isFull =
    event.maxParticipants !== null && event.currentParticipants >= event.maxParticipants
  const endTime = formatTime(event.endTime)
  const timeRange = endTime ? `${event.startTime} – ${endTime}` : event.startTime

  const renderEnrollButton = () => {
    if (isAuthenticated && isRegistered) {
      return (
        <button
          className="event-card__btn event-card__btn--unregister"
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
        className="event-card__btn event-card__btn--enroll"
        onClick={() => onEnroll?.(event)}
        disabled={isProcessing || isFull}
        type="button"
      >
        {isProcessing ? 'Procesando...' : isFull ? 'Lleno' : 'Asistiré'}
      </button>
    )
  }

  return (
    <article className="event-card">
      <div className="event-card__body">
        <div className="event-card__top">
          <h3 className="event-card__title">{event.title}</h3>
          <span className={`event-card__status ${STATUS_CLASS[event.status] ?? ''}`}>
            {STATUS_LABELS[event.status] ?? event.status}
          </span>
        </div>

        <div className="event-card__meta">
          <span>📅 {event.date}</span>
          <span>🕖 {timeRange}</span>
          <span>📍 {event.location}</span>
          <span>
            👥{' '}
            {event.maxParticipants !== null
              ? `${event.currentParticipants}/${event.maxParticipants}`
              : `${event.currentParticipants}`}
          </span>
        </div>

        <p className="event-card__desc">{event.description}</p>
      </div>

      <div className="event-card__sidebar">
        {isAdmin ? (
          <>
            {canPublish && (
              <button
                className="event-card__btn event-card__btn--publish"
                onClick={() => onPublish?.(event)}
                type="button"
              >
                Publicar
              </button>
            )}
            {canClose && (
              <button
                className="event-card__btn event-card__btn--close"
                onClick={() => onClose?.(event)}
                type="button"
              >
                Cerrar
              </button>
            )}
            {canCancel && (
              <button
                className="event-card__btn event-card__btn--cancel"
                onClick={() => onCancel?.(event)}
                type="button"
              >
                Cancelar
              </button>
            )}
            <button
              className="event-card__btn event-card__btn--edit"
              onClick={() => onEdit?.(event)}
              type="button"
            >
              Editar
            </button>
            <button
              className="event-card__btn event-card__btn--delete"
              onClick={() => onDelete?.(event.id)}
              type="button"
            >
              Eliminar
            </button>
          </>
        ) : event.status === 'PUBLISHED' ? (
          renderEnrollButton()
        ) : null}
      </div>
    </article>
  )
}
