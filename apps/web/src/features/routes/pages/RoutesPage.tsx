import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import RouteList from '../components/RouteList/RouteList'
import RouteForm from '../components/RouteForm'
import Modal from '@/components/Modal'
import { useRoutes } from '../hooks/useRoutes'
import { useAuth } from '@/features/auth/context/useAuth'
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
    registerToRoute,
    unregisterFromRoute,
    clearFeedback,
    cancelEdit,
  } = useRoutes()

  const { isAdmin, isAuthenticated } = useAuth()
  const navigate = useNavigate()

  const [isModalOpen, setIsModalOpen] = useState(false)

  const handleCreateRoute = () => {
    setIsModalOpen(true)
  }

  const handleEditRoute = (route: Parameters<typeof editRoute>[0]) => {
    editRoute(route)
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
          onEnroll={handleEnroll}
          onUnregister={handleUnregister}
        />
      )}

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
        />
      </Modal>
    </section>
  )
}
