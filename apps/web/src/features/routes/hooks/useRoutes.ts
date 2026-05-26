import { useCallback, useEffect, useMemo, useState } from 'react'
import * as routesService from '../services/routes.service'
import type { CreateRouteRequest, Route, RouteStatus } from '@/features/routes/types/route.types'

export function useRoutes() {
  const [routes, setRoutes] = useState<Route[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [editingRouteId, setEditingRouteId] = useState<number | null>(null)

  const fetchRoutes = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await routesService.getRoutes()
      setRoutes(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar rutas')
    } finally {
      setIsLoading(false)
    }
  }, [])

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

  const submitRoute = async (data: CreateRouteRequest) => {
    try {
      if (editingRouteId !== null) {
        const current = routes.find((r) => r.id === editingRouteId)
        await routesService.updateRoute(editingRouteId, {
          ...data,
          currentParticipants: current?.currentParticipants ?? 0,
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
        currentParticipants: route.currentParticipants,
        status: 'COMPLETED' satisfies RouteStatus,
      })
      await fetchRoutes()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cerrar ruta')
    }
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
    submitRoute,
    editRoute,
    deleteRoute,
    closeRoute,
    cancelEdit,
  }
}
