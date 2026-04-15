import { useMemo, useState } from 'react'
import RouteForm from '../components/RouteForm'
import RouteList from '../components/RouteList/RouteList'
import { mockRoutes } from '../data/mockRoutes'
import type { CreateRouteFormData, Route } from '../types/route.types'

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

export default function AdminRoutesPage() {
  const [routes, setRoutes] = useState<Route[]>(mockRoutes)
  const [editingRouteId, setEditingRouteId] = useState<number | null>(null)

  const editingRoute = useMemo(
    () => routes.find((route) => route.id === editingRouteId) ?? null,
    [routes, editingRouteId],
  )

  const handleSubmitRoute = (data: CreateRouteFormData) => {
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
    }

    setRoutes((prev) => [newRoute, ...prev])
  }

  const handleEditRoute = (route: Route) => {
    setEditingRouteId(route.id)
  }

  const handleDeleteRoute = (routeId: number) => {
    setRoutes((prev) => prev.filter((route) => route.id !== routeId))

    if (editingRouteId === routeId) {
      setEditingRouteId(null)
    }
  }

  const handleCloseRoute = (routeId: number) => {
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

  return (
    <section>
      <h1>Administración de rutas</h1>
      <p>Desde aquí el administrador puede crear, editar, cerrar y eliminar rutas.</p>

      <div style={{ margin: '24px 0' }}>
        <RouteForm
          onSubmit={handleSubmitRoute}
          initialValues={editingRoute ? mapRouteToFormData(editingRoute) : null}
          submitLabel={editingRoute ? 'Guardar cambios' : 'Crear ruta'}
        />
      </div>

      <h2>Rutas creadas</h2>

      <RouteList
        routes={routes}
        isAdminView={true}
        onEdit={handleEditRoute}
        onDelete={handleDeleteRoute}
        onClose={handleCloseRoute}
      />
    </section>
  )
}