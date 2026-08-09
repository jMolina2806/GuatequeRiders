import { Link, Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/context/useAuth'
import type { ReactNode } from 'react'
import './RequireAdmin.css'

export default function RequireAdmin({ children }: { children: ReactNode }) {
  const { isAuthenticated, isAdmin, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return (
      <section className="require-admin__loading">
        <div className="require-admin__spinner" />
        <p>Cargando...</p>
      </section>
    )
  }

  if (!isAuthenticated) {
    return (
      <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
    )
  }

  if (!isAdmin) {
    return (
      <section className="require-admin__denied">
        <h1>Acceso denegado</h1>
        <p>Necesitas permisos de administrador para acceder a esta sección.</p>
        <Link to="/" className="require-admin__link">Volver al inicio</Link>
      </section>
    )
  }

  return children
}
