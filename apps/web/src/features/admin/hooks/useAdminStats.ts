import { useCallback, useEffect, useState } from 'react'
import * as adminService from '../services/admin.service'
import type { AdminStats } from '../types/admin.types'

export function useAdminStats() {
  const [stats, setStats] = useState<AdminStats | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchStats = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await adminService.getAdminStats()
      setStats(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar estadísticas')
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchStats()
  }, [fetchStats])

  return { stats, isLoading, error, retry: fetchStats }
}
