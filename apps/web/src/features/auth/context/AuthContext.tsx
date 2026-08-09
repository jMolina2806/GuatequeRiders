import { useEffect, useState, type ReactNode } from 'react'
import * as authService from '../services/auth.service'
import {
  clearSession,
  getStoredUser,
  getToken,
  saveSession,
} from '../services/session'
import type { LoginRequest, RegisterRequest, User } from '../types/auth.types'
import { AuthContext } from './useAuth'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => getStoredUser())
  const [isLoading, setIsLoading] = useState(() => getToken() !== null)

  useEffect(() => {
    const token = getToken()
    if (!token) return

    let cancelled = false

    authService
      .getCurrentUser(token)
      .then((current) => {
        if (cancelled) return
        setUser(current)
      })
      .catch(() => {
        if (cancelled) return
        clearSession()
        setUser(null)
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const login = async (data: LoginRequest): Promise<User> => {
    const response = await authService.login(data)
    saveSession(response.token, response.user)
    setUser(response.user)
    return response.user
  }

  const register = async (data: RegisterRequest): Promise<User> => {
    const response = await authService.register(data)
    saveSession(response.token, response.user)
    setUser(response.user)
    return response.user
  }

  const logout = () => {
    clearSession()
    setUser(null)
  }

  const value = {
    user,
    isAuthenticated: user !== null,
    isAdmin: user?.role === 'ADMIN',
    isLoading,
    login,
    register,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
