import { useState } from 'react'
import RouteList from '../components/RouteList/RouteList'
import RouteForm from '../components/RouteForm'
import Modal from '@/components/Modal'
import { useRoutes } from '../hooks/useRoutes'
import { currentUser } from '../data/mockUser'
import './RoutesPage.css'

export default function RoutesPage() {
  const {
    routes,
    editingInitialValues,
    submitLabel,
    submitRoute,
    editRoute,
    deleteRoute,
    closeRoute,
    cancelEdit,
  } = useRoutes()

  const [isModalOpen, setIsModalOpen] = useState(false)
  const isAdmin = currentUser.role === 'admin'

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

      <RouteList
        routes={routes}
        isAdmin={isAdmin}
        onEdit={handleEditRoute}
        onDelete={deleteRoute}
        onClose={closeRoute}
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
        />
      </Modal>
    </section>
  )
}
