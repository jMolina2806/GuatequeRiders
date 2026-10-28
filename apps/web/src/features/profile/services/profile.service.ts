import type { Event } from '@/features/events/types/event.types'
import type { Route } from '@/features/routes/types/route.types'
import type {
  NotificationCategory,
  NotificationPreferences,
} from '../types/profile.types'
import { getToken } from '@/features/auth/services/session'
import { translateApiMessage } from '@/utils/apiMessages'

const API_BASE = '/api/users/me'
const NOTIFICATION_API_BASE = '/api/notifications'

function authHeaders(): HeadersInit {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    const rawMessage = body?.message ?? `Error ${response.status}`
    throw new Error(translateApiMessage(rawMessage))
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

export async function getNotificationPreferences(): Promise<NotificationPreferences> {
  const response = await fetch(`${NOTIFICATION_API_BASE}/preferences`, {
    headers: authHeaders(),
  })
  return handleResponse<NotificationPreferences>(response)
}

export async function updateNotificationPreferences(
  preferences: NotificationPreferences,
): Promise<NotificationPreferences> {
  const response = await fetch(`${NOTIFICATION_API_BASE}/preferences`, {
    method: 'PUT',
    headers: {
      ...authHeaders(),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(preferences),
  })
  return handleResponse<NotificationPreferences>(response)
}

export async function unsubscribeFromCategory(
  token: string,
  category: NotificationCategory,
): Promise<void> {
  const response = await fetch(`${NOTIFICATION_API_BASE}/unsubscribe`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ token, category }),
  })

  await handleResponse<{ unsubscribed: boolean }>(response)
}
