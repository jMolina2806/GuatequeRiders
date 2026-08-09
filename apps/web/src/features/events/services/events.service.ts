import type {
  CreateEventRequest,
  Event,
  EventRegistrationStatus,
  UpdateEventRequest,
} from '@/features/events/types/event.types'
import { getToken } from '@/features/auth/services/session'

const API_BASE = '/api/events'

function authHeaders(additional: HeadersInit = {}): HeadersInit {
  const token = getToken()
  return token
    ? { ...additional, Authorization: `Bearer ${token}` }
    : additional
}

const ERROR_MESSAGES: Record<string, string> = {
  'Event is full': 'El evento está completo',
  'Event is not open for registration': 'El evento está cerrado',
  'Already registered to this event': 'Ya estás apuntado a este evento',
  'Registration not found for event': 'No estás apuntado a este evento',
}

function translateError(message: string): string {
  if (ERROR_MESSAGES[message]) return ERROR_MESSAGES[message]
  if (message.startsWith('Registration not found for event')) {
    return ERROR_MESSAGES['Registration not found for event']
  }
  return message
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    const rawMessage = body?.message ?? `Error ${response.status}`
    throw new Error(translateError(rawMessage))
  }
  if (response.status === 204) return undefined as T
  return response.json()
}

export async function getEvents(): Promise<Event[]> {
  const response = await fetch(API_BASE)
  return handleResponse<Event[]>(response)
}

export async function getEventById(id: number): Promise<Event> {
  const response = await fetch(`${API_BASE}/${id}`)
  return handleResponse<Event>(response)
}

export async function createEvent(data: CreateEventRequest): Promise<Event> {
  const response = await fetch(API_BASE, {
    method: 'POST',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(data),
  })
  return handleResponse<Event>(response)
}

export async function updateEvent(id: number, data: UpdateEventRequest): Promise<Event> {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: 'PUT',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(data),
  })
  return handleResponse<Event>(response)
}

export async function deleteEvent(id: number): Promise<void> {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  return handleResponse<void>(response)
}

export async function registerToEvent(eventId: number): Promise<unknown> {
  const response = await fetch(`${API_BASE}/${eventId}/registrations`, {
    method: 'POST',
    headers: authHeaders(),
  })
  return handleResponse<unknown>(response)
}

export async function unregisterFromEvent(eventId: number): Promise<void> {
  const response = await fetch(`${API_BASE}/${eventId}/registrations/me`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  return handleResponse<void>(response)
}

export async function getMyRegistration(eventId: number): Promise<EventRegistrationStatus> {
  const response = await fetch(`${API_BASE}/${eventId}/registrations/me`, {
    headers: authHeaders(),
  })
  return handleResponse<EventRegistrationStatus>(response)
}
