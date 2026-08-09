import { Link } from 'react-router-dom'
import type { Event } from '@/features/events/types/event.types'
import './MyEvents.css'

type MyEventsProps = {
  events: Event[]
}

const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Publicado',
  CLOSED: 'Cerrado',
  CANCELLED: 'Cancelado',
}

const STATUS_CLASS: Record<string, string> = {
  DRAFT: 'my-events__status--draft',
  PUBLISHED: 'my-events__status--published',
  CLOSED: 'my-events__status--closed',
  CANCELLED: 'my-events__status--cancelled',
}

export default function MyEvents({ events }: MyEventsProps) {
  return (
    <section className="my-events">
      <div className="my-events__header">
        <h2 className="my-events__title">Mis eventos</h2>
        <Link to="/events" className="my-events__link">
          Ver todos los eventos →
        </Link>
      </div>

      {events.length === 0 ? (
        <div className="my-events__empty">
          <p>Aún no estás apuntado a ningún evento.</p>
          <Link to="/events" className="my-events__empty-link">
            Explorar eventos
          </Link>
        </div>
      ) : (
        <ul className="my-events__list">
          {events.map((event) => {
            const endTime = event.endTime
            const timeRange = endTime ? `${event.startTime} – ${endTime}` : event.startTime
            return (
              <li key={event.id} className="my-events__card">
                <div className="my-events__card-main">
                  <span className="my-events__card-title">{event.title}</span>
                  <span className="my-events__card-meta">
                    📅 {event.date} · 🕖 {timeRange} · 📍 {event.location}
                  </span>
                </div>
                <span className={`my-events__status ${STATUS_CLASS[event.status] ?? ''}`}>
                  {STATUS_LABELS[event.status] ?? event.status}
                </span>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}
