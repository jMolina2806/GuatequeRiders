import ParticipantList from '../ParticipantList'
import type { Route } from '../../types/route.types'
import './RouteCard.css'

type RouteCardProps = {
  route: Route
  isAdminView?: boolean
  onEdit?: (route: Route) => void
  onDelete?: (routeId: number) => void
  onClose?: (routeId: number) => void
}

export default function RouteCard({
  route,
  isAdminView = false,
  onEdit,
  onDelete,
  onClose,
}: RouteCardProps) {
  const isClosed = route.status === 'Cerrada'

  return (
    <article className="route-card">
      <div className="route-card__header">
        <h3 className="route-card__title">{route.title}</h3>
        <span
          className={`route-card__status ${
            isClosed ? 'route-card__status--closed' : 'route-card__status--open'
          }`}
        >
          {route.status}
        </span>
      </div>

      <p className="route-card__info">
        <strong>Fecha:</strong> {route.date}
      </p>
      <p className="route-card__info">
        <strong>Hora:</strong> {route.time}
      </p>
      <p className="route-card__info">
        <strong>Origen:</strong> {route.origin}
      </p>
      <p className="route-card__info">
        <strong>Destino:</strong> {route.destination}
      </p>
      <p className="route-card__info">
        <strong>Distancia:</strong> {route.distanceKm} km
      </p>
      <p className="route-card__info">
        <strong>Dificultad:</strong> {route.difficulty}
      </p>
      <p className="route-card__info">
        <strong>Plazas máximas:</strong> {route.maxParticipants}
      </p>
      <p className="route-card__info">
        <strong>Descripción:</strong> {route.description}
      </p>

      {!isAdminView && (
        <button className="route-card__button">Apuntarme</button>
      )}

      {isAdminView && (
        <div className="route-card__actions">
          <button
            className="route-card__button route-card__button--edit"
            onClick={() => onEdit?.(route)}
            type="button"
          >
            Editar
          </button>

          <button
            className="route-card__button route-card__button--delete"
            onClick={() => onDelete?.(route.id)}
            type="button"
          >
            Eliminar
          </button>

          {!isClosed && (
            <button
              className="route-card__button route-card__button--close"
              onClick={() => onClose?.(route.id)}
              type="button"
            >
              Cerrar ruta
            </button>
          )}
        </div>
      )}

      <ParticipantList participants={route.participants} />
    </article>
  )
}