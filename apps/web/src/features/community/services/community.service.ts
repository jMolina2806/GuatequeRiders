import { getToken } from '@/features/auth/services/session'

export type CommunityLinks = {
  whatsappGroupUrl: string | null
}

let cachedToken: string | null = null
let cachedRequest: Promise<CommunityLinks> | null = null

export function getCommunityLinks(): Promise<CommunityLinks> {
  const token = getToken()

  if (!token) {
    cachedToken = null
    cachedRequest = null
    return Promise.resolve({ whatsappGroupUrl: null })
  }

  if (cachedToken === token && cachedRequest) {
    return cachedRequest
  }

  cachedToken = token
  cachedRequest = fetch('/api/community/links', {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
    .then((response) => {
      if (!response.ok) {
        return { whatsappGroupUrl: null }
      }

      return response.json() as Promise<CommunityLinks>
    })
    .catch(() => ({ whatsappGroupUrl: null }))

  return cachedRequest
}
