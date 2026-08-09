import type {
  Route,
  CreateRouteRequest,
  RegistrationStatus,
  UpdateRouteRequest,
} from '@/features/routes/types/route.types'
import { getToken } from '@/features/auth/services/session'
import { translateApiMessage } from '@/utils/apiMessages'

const API_BASE = '/api/routes'

function authHeaders(additional: HeadersInit = {}): HeadersInit {
  const token = getToken()
  return token
    ? { ...additional, Authorization: `Bearer ${token}` }
    : additional
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    const rawMessage = body?.message ?? `Error ${response.status}`
    throw new Error(translateApiMessage(rawMessage))
  }
  if (response.status === 204) return undefined as T
  return response.json()
}

export async function getRoutes(): Promise<Route[]> {
  const response = await fetch(API_BASE, { headers: authHeaders() })
  return handleResponse<Route[]>(response)
}

export async function getRouteById(id: number): Promise<Route> {
  const response = await fetch(`${API_BASE}/${id}`, { headers: authHeaders() })
  return handleResponse<Route>(response)
}

export async function createRoute(data: CreateRouteRequest): Promise<Route> {
  const response = await fetch(API_BASE, {
    method: 'POST',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(data),
  })
  return handleResponse<Route>(response)
}

export async function updateRoute(id: number, data: UpdateRouteRequest): Promise<Route> {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: 'PUT',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(data),
  })
  return handleResponse<Route>(response)
}

export async function publishRoute(id: number): Promise<Route> {
  const response = await fetch(`${API_BASE}/${id}/publish`, {
    method: 'PATCH',
    headers: authHeaders(),
  })
  return handleResponse<Route>(response)
}

export async function deleteRoute(id: number): Promise<void> {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  return handleResponse<void>(response)
}

export async function registerToRoute(routeId: number): Promise<unknown> {
  const response = await fetch(`${API_BASE}/${routeId}/registrations`, {
    method: 'POST',
    headers: authHeaders(),
  })
  return handleResponse<unknown>(response)
}

export async function unregisterFromRoute(routeId: number): Promise<void> {
  const response = await fetch(`${API_BASE}/${routeId}/registrations/me`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  return handleResponse<void>(response)
}

export async function getMyRegistration(routeId: number): Promise<RegistrationStatus> {
  const response = await fetch(`${API_BASE}/${routeId}/registrations/me`, {
    headers: authHeaders(),
  })
  return handleResponse<RegistrationStatus>(response)
}
