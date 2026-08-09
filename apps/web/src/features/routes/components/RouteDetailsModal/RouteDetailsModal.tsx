import Modal from '@/components/Modal'
import { getGoogleMapsSearchUrl } from '@/utils/googleMaps'
import type { Route } from '../../types/route.types'
import './RouteDetailsModal.css'

type RouteDetailsModalProps = {
  route: Route | null
  isAdmin?: boolean
  isAuthenticated?: boolean
  isRegistered?: boolean
  isProcessing?: boolean
  onClose: () => void
  onEnroll?: (route: Route) => void
  onUnregister?: (route: Route) => void
  onEdit?: (route: Route) => void
  onDelete?: (routeId: number) => void
  onCloseRoute?: (routeId: number) => void
  onPublish?: (routeId: number) => void
}

const DIFFICULTY_LABELS: Record<string, string> = {
  EASY: 'Fácil',
  MODERATE: 'Media',
  HARD: 'Difícil',
  EXTREME: 'Extrema',
}

const DIFFICULTY_CLASS: Record<string, string> = {
  EASY: 'route-details__badge--easy',
  MODERATE: 'route-details__badge--moderate',
  HARD: 'route-details__badge--hard',
  EXTREME: 'route-details__badge--extreme',
}

const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Abierta',
  IN_PROGRESS: 'En curso',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
}

const STATUS_CLASS: Record<string, string> = {
  DRAFT: 'route-details__status--draft',
  PUBLISHED: 'route-details__status--open',
  IN_PROGRESS: 'route-details__status--in-progress',
  COMPLETED: 'route-details__status--completed',
  CANCELLED: 'route-details__status--cancelled',
}

export default function RouteDetailsModal({
  route,
  isAdmin = false,
  isAuthenticated = false,
  isRegistered = false,
  isProcessing = false,
  onClose,
  onEnroll,
  onUnregister,
  onEdit,
  onDelete,
  onCloseRoute,
  onPublish,
}: RouteDetailsModalProps) {
  if (!route) return null

  const isFull = route.currentParticipants >= route.maxParticipants
  const availableSpots = Math.max(0, route.maxParticipants - route.currentParticipants)
  const searchUrl = getGoogleMapsSearchUrl(route.origin)

  const renderActions = () => {
    if (isAdmin) {
      return (
        <>
          {route.status === 'DRAFT' && (
            <button
              className="btn btn--primary"
              onClick={() => onPublish?.(route.id)}
              type="button"
            >
              Publicar
            </button>
          )}
          <button
            className="btn btn--secondary"
            onClick={() => onEdit?.(route)}
            type="button"
          >
            Editar
          </button>
          <button
            className="btn btn--danger"
            onClick={() => onDelete?.(route.id)}
            type="button"
          >
            Eliminar
          </button>
          {(route.status === 'PUBLISHED' || route.status === 'IN_PROGRESS') && (
            <button
              className="btn btn--ghost"
              onClick={() => onCloseRoute?.(route.id)}
              type="button"
            >
              Cerrar ruta
            </button>
          )}
        </>
      )
    }

    if (route.status !== 'PUBLISHED') return null

    if (isAuthenticated && isRegistered) {
      return (
        <button
          className="btn btn--outline-danger"
          onClick={() => onUnregister?.(route)}
          disabled={isProcessing}
          type="button"
        >
          {isProcessing ? 'Procesando...' : 'Desapuntarme'}
        </button>
      )
    }

    return (
      <button
        className="btn btn--primary"
        onClick={() => onEnroll?.(route)}
        disabled={isProcessing || isFull}
        type="button"
      >
        {isProcessing ? 'Procesando...' : isFull ? 'Ruta llena' : 'Apuntarme'}
      </button>
    )
  }

  return (
    <Modal isOpen={route !== null} onClose={onClose} title={route.title}>
      <div className="route-details">
        <div className="route-details__header">
          <span className={`route-details__status ${STATUS_CLASS[route.status] ?? ''}`}>
            {STATUS_LABELS[route.status] ?? route.status}
          </span>
          <p className="route-details__desc">{route.description}</p>
        </div>

        <div className="route-details__grid">
          <div className="route-details__item">
            <span className="route-details__label">Fecha</span>
            <span className="route-details__value">{route.date}</span>
          </div>
          <div className="route-details__item">
            <span className="route-details__label">Hora de quedada</span>
            <span className="route-details__value">{route.meetingTime ?? '—'}</span>
          </div>
          <div className="route-details__item">
            <span className="route-details__label">Hora de salida</span>
            <span className="route-details__value">{route.departureTime ?? '—'}</span>
          </div>
          <div className="route-details__item">
            <span className="route-details__label">Origen</span>
            <span className="route-details__value">{route.origin}</span>
          </div>
          <div className="route-details__item">
            <span className="route-details__label">Destino</span>
            <span className="route-details__value">{route.destination}</span>
          </div>
          <div className="route-details__item">
            <span className="route-details__label">Distancia</span>
            <span className="route-details__value">{route.distanceKm} km</span>
          </div>
          <div className="route-details__item">
            <span className="route-details__label">Dificultad</span>
            <span className={`route-details__badge ${DIFFICULTY_CLASS[route.difficulty] ?? ''}`}>
              {DIFFICULTY_LABELS[route.difficulty] ?? route.difficulty}
            </span>
          </div>
          <div className="route-details__item">
            <span className="route-details__label">Participantes actuales</span>
            <span className="route-details__value">
              {route.currentParticipants} / {route.maxParticipants}
            </span>
          </div>
          <div className="route-details__item">
            <span className="route-details__label">Plazas máximas</span>
            <span className="route-details__value">{route.maxParticipants}</span>
          </div>
          <div className="route-details__item">
            <span className="route-details__label">Plazas disponibles</span>
            <span className="route-details__value">{availableSpots}</span>
          </div>
        </div>

        <div className="route-details__maps">
          <a
            className="route-details__maps-link"
            href={searchUrl}
            target="_blank"
            rel="noopener noreferrer"
          >
            Ver punto de salida
          </a>
        </div>

        {route.organizers.length > 0 && (
          <div className="route-details__organizers">
            <span className="route-details__organizers-title">Organizadores</span>
            <ul className="route-details__organizers-list">
              {route.organizers.map((organizer) => (
                <li key={organizer.id} className="route-details__organizer">
                  <span className="route-details__organizer-avatar">
                    {organizer.username.slice(0, 2).toUpperCase()}
                  </span>
                  <span className="route-details__organizer-name">
                    {organizer.username}
                  </span>
                </li>
              ))}
            </ul>
          </div>
        )}

        <div className="route-details__actions">{renderActions()}</div>
      </div>
    </Modal>
  )
}
