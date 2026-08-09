import StatsGrid from '../components/StatsGrid'
import QuickActions from '../components/QuickActions'
import { useAdminStats } from '../hooks/useAdminStats'
import './AdminDashboardPage.css'

export default function AdminDashboardPage() {
  const { stats, isLoading, error, retry } = useAdminStats()

  return (
    <section className="admin-dashboard">
      <header className="admin-dashboard__header">
        <h1>Panel de administración</h1>
        <p>Estado general de la plataforma Guateque Riders.</p>
      </header>

      {error && (
        <div className="admin-dashboard__error" role="alert">
          <span>{error}</span>
          <button className="btn btn--outline-danger btn--sm" onClick={retry} type="button">
            Reintentar
          </button>
        </div>
      )}

      {isLoading ? (
        <div className="admin-dashboard__loading" role="status" aria-live="polite">
          <div className="admin-dashboard__spinner" />
          <p>Cargando estadísticas...</p>
        </div>
      ) : stats ? (
        <>
          <StatsGrid stats={stats} />
          <h2 className="admin-dashboard__section-title">Gestión rápida</h2>
          <QuickActions />
        </>
      ) : null}
    </section>
  )
}
