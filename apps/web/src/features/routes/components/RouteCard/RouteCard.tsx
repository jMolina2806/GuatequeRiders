import type { Route } from '@/features/routes/types/route.types'
import './RouteCard.css'

type RouteCardProps = {
  route: Route
  isAdmin?: boolean
  onEdit?: (route: Route) => void
  onDelete?: (routeId: number) => void
  onClose?: (routeId: number) => void
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
  onEdit,
  onDelete,
  onClose,
}: RouteCardProps) {
  const canClose = route.status === 'PUBLISHED' || route.status === 'IN_PROGRESS'

  return (
    <article className="route-card">
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
          <span>📍 {route.origin} → {route.destination}</span>
          <span>📏 {route.distanceKm} km</span>
          <span
            className={`route-card__difficulty ${DIFFICULTY_CLASS[route.difficulty] ?? ''}`}
          >
            {DIFFICULTY_LABELS[route.difficulty] ?? route.difficulty}
          </span>
          <span>👥 {route.currentParticipants}/{route.maxParticipants}</span>
        </div>

        <p className="route-card__desc">{route.description}</p>
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
            {canClose && (
              <button
                className="route-card__btn route-card__btn--close"
                onClick={() => onClose?.(route.id)}
                type="button"
              >
                Cerrar
              </button>
            )}
          </>
        ) : route.status === 'PUBLISHED' ? (
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
