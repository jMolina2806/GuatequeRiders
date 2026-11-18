import type {
  Circuit,
  CreateCircuitRequest,
  UpdateCircuitRequest,
} from '@/features/circuits/types/circuit.types'
import { getToken } from '@/features/auth/services/session'
import { translateApiMessage } from '@/utils/apiMessages'

const API_BASE = '/api/circuits'

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

export async function getCircuits(): Promise<Circuit[]> {
  const response = await fetch(API_BASE, { headers: authHeaders() })
  return handleResponse<Circuit[]>(response)
}

export async function getPublicCircuits(): Promise<Circuit[]> {
  const response = await fetch(API_BASE)
  return handleResponse<Circuit[]>(response)
}

export async function getCircuitById(id: number): Promise<Circuit> {
  const response = await fetch(`${API_BASE}/${id}`, { headers: authHeaders() })
  return handleResponse<Circuit>(response)
}

export async function createCircuit(data: CreateCircuitRequest): Promise<Circuit> {
  const response = await fetch(API_BASE, {
    method: 'POST',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(data),
  })
  return handleResponse<Circuit>(response)
}

export async function updateCircuit(id: number, data: UpdateCircuitRequest): Promise<Circuit> {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: 'PUT',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(data),
  })
  return handleResponse<Circuit>(response)
}

export async function publishCircuit(id: number): Promise<Circuit> {
  const response = await fetch(`${API_BASE}/${id}/publish`, {
    method: 'PATCH',
    headers: authHeaders(),
  })
  return handleResponse<Circuit>(response)
}

export async function completeCircuit(id: number): Promise<Circuit> {
  const response = await fetch(`${API_BASE}/${id}/complete`, {
    method: 'PATCH',
    headers: authHeaders(),
  })
  return handleResponse<Circuit>(response)
}

export async function cancelCircuit(id: number): Promise<Circuit> {
  const response = await fetch(`${API_BASE}/${id}/cancel`, {
    method: 'PATCH',
    headers: authHeaders(),
  })
  return handleResponse<Circuit>(response)
}

export async function deleteCircuit(id: number): Promise<void> {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  return handleResponse<void>(response)
}
