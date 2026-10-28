import type {
  Collaborator,
  CreateCollaboratorRequest,
} from '../types/collaborator.types'
import { getToken } from '@/features/auth/services/session'
import { translateApiMessage } from '@/utils/apiMessages'

const API_BASE = '/api/collaborators'

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

export async function getCollaborators(publicOnly = false): Promise<Collaborator[]> {
  const response = await fetch(API_BASE, {
    headers: publicOnly ? undefined : authHeaders(),
  })
  return handleResponse<Collaborator[]>(response)
}

export async function getCollaborator(id: number): Promise<Collaborator> {
  const response = await fetch(`${API_BASE}/${id}`, { headers: authHeaders() })
  return handleResponse<Collaborator>(response)
}

export async function createCollaborator(data: CreateCollaboratorRequest): Promise<Collaborator> {
  const response = await fetch(API_BASE, {
    method: 'POST',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(data),
  })
  return handleResponse<Collaborator>(response)
}

export async function updateCollaborator(
  id: number,
  data: CreateCollaboratorRequest,
): Promise<Collaborator> {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: 'PUT',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(data),
  })
  return handleResponse<Collaborator>(response)
}

export async function updateCollaboratorActive(id: number, active: boolean): Promise<Collaborator> {
  const response = await fetch(`${API_BASE}/${id}/active`, {
    method: 'PATCH',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify({ active }),
  })
  return handleResponse<Collaborator>(response)
}

export async function uploadCollaboratorImage(id: number, file: File): Promise<Collaborator> {
  const formData = new FormData()
  formData.append('file', file)
  const response = await fetch(`${API_BASE}/${id}/image`, {
    method: 'POST',
    headers: authHeaders(),
    body: formData,
  })
  return handleResponse<Collaborator>(response)
}

export async function deleteCollaboratorImage(id: number): Promise<Collaborator> {
  const response = await fetch(`${API_BASE}/${id}/image`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  return handleResponse<Collaborator>(response)
}

export async function deleteCollaborator(id: number): Promise<void> {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  return handleResponse<void>(response)
}
