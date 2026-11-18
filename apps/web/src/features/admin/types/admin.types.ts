import type { UserRole } from '@/features/auth/types/auth.types'

export type AdminStats = {
  totalUsers: number
  totalRoutes: number
  publishedRoutes: number
  totalRouteRegistrations: number
  totalEvents: number
  publishedEvents: number
  totalEventRegistrations: number
}

export type UpdateRoleRequest = {
  role: UserRole
}
