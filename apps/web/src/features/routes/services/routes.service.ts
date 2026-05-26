import type { Route, CreateRouteRequest, UpdateRouteRequest } from '@/features/routes/types/route.types'

const API_BASE = '/api/routes'

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    const message = body?.message ?? `Error ${response.status}`
    throw new Error(message)
  }
  if (response.status === 204) return undefined as T
  return response.json()
}

export async function getRoutes(): Promise<Route[]> {
  const response = await fetch(API_BASE)
  return handleResponse<Route[]>(response)
}

export async function getRouteById(id: number): Promise<Route> {
  const response = await fetch(`${API_BASE}/${id}`)
  return handleResponse<Route>(response)
}

export async function createRoute(data: CreateRouteRequest): Promise<Route> {
  const response = await fetch(API_BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  return handleResponse<Route>(response)
}

export async function updateRoute(id: number, data: UpdateRouteRequest): Promise<Route> {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  return handleResponse<Route>(response)
}

export async function deleteRoute(id: number): Promise<void> {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: 'DELETE',
  })
  return handleResponse<void>(response)
}
