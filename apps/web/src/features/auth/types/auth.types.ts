export type UserRole = 'USER' | 'ADMIN'

export type User = {
  id: number
  username: string
  email: string
  role: UserRole
  createdAt: string
}

export type LoginRequest = {
  username: string
  password: string
}

export type RegisterRequest = {
  username: string
  email: string
  password: string
}

export type AuthResponse = {
  token: string
  user: User
}
