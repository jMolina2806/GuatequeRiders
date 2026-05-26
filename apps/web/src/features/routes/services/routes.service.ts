import { mockRoutes } from '../data/mockRoutes'
import type { Route } from '@/features/routes/types/route.types'

export function getRoutes(): Route[] {
  return mockRoutes
}
