import type { ReactNode } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/context/useAuth'
import './RequireMember.css'

export default function RequireVerifier({ children }: { children: ReactNode }) {
  const { user, isAuthenticated, isLoading, isAdmin } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <section className="require-member__state" role="status"><p>Comprobando permisos...</p></section>
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  }

  if (!isAdmin && user?.role !== 'COLLABORATOR') {
    return (
      <section className="require-member__state">
        <h1>Acceso para colaboradores</h1>
        <p>La verificación de carnets está reservada a establecimientos autorizados por GuatequeRiders.</p>
        <Link to="/" className="btn btn--ghost">Volver al inicio</Link>
      </section>
    )
  }

  return children
}
