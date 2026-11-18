import { Link, Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/context/useAuth'
import type { ReactNode } from 'react'
import './RequireMember.css'

export default function RequireMember({ children }: { children: ReactNode }) {
  const { isAuthenticated, isMember, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return (
      <section className="require-member__state" role="status" aria-live="polite">
        <div className="require-member__spinner" />
        <p>Cargando...</p>
      </section>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  }

  if (!isMember) {
    return (
      <section className="require-member__state">
        <h1>Carnet de socio</h1>
        <p>Esta sección solo está disponible para socios activos.</p>
        <Link to="/profile" className="btn btn--ghost">
          Volver al perfil
        </Link>
      </section>
    )
  }

  return children
}
