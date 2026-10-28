import { getToken } from '@/features/auth/services/session'

export type CommunityLinks = {
  whatsappGroupUrl: string | null
}

export async function getCommunityLinks(): Promise<CommunityLinks> {
  const token = getToken()

  if (!token) {
    return { whatsappGroupUrl: null }
  }

  const response = await fetch('/api/community/links', {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })

  if (!response.ok) {
    return { whatsappGroupUrl: null }
  }

  return response.json() as Promise<CommunityLinks>
}
