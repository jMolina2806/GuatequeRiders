import { getToken } from '@/features/auth/services/session'

export type NewsMedia = { url: string; type: 'IMAGE' | 'VIDEO' }
export type NewsItem = {
  id: number
  title: string
  summary: string
  body: string
  category: 'COLLABORATORS' | 'COMMUNITY' | 'ROUTES' | 'MULTIMEDIA' | 'PROMOTIONS'
  media: NewsMedia[]
  published: boolean
  featured: boolean
  publishAt: string | null
  createdAt: string
}
export type NewsInput = Omit<NewsItem, 'id' | 'createdAt'>

const base = '/api/news'
const headers = (json = false): HeadersInit => ({
  ...(json ? { 'Content-Type': 'application/json' } : {}),
  ...(getToken() ? { Authorization: `Bearer ${getToken()}` } : {}),
})

async function parse<T>(response: Response): Promise<T> {
  if (!response.ok) throw new Error((await response.json().catch(() => null))?.message || `Error ${response.status}`)
  if (response.status === 204) return undefined as T
  return response.json()
}
export const getNews = () => fetch(base).then((response) => parse<NewsItem[]>(response))
export const getAdminNews = () => fetch(`${base}/admin`, { headers: headers() }).then((response) => parse<NewsItem[]>(response))
export const saveNews = (input: NewsInput, id?: number) =>
  fetch(id ? `${base}/${id}` : base, {
    method: id ? 'PUT' : 'POST', headers: headers(true), body: JSON.stringify(input),
  }).then((response) => parse<NewsItem>(response))
export const deleteNews = (id: number) =>
  fetch(`${base}/${id}`, { method: 'DELETE', headers: headers() }).then((response) => parse<void>(response))
export const uploadNewsMedia = (file: File) => {
  const body = new FormData()
  body.append('file', file)
  return fetch(`${base}/media`, { method: 'POST', headers: headers(), body })
    .then((response) => parse<NewsMedia>(response))
}
export const CATEGORY_LABELS: Record<NewsItem['category'], string> = {
  COLLABORATORS: 'Colaboradores', COMMUNITY: 'Comunidad', ROUTES: 'Rutas',
  MULTIMEDIA: 'Multimedia', PROMOTIONS: 'Promociones',
}
