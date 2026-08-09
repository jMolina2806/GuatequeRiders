import type { Event } from '@/features/events/types/event.types'
import type { Route } from '@/features/routes/types/route.types'
import { getToken } from '@/features/auth/services/session'

const API_BASE = '/api/users/me'

function authHeaders(): HeadersInit {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    const rawMessage = body?.message ?? `Error ${response.status}`
    throw new Error(rawMessage)
  }
  return response.json()
}

export async function getMyRoutes(): Promise<Route[]> {
  const response = await fetch(`${API_BASE}/routes`, {
    headers: authHeaders(),
  })
  return handleResponse<Route[]>(response)
}

export async function getMyEvents(): Promise<Event[]> {
  const response = await fetch(`${API_BASE}/events`, {
    headers: authHeaders(),
  })
  return handleResponse<Event[]>(response)
}
