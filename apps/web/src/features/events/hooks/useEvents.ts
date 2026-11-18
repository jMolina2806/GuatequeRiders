import { useCallback, useEffect, useMemo, useState } from 'react'
import * as eventsService from '../services/events.service'
import { useAuth } from '@/features/auth/context/useAuth'
import type {
  CreateEventRequest,
  Event,
  EventStatus,
} from '@/features/events/types/event.types'

export type EventFeedback = {
  message: string
  type: 'success' | 'error'
}

export function useEvents() {
  const { isAuthenticated } = useAuth()

  const [events, setEvents] = useState<Event[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [editingEventId, setEditingEventId] = useState<number | null>(null)
  const [registrations, setRegistrations] = useState<Record<number, boolean>>({})
  const [registrationLoading, setRegistrationLoading] = useState<Record<number, boolean>>({})
  const [feedback, setFeedback] = useState<EventFeedback | null>(null)

  const refreshRegistrations = useCallback(async (eventList: Event[]) => {
    if (!isAuthenticated) {
      setRegistrations({})
      return
    }
    const results = await Promise.all(
      eventList.map(async (event) => {
        const status = await eventsService
          .getMyRegistration(event.id)
          .catch(() => ({ registered: false }))
        return { id: event.id, registered: status.registered }
      }),
    )
    const next: Record<number, boolean> = {}
    results.forEach((result) => {
      next[result.id] = result.registered
    })
    setRegistrations(next)
  }, [isAuthenticated])

  const fetchEvents = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await eventsService.getEvents()
      setEvents(data)
      await refreshRegistrations(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar eventos')
    } finally {
      setIsLoading(false)
    }
  }, [refreshRegistrations])

  useEffect(() => {
    fetchEvents()
  }, [fetchEvents])

  const editingEvent = useMemo(
    () => events.find((event) => event.id === editingEventId) ?? null,
    [events, editingEventId],
  )

  const editingInitialValues = useMemo(() => {
    if (!editingEvent) return null
    return {
      title: editingEvent.title,
      description: editingEvent.description,
      location: editingEvent.location,
      date: editingEvent.date,
      startTime: editingEvent.startTime,
      endTime: editingEvent.endTime,
      maxParticipants: editingEvent.maxParticipants,
    } satisfies CreateEventRequest
  }, [editingEvent])

  const submitLabel = editingEvent ? 'Guardar cambios' : 'Crear evento'

  const updateAttendees = useCallback((eventId: number, delta: number) => {
    setEvents((prev) =>
      prev.map((event) =>
        event.id === eventId
          ? { ...event, currentParticipants: Math.max(0, event.currentParticipants + delta) }
          : event,
      ),
    )
  }, [])

  const submitEvent = async (data: CreateEventRequest) => {
    try {
      if (editingEventId !== null) {
        const current = events.find((e) => e.id === editingEventId)
        await eventsService.updateEvent(editingEventId, {
          ...data,
          status: current?.status ?? 'DRAFT',
        })
        setEditingEventId(null)
      } else {
        await eventsService.createEvent(data)
      }
      await fetchEvents()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al guardar evento')
    }
  }

  const editEvent = (event: Event) => {
    setEditingEventId(event.id)
  }

  const deleteEvent = async (eventId: number) => {
    try {
      await eventsService.deleteEvent(eventId)
      if (editingEventId === eventId) {
        setEditingEventId(null)
      }
      await fetchEvents()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al eliminar evento')
    }
  }

  const changeStatus = async (event: Event, status: EventStatus) => {
    try {
      await eventsService.updateEvent(event.id, {
        title: event.title,
        description: event.description,
        location: event.location,
        date: event.date,
        startTime: event.startTime,
        endTime: event.endTime,
        maxParticipants: event.maxParticipants,
        status,
      })
      await fetchEvents()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cambiar el estado del evento')
    }
  }

  const registerToEvent = async (eventId: number) => {
    setRegistrationLoading((prev) => ({ ...prev, [eventId]: true }))
    setFeedback(null)
    try {
      await eventsService.registerToEvent(eventId)
      updateAttendees(eventId, 1)
      setRegistrations((prev) => ({ ...prev, [eventId]: true }))
      setFeedback({ message: 'Asistencia confirmada', type: 'success' })
    } catch (err) {
      setFeedback({
        message: err instanceof Error ? err.message : 'Error al confirmar asistencia',
        type: 'error',
      })
    } finally {
      setRegistrationLoading((prev) => ({ ...prev, [eventId]: false }))
    }
  }

  const unregisterFromEvent = async (eventId: number) => {
    setRegistrationLoading((prev) => ({ ...prev, [eventId]: true }))
    setFeedback(null)
    try {
      await eventsService.unregisterFromEvent(eventId)
      updateAttendees(eventId, -1)
      setRegistrations((prev) => ({ ...prev, [eventId]: false }))
      setFeedback({ message: 'Asistencia cancelada', type: 'success' })
    } catch (err) {
      setFeedback({
        message: err instanceof Error ? err.message : 'Error al cancelar asistencia',
        type: 'error',
      })
    } finally {
      setRegistrationLoading((prev) => ({ ...prev, [eventId]: false }))
    }
  }

  const clearFeedback = () => {
    setFeedback(null)
  }

  const cancelEdit = () => {
    setEditingEventId(null)
  }

  return {
    events,
    editingInitialValues,
    submitLabel,
    isLoading,
    error,
    registrations,
    registrationLoading,
    feedback,
    retry: fetchEvents,
    submitEvent,
    editEvent,
    deleteEvent,
    changeStatus,
    registerToEvent,
    unregisterFromEvent,
    clearFeedback,
    cancelEdit,
  }
}
