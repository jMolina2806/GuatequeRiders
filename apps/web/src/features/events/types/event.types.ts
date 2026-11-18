export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CLOSED' | 'CANCELLED'

export type Event = {
  id: number
  title: string
  description: string
  location: string
  date: string
  startTime: string
  endTime: string | null
  maxParticipants: number | null
  currentParticipants: number
  status: EventStatus
  createdAt: string
}

export type CreateEventRequest = {
  title: string
  description: string
  location: string
  date: string
  startTime: string
  endTime: string | null
  maxParticipants: number | null
}

export type UpdateEventRequest = CreateEventRequest & {
  status: EventStatus
}

export type EventRegistrationStatus = {
  registered: boolean
}
