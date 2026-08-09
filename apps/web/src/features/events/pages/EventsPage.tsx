import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import EventList from '../components/EventList'
import EventForm from '../components/EventForm'
import EventDetailsModal from '../components/EventDetailsModal'
import Modal from '@/components/Modal'
import { useEvents } from '../hooks/useEvents'
import { useAuth } from '@/features/auth/context/useAuth'
import type { Event } from '../types/event.types'
import './EventsPage.css'

export default function EventsPage() {
  const {
    events,
    editingInitialValues,
    submitLabel,
    isLoading,
    error,
    registrations,
    registrationLoading,
    feedback,
    retry,
    submitEvent,
    editEvent,
    deleteEvent,
    changeStatus,
    registerToEvent,
    unregisterFromEvent,
    clearFeedback,
    cancelEdit,
  } = useEvents()

  const { isAdmin, isAuthenticated } = useAuth()
  const navigate = useNavigate()

  const [isModalOpen, setIsModalOpen] = useState(false)
  const [detailsEvent, setDetailsEvent] = useState<Event | null>(null)

  const handleCreateEvent = () => {
    setIsModalOpen(true)
  }

  const handleEditEvent = (event: Parameters<typeof editEvent>[0]) => {
    editEvent(event)
    setDetailsEvent(null)
    setIsModalOpen(true)
  }

  const handleCloseModal = () => {
    cancelEdit()
    setIsModalOpen(false)
  }

  const handleEnroll = (event: Event) => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/events' } })
      return
    }
    registerToEvent(event.id)
  }

  const handleUnregister = (event: Event) => {
    unregisterFromEvent(event.id)
  }

  return (
    <section>
      <div className="events-page__header">
        <div className="events-page__header-text">
          <h1>Eventos</h1>
          <p>Descubre los próximos eventos y quedadas del club.</p>
        </div>

        {isAdmin && (
          <button
            className="btn btn--primary"
            onClick={handleCreateEvent}
            type="button"
          >
            Crear evento
          </button>
        )}
      </div>

      {feedback && (
        <div
          className={`events-page__feedback events-page__feedback--${feedback.type}`}
          role="status"
        >
          <span>{feedback.message}</span>
          <button
            className="events-page__feedback-close"
            onClick={clearFeedback}
            type="button"
            aria-label="Cerrar"
          >
            ✕
          </button>
        </div>
      )}

      {error && (
        <div className="events-page__error" role="alert">
          <span>{error}</span>
          <button className="btn btn--outline-danger btn--sm" onClick={retry} type="button">
            Reintentar
          </button>
        </div>
      )}

      {isLoading ? (
        <div className="events-page__loading" role="status" aria-live="polite">
          <div className="events-page__spinner" />
          <p>Cargando eventos...</p>
        </div>
      ) : events.length === 0 && !error ? (
        <div className="events-page__empty">
          <p>No hay eventos disponibles.</p>
        </div>
      ) : (
        <EventList
          events={events}
          isAdmin={isAdmin}
          isAuthenticated={isAuthenticated}
          registrations={registrations}
          registrationLoading={registrationLoading}
          onEdit={handleEditEvent}
          onDelete={deleteEvent}
          onPublish={(event) => changeStatus(event, 'PUBLISHED')}
          onClose={(event) => changeStatus(event, 'CLOSED')}
          onCancel={(event) => changeStatus(event, 'CANCELLED')}
          onEnroll={handleEnroll}
          onUnregister={handleUnregister}
          onDetails={setDetailsEvent}
        />
      )}

      <EventDetailsModal
        event={detailsEvent}
        isAdmin={isAdmin}
        isAuthenticated={isAuthenticated}
        isRegistered={detailsEvent ? !!registrations[detailsEvent.id] : false}
        isProcessing={detailsEvent ? !!registrationLoading[detailsEvent.id] : false}
        onClose={() => setDetailsEvent(null)}
        onEnroll={handleEnroll}
        onUnregister={handleUnregister}
        onEdit={handleEditEvent}
        onDelete={deleteEvent}
        onPublish={(event) => changeStatus(event, 'PUBLISHED')}
        onCloseEvent={(event) => changeStatus(event, 'CLOSED')}
        onCancel={(event) => changeStatus(event, 'CANCELLED')}
      />

      <Modal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        title={submitLabel}
      >
        <EventForm
          onSubmit={submitEvent}
          onCancel={handleCloseModal}
          initialValues={editingInitialValues}
          submitLabel={submitLabel}
        />
      </Modal>
    </section>
  )
}
