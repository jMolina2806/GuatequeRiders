import { getToken } from '@/features/auth/services/session'
import { translateApiMessage } from '@/utils/apiMessages'

export type MemberCard = {
  memberNumber: string
  username: string
  verificationCode: string
  verificationExpiresAt: string
}

export type MembershipVerification = {
  valid: boolean
  memberNumber: string
  username: string
  verificationCode: string
  expiresAt: string
}

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

export async function getMyMemberCard(): Promise<MemberCard> {
  const response = await fetch('/api/membership/me/card', {
    headers: {
      ...authHeaders(),
      Accept: 'application/json',
      'Cache-Control': 'no-store',
    },
    cache: 'no-store',
  })
  return handleResponse<MemberCard>(response)
}

export async function verifyMemberCode(memberNumber: string, code: string): Promise<MembershipVerification> {
  const response = await fetch('/api/membership/verify-code', {
    method: 'POST',
    headers: {
      ...authHeaders(),
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'Cache-Control': 'no-store',
    },
    cache: 'no-store',
    body: JSON.stringify({ memberNumber, code }),
  })
  return handleResponse<MembershipVerification>(response)
}

export async function verifyMembership(token: string): Promise<MembershipVerification> {
  const response = await fetch('/api/membership/verify', {
    method: 'POST',
    headers: {
      ...authHeaders(),
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'Cache-Control': 'no-store',
    },
    cache: 'no-store',
    body: JSON.stringify({ token }),
  })
  return handleResponse<MembershipVerification>(response)
}
