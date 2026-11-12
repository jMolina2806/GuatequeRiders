import { getToken } from '@/features/auth/services/session'
import { translateApiMessage } from '@/utils/apiMessages'
import type {
  Campaign,
  CampaignDashboard,
  CampaignHistoryEntry,
  CloseCampaignResponse,
  Order,
  SupplierOrderBatch,
} from '../types/merch.types'

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

export async function getCurrentCampaign(): Promise<Campaign | null> {
  const response = await fetch('/api/merch/campaign/current')
  if (response.status === 404) return null
  return handleResponse<Campaign>(response)
}

export async function openCampaign(): Promise<Campaign> {
  const response = await fetch('/api/admin/merch/campaigns', {
    method: 'POST',
    headers: authHeaders(),
  })
  return handleResponse<Campaign>(response)
}

export async function getCampaigns(): Promise<CampaignHistoryEntry[]> {
  const response = await fetch('/api/admin/merch/campaigns', { headers: authHeaders() })
  return handleResponse<CampaignHistoryEntry[]>(response)
}

export async function getCampaignDashboard(id: number): Promise<CampaignDashboard> {
  const response = await fetch(`/api/admin/merch/campaigns/${id}/dashboard`, {
    headers: authHeaders(),
  })
  return handleResponse<CampaignDashboard>(response)
}

export async function closeCampaign(id: number): Promise<CloseCampaignResponse> {
  const response = await fetch(`/api/admin/merch/campaigns/${id}/close`, {
    method: 'POST',
    headers: authHeaders(),
  })
  return handleResponse<CloseCampaignResponse>(response)
}

export async function markCampaignOrdered(id: number): Promise<Campaign> {
  const response = await fetch(`/api/admin/merch/campaigns/${id}/ordered`, {
    method: 'POST',
    headers: authHeaders(),
  })
  return handleResponse<Campaign>(response)
}

export async function getSupplierOrder(id: number): Promise<SupplierOrderBatch> {
  const response = await fetch(`/api/admin/merch/campaigns/${id}/supplier-order`, {
    headers: authHeaders(),
  })
  return handleResponse<SupplierOrderBatch>(response)
}


export async function advanceOrderStatus(id: number): Promise<Order> {
  const response = await fetch(`/api/admin/orders/${id}/advance-status`, {
    method: 'POST',
    headers: authHeaders(),
  })
  return handleResponse<Order>(response)
}
