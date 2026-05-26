export type Difficulty = 'EASY' | 'MODERATE' | 'HARD' | 'EXTREME'
export type RouteStatus = 'DRAFT' | 'PUBLISHED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export type Route = {
  id: number
  title: string
  description: string
  origin: string
  destination: string
  date: string
  distanceKm: number
  difficulty: Difficulty
  maxParticipants: number
  currentParticipants: number
  status: RouteStatus
}

export type CreateRouteRequest = {
  title: string
  description: string
  origin: string
  destination: string
  date: string
  distanceKm: number
  difficulty: Difficulty
  maxParticipants: number
}

export type UpdateRouteRequest = CreateRouteRequest & {
  currentParticipants: number
  status: RouteStatus
}
