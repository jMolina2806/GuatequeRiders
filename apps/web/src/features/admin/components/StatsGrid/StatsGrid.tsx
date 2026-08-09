import type { AdminStats } from '../../types/admin.types'
import './StatsGrid.css'

const METRICS: Array<{ key: keyof AdminStats; label: string }> = [
  { key: 'totalUsers', label: 'Usuarios' },
  { key: 'totalRoutes', label: 'Rutas' },
  { key: 'publishedRoutes', label: 'Rutas publicadas' },
  { key: 'totalRouteRegistrations', label: 'Inscripciones a rutas' },
  { key: 'totalEvents', label: 'Eventos' },
  { key: 'publishedEvents', label: 'Eventos publicados' },
  { key: 'totalEventRegistrations', label: 'Asistencias a eventos' },
]

export default function StatsGrid({ stats }: { stats: AdminStats }) {
  return (
    <div className="stats-grid">
      {METRICS.map((metric) => (
        <article className="stats-grid__card" key={metric.key}>
          <span className="stats-grid__label">{metric.label}</span>
          <span className="stats-grid__value">{stats[metric.key]}</span>
        </article>
      ))}
    </div>
  )
}
