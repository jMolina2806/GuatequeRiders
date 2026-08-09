import type { Route } from '@/features/routes/types/route.types'
import './RouteCard.css'

type RouteCardProps = {
  route: Route
  isAdmin?: boolean
  isAuthenticated?: boolean
  isRegistered?: boolean
  isProcessing?: boolean
  onDetails?: (route: Route) => void
  onEdit?: (route: Route) => void
  onDelete?: (routeId: number) => void
  onClose?: (routeId: number) => void
  onPublish?: (routeId: number) => void
  onEnroll?: (route: Route) => void
  onUnregister?: (route: Route) => void
}

const DIFFICULTY_LABELS: Record<string, string> = {
  EASY: 'Fácil',
  MODERATE: 'Media',
  HARD: 'Difícil',
  EXTREME: 'Extrema',
}

const DIFFICULTY_CLASS: Record<string, string> = {
  EASY: 'route-card__difficulty--easy',
  MODERATE: 'route-card__difficulty--moderate',
  HARD: 'route-card__difficulty--hard',
  EXTREME: 'route-card__difficulty--extreme',
}

const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Abierta',
  IN_PROGRESS: 'En curso',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
}

const STATUS_CLASS: Record<string, string> = {
  DRAFT: 'route-card__status--draft',
  PUBLISHED: 'route-card__status--open',
  IN_PROGRESS: 'route-card__status--in-progress',
  COMPLETED: 'route-card__status--completed',
  CANCELLED: 'route-card__status--cancelled',
}

export default function RouteCard({
  route,
  isAdmin = false,
  isAuthenticated = false,
  isRegistered = false,
  isProcessing = false,
  onDetails,
  onEdit,
  onDelete,
  onClose,
  onPublish,
  onEnroll,
  onUnregister,
}: RouteCardProps) {
  const canClose = route.status === 'PUBLISHED' || route.status === 'IN_PROGRESS'
  const isFull = route.currentParticipants >= route.maxParticipants

  const handleDetails = () => {
    onDetails?.(route)
  }

  const renderOrganizers = () => {
    if (route.organizers.length === 0) return null
    const [first] = route.organizers
    const label =
      route.organizers.length === 1
        ? `Organizador: ${first.username}`
        : `Organizadores: ${first.username} +${route.organizers.length - 1}`
    return <span className="route-card__organizers">🧭 {label}</span>
  }

  const renderEnrollButton = () => {
    if (isAuthenticated && isRegistered) {
      return (
        <button
          className="route-card__btn route-card__btn--unregister"
          onClick={(event) => {
            event.stopPropagation()
            onUnregister?.(route)
          }}
          disabled={isProcessing}
          type="button"
        >
          {isProcessing ? 'Procesando...' : 'Desapuntarme'}
        </button>
      )
    }
    return (
      <button
        className="route-card__btn route-card__btn--enroll"
        onClick={(event) => {
          event.stopPropagation()
          onEnroll?.(route)
        }}
        disabled={isProcessing || isFull}
        type="button"
      >
        {isProcessing ? 'Procesando...' : isFull ? 'Llena' : 'Apuntarme'}
      </button>
    )
  }

  return (
    <article className="route-card" onClick={handleDetails}>
      <div className="route-card__body">
        <div className="route-card__top">
          <h3 className="route-card__title">{route.title}</h3>
          <span
            className={`route-card__status ${STATUS_CLASS[route.status] ?? ''}`}
          >
            {STATUS_LABELS[route.status] ?? route.status}
          </span>
        </div>

        <div className="route-card__meta">
          <span>📅 {route.date}</span>
          {route.meetingTime && <span>🕒 Quedada: {route.meetingTime}</span>}
          {route.departureTime && <span>🏍️ Salida: {route.departureTime}</span>}
          <span>📍 {route.origin} → {route.destination}</span>
          <span>📏 {route.distanceKm} km</span>
          <span
            className={`route-card__difficulty ${DIFFICULTY_CLASS[route.difficulty] ?? ''}`}
          >
            {DIFFICULTY_LABELS[route.difficulty] ?? route.difficulty}
          </span>
          <span>👥 {route.currentParticipants}/{route.maxParticipants}</span>
          {renderOrganizers()}
        </div>

        <p className="route-card__desc">{route.description}</p>

        <button
          className="route-card__details-btn"
          onClick={(event) => {
            event.stopPropagation()
            handleDetails()
          }}
          type="button"
        >
          Ver detalles
        </button>
      </div>

      <div className="route-card__sidebar">
        {isAdmin ? (
          <>
            {route.status === 'DRAFT' && (
              <button
                className="route-card__btn route-card__btn--publish"
                onClick={(event) => {
                  event.stopPropagation()
                  onPublish?.(route.id)
                }}
                type="button"
              >
                Publicar
              </button>
            )}
            <button
              className="route-card__btn route-card__btn--edit"
              onClick={(event) => {
                event.stopPropagation()
                onEdit?.(route)
              }}
              type="button"
            >
              Editar
            </button>
            <button
              className="route-card__btn route-card__btn--delete"
              onClick={(event) => {
                event.stopPropagation()
                onDelete?.(route.id)
              }}
              type="button"
            >
              Eliminar
            </button>
            {canClose && (
              <button
                className="route-card__btn route-card__btn--close"
                onClick={(event) => {
                  event.stopPropagation()
                  onClose?.(route.id)
                }}
                type="button"
              >
                Cerrar
              </button>
            )}
          </>
        ) : route.status === 'PUBLISHED' ? (
          renderEnrollButton()
        ) : null}
      </div>
    </article>
  )
}
