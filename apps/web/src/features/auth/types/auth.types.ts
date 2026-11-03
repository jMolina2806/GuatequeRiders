export type UserRole = 'USER' | 'ADMIN'

export type User = {
  id: number
  username: string
  email: string
  role: UserRole
  createdAt: string
  memberActive: boolean
  memberNumber: string | null
  membershipValidUntil: string | null
}

export type LoginRequest = {
  username: string
  password: string
}

export type RegisterRequest = {
  username: string
  email: string
  password: string
  privacyAccepted: boolean
}

export type AuthResponse = {
  token: string
  user: User
}
