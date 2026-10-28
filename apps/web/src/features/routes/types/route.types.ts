export type Difficulty = 'EASY' | 'MODERATE' | 'HARD' | 'EXTREME'
export type RouteStatus = 'DRAFT' | 'PUBLISHED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export type RouteOrganizer = {
  id: number
  username: string
}

export type Route = {
  id: number
  title: string
  description: string
  origin: string
  destination: string
  date: string
  meetingTime: string | null
  departureTime: string | null
  distanceKm: number
  difficulty: Difficulty
  maxParticipants: number
  currentParticipants: number
  status: RouteStatus
  organizers: RouteOrganizer[]
}

export type CreateRouteRequest = {
  title: string
  description: string
  origin: string
  destination: string
  date: string
  meetingTime: string
  departureTime: string
  distanceKm: number
  difficulty: Difficulty
  maxParticipants: number
  organizerIds: number[]
}

export type UpdateRouteRequest = CreateRouteRequest & {
  status: RouteStatus
}

export type RegistrationStatus = {
  registered: boolean
}

export type RoutePhoto = {
  id: number
  imageUrl: string
  downloadUrl: string
  uploadedBy: {
    id: number
    username: string
  }
  createdAt: string
}
