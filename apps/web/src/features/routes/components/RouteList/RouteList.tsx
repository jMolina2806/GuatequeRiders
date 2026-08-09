import RouteCard from '../RouteCard'
import type { Route } from '@/features/routes/types/route.types'

type RouteListProps = {
  routes: Route[]
  isAdmin?: boolean
  isAuthenticated?: boolean
  registrations?: Record<number, boolean>
  registrationLoading?: Record<number, boolean>
  onEdit?: (route: Route) => void
  onDelete?: (routeId: number) => void
  onClose?: (routeId: number) => void
  onEnroll?: (route: Route) => void
  onUnregister?: (route: Route) => void
  onDetails?: (route: Route) => void
}

export default function RouteList({
  routes,
  isAdmin = false,
  isAuthenticated = false,
  registrations = {},
  registrationLoading = {},
  onEdit,
  onDelete,
  onClose,
  onEnroll,
  onUnregister,
  onDetails,
}: RouteListProps) {
  return (
    <section>
      {routes.map((route) => (
        <RouteCard
          key={route.id}
          route={route}
          isAdmin={isAdmin}
          isAuthenticated={isAuthenticated}
          isRegistered={registrations[route.id]}
          isProcessing={registrationLoading[route.id]}
          onEdit={onEdit}
          onDelete={onDelete}
          onClose={onClose}
          onEnroll={onEnroll}
          onUnregister={onUnregister}
          onDetails={onDetails}
        />
      ))}
    </section>
  )
}
