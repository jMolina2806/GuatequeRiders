import { getToken } from '@/features/auth/services/session'
import { translateApiMessage } from '@/utils/apiMessages'
import type { CartLine } from '../types/cart.types'
import type { Order } from '../types/merch.types'

const API_BASE = '/api/orders'

export type CreateOrderRequest = {
  items: CartLine[]
  customerName: string
  customerEmail: string
  customerPhone: string
  notes?: string
  fulfillmentMethod?: 'PICKUP' | 'SHIPPING'
  address?: string
  city?: string
  postalCode?: string
  province?: string
  merchTermsAccepted: boolean
}

export type CheckoutSessionResponse = {
  checkoutUrl: string
  sessionId: string
}

function authHeaders(additional: HeadersInit = {}): HeadersInit {
  const token = getToken()
  return token ? { ...additional, Authorization: `Bearer ${token}` } : additional
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    const rawMessage = body?.message ?? `Error ${response.status}`
    throw new Error(translateApiMessage(rawMessage))
  }
  return response.json() as Promise<T>
}

export async function createOrder(data: CreateOrderRequest): Promise<Order> {
  const response = await fetch(API_BASE, {
    method: 'POST',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(data),
  })
  return handleResponse<Order>(response)
}

export async function getOrder(id: number): Promise<Order> {
  const response = await fetch(`${API_BASE}/${id}`, { headers: authHeaders() })
  return handleResponse<Order>(response)
}

export async function cancelOrder(id: number): Promise<Order> {
  const response = await fetch(`${API_BASE}/${id}/cancel`, {
    method: 'POST',
    headers: authHeaders(),
  })
  return handleResponse<Order>(response)
}

export async function createCheckoutSession(id: number): Promise<CheckoutSessionResponse> {
  const response = await fetch(`${API_BASE}/${id}/checkout-session`, {
    method: 'POST',
    headers: authHeaders(),
  })
  return handleResponse<CheckoutSessionResponse>(response)
}

export async function getMyOrders(): Promise<Order[]> {
  const response = await fetch('/api/users/me/orders', { headers: authHeaders() })
  return handleResponse<Order[]>(response)
}

export async function getDevelopmentPaymentStatus(): Promise<boolean> {
  if (!import.meta.env.DEV) return false
  try {
    const response = await fetch('/api/dev/payments/status', { headers: authHeaders() })
    if (!response.ok) return false
    const status = await response.json() as { enabled?: boolean; environment?: string }
    return status.enabled === true && status.environment === 'development'
  } catch {
    return false
  }
}

export async function simulateDevelopmentPayment(id: number): Promise<Order> {
  const response = await fetch(`/api/dev/orders/${id}/simulate-payment`, {
    method: 'POST',
    headers: authHeaders(),
  })
  return handleResponse<Order>(response)
}
