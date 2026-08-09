import type { User } from '@/features/auth/types/auth.types'
import { getToken } from '@/features/auth/services/session'
import type { AdminStats, UpdateRoleRequest } from '../types/admin.types'

const API_BASE = '/api/admin'

function authHeaders(additional: HeadersInit = {}): HeadersInit {
  const token = getToken()
  return token ? { ...additional, Authorization: `Bearer ${token}` } : additional
}

const ERROR_MESSAGES: Record<string, string> = {
  'Cannot remove the last admin': 'No puedes quitar el último administrador',
}

function translateError(message: string): string {
  return ERROR_MESSAGES[message] ?? message
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

export async function getAdminStats(): Promise<AdminStats> {
  const response = await fetch(`${API_BASE}/stats`, { headers: authHeaders() })
  return handleResponse<AdminStats>(response)
}

export async function getUsers(): Promise<User[]> {
  const response = await fetch(`${API_BASE}/users`, { headers: authHeaders() })
  return handleResponse<User[]>(response)
}

export async function updateUserRole(userId: number, role: 'USER' | 'ADMIN'): Promise<User> {
  const request: UpdateRoleRequest = { role }
  const response = await fetch(`${API_BASE}/users/${userId}/role`, {
    method: 'PATCH',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(request),
  })
  return handleResponse<User>(response)
}
