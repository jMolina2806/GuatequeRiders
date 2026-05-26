import { useMemo, useState } from 'react'
import { getRoutes } from '../services/routes.service'
import type { CreateRouteFormData, Route } from '@/features/routes/types/route.types'

function mapRouteToFormData(route: Route): CreateRouteFormData {
  return {
    title: route.title,
    description: route.description,
    date: route.date,
    time: route.time,
    origin: route.origin,
    destination: route.destination,
    distanceKm: route.distanceKm,
    difficulty: route.difficulty,
    status: route.status,
    maxParticipants: route.maxParticipants,
  }
}

export function useRoutes() {
  const [routes, setRoutes] = useState<Route[]>(getRoutes())
  const [editingRouteId, setEditingRouteId] = useState<number | null>(null)
  const [isLoading] = useState(false)
  const [error] = useState<string | null>(null)

  const editingRoute = useMemo(
    () => routes.find((route) => route.id === editingRouteId) ?? null,
    [routes, editingRouteId],
  )

  const editingInitialValues = useMemo(
    () => (editingRoute ? mapRouteToFormData(editingRoute) : null),
    [editingRoute],
  )

  const submitLabel = editingRoute ? 'Guardar cambios' : 'Crear ruta'

  const submitRoute = (data: CreateRouteFormData) => {
    if (editingRouteId !== null) {
      setRoutes((prev) =>
        prev.map((route) =>
          route.id === editingRouteId
            ? {
                ...route,
                ...data,
              }
            : route,
        ),
      )

      setEditingRouteId(null)
      return
    }

    const newRoute: Route = {
      id: Date.now(),
      ...data,
      participants: [],
      imageUrl: '',
    }

    setRoutes((prev) => [newRoute, ...prev])
  }

  const editRoute = (route: Route) => {
    setEditingRouteId(route.id)
  }

  const deleteRoute = (routeId: number) => {
    setRoutes((prev) => prev.filter((route) => route.id !== routeId))

    if (editingRouteId === routeId) {
      setEditingRouteId(null)
    }
  }

  const cancelEdit = () => {
    setEditingRouteId(null)
  }

  const closeRoute = (routeId: number) => {
    setRoutes((prev) =>
      prev.map((route) =>
        route.id === routeId
          ? {
              ...route,
              status: 'Cerrada',
            }
          : route,
      ),
    )
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
