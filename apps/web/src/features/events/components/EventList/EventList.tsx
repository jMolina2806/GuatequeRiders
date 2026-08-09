import EventCard from '../EventCard'
import type { Event } from '@/features/events/types/event.types'

type EventListProps = {
  events: Event[]
  isAdmin?: boolean
  isAuthenticated?: boolean
  registrations?: Record<number, boolean>
  registrationLoading?: Record<number, boolean>
  onEdit?: (event: Event) => void
  onDelete?: (eventId: number) => void
  onPublish?: (event: Event) => void
  onClose?: (event: Event) => void
  onCancel?: (event: Event) => void
  onEnroll?: (event: Event) => void
  onUnregister?: (event: Event) => void
}

export default function EventList({
  events,
  isAdmin = false,
  isAuthenticated = false,
  registrations = {},
  registrationLoading = {},
  onEdit,
  onDelete,
  onPublish,
  onClose,
  onCancel,
  onEnroll,
  onUnregister,
}: EventListProps) {
  return (
    <section>
      {events.map((event) => (
        <EventCard
          key={event.id}
          event={event}
          isAdmin={isAdmin}
          isAuthenticated={isAuthenticated}
          isRegistered={registrations[event.id]}
          isProcessing={registrationLoading[event.id]}
          onEdit={onEdit}
          onDelete={onDelete}
          onPublish={onPublish}
          onClose={onClose}
          onCancel={onCancel}
          onEnroll={onEnroll}
          onUnregister={onUnregister}
        />
      ))}
    </section>
  )
}
