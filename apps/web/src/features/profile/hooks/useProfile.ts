import { useCallback, useEffect, useState } from 'react'
import * as profileService from '../services/profile.service'
import type { ProfileStats } from '../types/profile.types'
import { useAuth } from '@/features/auth/context/useAuth'
import type { Event } from '@/features/events/types/event.types'
import type { Route } from '@/features/routes/types/route.types'

export function useProfile() {
  const { isAuthenticated } = useAuth()

  const [routes, setRoutes] = useState<Route[]>([])
  const [events, setEvents] = useState<Event[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchProfile = useCallback(async () => {
    if (!isAuthenticated) return
    setIsLoading(true)
    setError(null)
    try {
      const [routeData, eventData] = await Promise.all([
        profileService.getMyRoutes(),
        profileService.getMyEvents(),
      ])
      setRoutes(routeData)
      setEvents(eventData)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar tu perfil')
    } finally {
      setIsLoading(false)
    }
  }, [isAuthenticated])

  useEffect(() => {
    if (!isAuthenticated) {
      setRoutes([])
      setEvents([])
      setError(null)
      setIsLoading(false)
      return
    }
    fetchProfile()
  }, [fetchProfile, isAuthenticated])

  const stats: ProfileStats = {
    routesCount: routes.length,
    eventsCount: events.length,
    totalKm: Math.round(routes.reduce((sum, route) => sum + (route.distanceKm ?? 0), 0) * 10) / 10,
  }

  return {
    routes,
    events,
    stats,
    isLoading,
    error,
    retry: fetchProfile,
  }
}
