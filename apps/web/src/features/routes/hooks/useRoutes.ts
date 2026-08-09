import { useCallback, useEffect, useMemo, useState } from 'react'
import * as routesService from '../services/routes.service'
import { useAuth } from '@/features/auth/context/useAuth'
import type { CreateRouteRequest, Route, RouteStatus } from '@/features/routes/types/route.types'

export type RegistrationFeedback = {
  message: string
  type: 'success' | 'error'
}

export function useRoutes() {
  const { isAuthenticated } = useAuth()

  const [routes, setRoutes] = useState<Route[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [editingRouteId, setEditingRouteId] = useState<number | null>(null)
  const [registrations, setRegistrations] = useState<Record<number, boolean>>({})
  const [registrationLoading, setRegistrationLoading] = useState<Record<number, boolean>>({})
  const [feedback, setFeedback] = useState<RegistrationFeedback | null>(null)

  const refreshRegistrations = useCallback(async (routeList: Route[]) => {
    if (!isAuthenticated) {
      setRegistrations({})
      return
    }
    const results = await Promise.all(
      routeList.map(async (route) => {
        const status = await routesService
          .getMyRegistration(route.id)
          .catch(() => ({ registered: false }))
        return { id: route.id, registered: status.registered }
      }),
    )
    const next: Record<number, boolean> = {}
    results.forEach((result) => {
      next[result.id] = result.registered
    })
    setRegistrations(next)
  }, [isAuthenticated])

  const fetchRoutes = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await routesService.getRoutes()
      setRoutes(data)
      await refreshRegistrations(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar rutas')
    } finally {
      setIsLoading(false)
    }
  }, [refreshRegistrations])

  useEffect(() => {
    fetchRoutes()
  }, [fetchRoutes])

  const editingRoute = useMemo(
    () => routes.find((route) => route.id === editingRouteId) ?? null,
    [routes, editingRouteId],
  )

  const editingInitialValues = useMemo(() => {
    if (!editingRoute) return null
    return {
      title: editingRoute.title,
      description: editingRoute.description,
      date: editingRoute.date,
      origin: editingRoute.origin,
      destination: editingRoute.destination,
      distanceKm: editingRoute.distanceKm,
      difficulty: editingRoute.difficulty,
      maxParticipants: editingRoute.maxParticipants,
    } satisfies CreateRouteRequest
  }, [editingRoute])

  const submitLabel = editingRoute ? 'Guardar cambios' : 'Crear ruta'

  const updateParticipants = useCallback((routeId: number, delta: number) => {
    setRoutes((prev) =>
      prev.map((route) =>
        route.id === routeId
          ? { ...route, currentParticipants: Math.max(0, route.currentParticipants + delta) }
          : route,
      ),
    )
  }, [])

  const submitRoute = async (data: CreateRouteRequest) => {
    try {
      if (editingRouteId !== null) {
        const current = routes.find((r) => r.id === editingRouteId)
        await routesService.updateRoute(editingRouteId, {
          ...data,
          status: current?.status ?? 'DRAFT',
        })
        setEditingRouteId(null)
      } else {
        await routesService.createRoute(data)
      }
      await fetchRoutes()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al guardar ruta')
    }
  }

  const editRoute = (route: Route) => {
    setEditingRouteId(route.id)
  }

  const deleteRoute = async (routeId: number) => {
    try {
      await routesService.deleteRoute(routeId)
      if (editingRouteId === routeId) {
        setEditingRouteId(null)
      }
      await fetchRoutes()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al eliminar ruta')
    }
  }

  const closeRoute = async (routeId: number) => {
    try {
      const route = routes.find((r) => r.id === routeId)
      if (!route) return
      await routesService.updateRoute(routeId, {
        title: route.title,
        description: route.description,
        date: route.date,
        origin: route.origin,
        destination: route.destination,
        distanceKm: route.distanceKm,
        difficulty: route.difficulty,
        maxParticipants: route.maxParticipants,
        status: 'COMPLETED' satisfies RouteStatus,
      })
      await fetchRoutes()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cerrar ruta')
    }
  }

  const registerToRoute = async (routeId: number) => {
    setRegistrationLoading((prev) => ({ ...prev, [routeId]: true }))
    setFeedback(null)
    try {
      await routesService.registerToRoute(routeId)
      updateParticipants(routeId, 1)
      setRegistrations((prev) => ({ ...prev, [routeId]: true }))
      setFeedback({ message: 'Inscripción realizada correctamente', type: 'success' })
    } catch (err) {
      setFeedback({
        message: err instanceof Error ? err.message : 'Error al inscribirse',
        type: 'error',
      })
    } finally {
      setRegistrationLoading((prev) => ({ ...prev, [routeId]: false }))
    }
  }

  const unregisterFromRoute = async (routeId: number) => {
    setRegistrationLoading((prev) => ({ ...prev, [routeId]: true }))
    setFeedback(null)
    try {
      await routesService.unregisterFromRoute(routeId)
      updateParticipants(routeId, -1)
      setRegistrations((prev) => ({ ...prev, [routeId]: false }))
      setFeedback({ message: 'Inscripción cancelada', type: 'success' })
    } catch (err) {
      setFeedback({
        message: err instanceof Error ? err.message : 'Error al desapuntarse',
        type: 'error',
      })
    } finally {
      setRegistrationLoading((prev) => ({ ...prev, [routeId]: false }))
    }
  }

  const clearFeedback = () => {
    setFeedback(null)
  }

  const cancelEdit = () => {
    setEditingRouteId(null)
  }

  return {
    routes,
    editingInitialValues,
    submitLabel,
    isLoading,
    error,
    registrations,
    registrationLoading,
    feedback,
    submitRoute,
    editRoute,
    deleteRoute,
    closeRoute,
    registerToRoute,
    unregisterFromRoute,
    clearFeedback,
    cancelEdit,
  }
}
