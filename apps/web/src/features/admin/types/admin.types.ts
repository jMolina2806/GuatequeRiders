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
  role: 'USER' | 'ADMIN'
}
