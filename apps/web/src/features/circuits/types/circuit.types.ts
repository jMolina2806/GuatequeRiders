export type CircuitStatus = 'DRAFT' | 'PUBLISHED' | 'COMPLETED' | 'CANCELLED'

export type Circuit = {
  id: number
  title: string
  circuitName: string
  location: string
  description: string
  date: string
  startTime: string
  endTime: string | null
  price: number | null
  discount: string | null
  benefit: string | null
  registrationUrl: string
  informationUrl: string | null
  imageUrl: string | null
  status: CircuitStatus
  createdAt: string
}

export type CreateCircuitRequest = {
  title: string
  circuitName: string
  description: string
  location: string
  date: string
  startTime: string
  endTime: string | null
  price: number | null
  discount: string | null
  benefit: string | null
  registrationUrl: string
  informationUrl: string | null
  imageUrl: string | null
}

export type UpdateCircuitRequest = CreateCircuitRequest
