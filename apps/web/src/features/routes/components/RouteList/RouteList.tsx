import RouteCard from '../RouteCard'
import type { Route } from '@/features/routes/types/route.types'

type RouteListProps = {
  routes: Route[]
  isAdmin?: boolean
  onEdit?: (route: Route) => void
  onDelete?: (routeId: number) => void
  onClose?: (routeId: number) => void
}

export default function RouteList({
  routes,
  isAdmin = false,
  onEdit,
  onDelete,
  onClose,
}: RouteListProps) {
  return (
    <section>
      {routes.map((route) => (
        <RouteCard
          key={route.id}
          route={route}
          isAdmin={isAdmin}
          onEdit={onEdit}
          onDelete={onDelete}
          onClose={onClose}
        />
      ))}
    </section>
  )
}