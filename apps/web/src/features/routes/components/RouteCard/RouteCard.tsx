import type { Route } from '@/features/routes/types/route.types'
import './RouteCard.css'

type RouteCardProps = {
  route: Route
  isAdmin?: boolean
  onEdit?: (route: Route) => void
  onDelete?: (routeId: number) => void
  onClose?: (routeId: number) => void
}

function difficultyLabel(level: string): string {
  switch (level) {
    case 'Fácil':
      return 'route-card__difficulty--fácil'
    case 'Media':
      return 'route-card__difficulty--media'
    case 'Difícil':
      return 'route-card__difficulty--difícil'
    default:
      return ''
  }
}

export default function RouteCard({
  route,
  isAdmin = false,
  onEdit,
  onDelete,
  onClose,
}: RouteCardProps) {
  const isClosed = route.status === 'Cerrada'

  return (
    <article className="route-card">
      <div className="route-card__image">
        {route.imageUrl && (
          <img src={route.imageUrl} alt={route.title} loading="lazy" />
        )}
      </div>

      <div className="route-card__body">
        <div className="route-card__top">
          <h3 className="route-card__title">{route.title}</h3>
          <span
            className={`route-card__status ${
              isClosed ? 'route-card__status--closed' : 'route-card__status--open'
            }`}
          >
            {route.status}
          </span>
        </div>

        <div className="route-card__meta">
          <span>📅 {route.date}</span>
          <span>⏰ {route.time}</span>
          <span>📍 {route.origin} → {route.destination}</span>
          <span>📏 {route.distanceKm} km</span>
          <span
            className={`route-card__difficulty ${difficultyLabel(route.difficulty)}`}
          >
            {route.difficulty}
          </span>
          <span>👥 {route.participants.length}/{route.maxParticipants}</span>
        </div>

        <p className="route-card__desc">{route.description}</p>

        {route.participants.length > 0 && (
          <div className="route-card__participants">
            {route.participants.map((p) => (
              <span key={p.id} className="route-card__participant-tag">
                {p.name}
              </span>
            ))}
          </div>
        )}
      </div>

      <div className="route-card__sidebar">
        {isAdmin ? (
          <>
            <button
              className="route-card__btn route-card__btn--edit"
              onClick={() => onEdit?.(route)}
              type="button"
            >
              Editar
            </button>
            <button
              className="route-card__btn route-card__btn--delete"
              onClick={() => onDelete?.(route.id)}
              type="button"
            >
              Eliminar
            </button>
            {!isClosed && (
              <button
                className="route-card__btn route-card__btn--close"
                onClick={() => onClose?.(route.id)}
                type="button"
              >
                Cerrar
              </button>
            )}
          </>
        ) : !isClosed ? (
          <button
            className="route-card__btn route-card__btn--enroll"
            type="button"
          >
            Apuntarme
          </button>
        ) : null}
      </div>
    </article>
  )
}
