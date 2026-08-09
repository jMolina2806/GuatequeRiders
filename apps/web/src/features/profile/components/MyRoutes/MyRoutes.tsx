import { Link } from 'react-router-dom'
import type { Route } from '@/features/routes/types/route.types'
import './MyRoutes.css'

type MyRoutesProps = {
  routes: Route[]
}

const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Abierta',
  IN_PROGRESS: 'En curso',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
}

const STATUS_CLASS: Record<string, string> = {
  DRAFT: 'my-routes__status--draft',
  PUBLISHED: 'my-routes__status--open',
  IN_PROGRESS: 'my-routes__status--in-progress',
  COMPLETED: 'my-routes__status--completed',
  CANCELLED: 'my-routes__status--cancelled',
}

export default function MyRoutes({ routes }: MyRoutesProps) {
  return (
    <section className="my-routes">
      <div className="my-routes__header">
        <h2 className="my-routes__title">Mis rutas</h2>
        <Link to="/routes" className="my-routes__link">
          Ver todas las rutas →
        </Link>
      </div>

      {routes.length === 0 ? (
        <div className="my-routes__empty">
          <p>Aún no estás apuntado a ninguna ruta.</p>
          <Link to="/routes" className="my-routes__empty-link">
            Explorar rutas
          </Link>
        </div>
      ) : (
        <ul className="my-routes__list">
          {routes.map((route) => (
            <li key={route.id} className="my-routes__card">
              <div className="my-routes__card-main">
                <span className="my-routes__card-title">{route.title}</span>
                <span className="my-routes__card-meta">
                  📅 {route.date} · 📍 {route.origin} → {route.destination}
                </span>
              </div>
              <span className={`my-routes__status ${STATUS_CLASS[route.status] ?? ''}`}>
                {STATUS_LABELS[route.status] ?? route.status}
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
