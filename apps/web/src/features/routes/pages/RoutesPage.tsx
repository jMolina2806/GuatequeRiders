import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import RouteList from '../components/RouteList/RouteList'
import RouteForm from '../components/RouteForm'
import RouteDetailsModal from '../components/RouteDetailsModal'
import Modal from '@/components/Modal'
import { useRoutes } from '../hooks/useRoutes'
import { useAuth } from '@/features/auth/context/useAuth'
import { getUsers } from '@/features/admin/services/admin.service'
import type { User } from '@/features/auth/types/auth.types'
import type { Route } from '../types/route.types'
import './RoutesPage.css'

export default function RoutesPage() {
  const {
    routes,
    editingInitialValues,
    submitLabel,
    isLoading,
    error,
    registrations,
    registrationLoading,
    feedback,
    submitRoute,
    editRoute,
    deleteRoute,
    closeRoute,
    publishRoute,
    registerToRoute,
    unregisterFromRoute,
    clearFeedback,
    cancelEdit,
  } = useRoutes()

  const { isAdmin, isAuthenticated } = useAuth()
  const navigate = useNavigate()

  const [isModalOpen, setIsModalOpen] = useState(false)
  const [detailsRoute, setDetailsRoute] = useState<Route | null>(null)
  const [availableUsers, setAvailableUsers] = useState<User[]>([])

  const adminUsers = availableUsers.filter((user) => user.role === 'ADMIN')

  useEffect(() => {
    if (!isAdmin) return
    let cancelled = false
    getUsers()
      .then((users) => {
        if (!cancelled) setAvailableUsers(users)
      })
      .catch(() => {
        if (!cancelled) setAvailableUsers([])
      })
    return () => {
      cancelled = true
    }
  }, [isAdmin])

  const handleCreateRoute = () => {
    setIsModalOpen(true)
  }

  const handleEditRoute = (route: Parameters<typeof editRoute>[0]) => {
    editRoute(route)
    setDetailsRoute(null)
    setIsModalOpen(true)
  }

  const handleCloseModal = () => {
    cancelEdit()
    setIsModalOpen(false)
  }

  const handleEnroll = (route: Route) => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/routes' } })
      return
    }
    registerToRoute(route.id)
  }

  const handleUnregister = (route: Route) => {
    unregisterFromRoute(route.id)
  }

  return (
    <section>
      <div className="routes-page__header">
        <div className="routes-page__header-text">
          <h1>Rutas</h1>
          <p>Descubre las próximas rutas del club y apúntate.</p>
        </div>

        {isAdmin && (
          <button
            className="routes-page__create-btn"
            onClick={handleCreateRoute}
            type="button"
          >
            Crear ruta
          </button>
        )}
      </div>

      {feedback && (
        <div className={`routes-page__feedback routes-page__feedback--${feedback.type}`}>
          <span>{feedback.message}</span>
          <button
            className="routes-page__feedback-close"
            onClick={clearFeedback}
            type="button"
            aria-label="Cerrar"
          >
            ✕
          </button>
        </div>
      )}

      {error && (
        <div className="routes-page__error">
          <span>{error}</span>
          <button
            className="routes-page__retry-btn"
            onClick={() => window.location.reload()}
            type="button"
          >
            Reintentar
          </button>
        </div>
      )}

      {isLoading ? (
        <div className="routes-page__loading">
          <div className="routes-page__spinner" />
          <p>Cargando rutas...</p>
        </div>
      ) : routes.length === 0 && !error ? (
        <div className="routes-page__empty">
          <p>No hay rutas disponibles.</p>
        </div>
      ) : (
        <RouteList
          routes={routes}
          isAdmin={isAdmin}
          isAuthenticated={isAuthenticated}
          registrations={registrations}
          registrationLoading={registrationLoading}
          onEdit={handleEditRoute}
          onDelete={deleteRoute}
          onClose={closeRoute}
          onPublish={publishRoute}
          onEnroll={handleEnroll}
          onUnregister={handleUnregister}
          onDetails={setDetailsRoute}
        />
      )}

      <RouteDetailsModal
        route={detailsRoute}
        isAdmin={isAdmin}
        isAuthenticated={isAuthenticated}
        isRegistered={detailsRoute ? !!registrations[detailsRoute.id] : false}
        isProcessing={detailsRoute ? !!registrationLoading[detailsRoute.id] : false}
        onClose={() => setDetailsRoute(null)}
        onEnroll={handleEnroll}
        onUnregister={handleUnregister}
        onEdit={handleEditRoute}
        onDelete={deleteRoute}
        onCloseRoute={closeRoute}
        onPublish={publishRoute}
      />

      <Modal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        title={submitLabel}
      >
        <RouteForm
          onSubmit={submitRoute}
          onCancel={handleCloseModal}
          initialValues={editingInitialValues}
          submitLabel={submitLabel}
          availableUsers={adminUsers}
        />
      </Modal>
    </section>
  )
}
