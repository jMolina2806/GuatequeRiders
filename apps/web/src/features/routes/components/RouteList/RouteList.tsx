import RouteCard from '../RouteCard'
import type { Route } from '../../types/route.types'

type RouteListProps = {
  routes: Route[]
  isAdminView?: boolean
  onEdit?: (route: Route) => void
  onDelete?: (routeId: number) => void
  onClose?: (routeId: number) => void
}

export default function RouteList({
  routes,
  isAdminView = false,
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
          isAdminView={isAdminView}
          onEdit={onEdit}
          onDelete={onDelete}
          onClose={onClose}
        />
      ))}
    </section>
  )
}