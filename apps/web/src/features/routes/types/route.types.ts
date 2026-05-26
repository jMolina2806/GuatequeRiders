export type Participant = {
  id: number
  name: string
}

export type RouteDifficulty = 'Fácil' | 'Media' | 'Difícil'
export type RouteStatus = 'Abierta' | 'Cerrada'

export type Route = {
  id: number
  title: string
  description: string
  date: string
  time: string
  origin: string
  destination: string
  distanceKm: number
  difficulty: RouteDifficulty
  status: RouteStatus
  maxParticipants: number
  participants: Participant[]
  imageUrl: string
}

export type CreateRouteFormData = {
  title: string
  description: string
  date: string
  time: string
  origin: string
  destination: string
  distanceKm: number
  difficulty: RouteDifficulty
  status: RouteStatus
  maxParticipants: number
}