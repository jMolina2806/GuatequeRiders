import { createContext, useContext } from 'react'
import type { LoginRequest, RegisterRequest, User } from '../types/auth.types'

export type AuthContextValue = {
  user: User | null
  isAuthenticated: boolean
  isAdmin: boolean
  isLoading: boolean
  login: (data: LoginRequest) => Promise<User>
  register: (data: RegisterRequest) => Promise<User>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (context === null) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
