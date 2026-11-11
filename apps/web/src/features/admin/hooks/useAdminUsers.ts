import { useCallback, useEffect, useState } from 'react'
import type { User } from '@/features/auth/types/auth.types'
import * as adminService from '../services/admin.service'

export type AdminFeedback = {
  message: string
  type: 'success' | 'error'
}

export function useAdminUsers() {
  const [users, setUsers] = useState<User[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [roleUpdatingId, setRoleUpdatingId] = useState<number | null>(null)
  const [membershipUpdatingId, setMembershipUpdatingId] = useState<number | null>(null)
  const [feedback, setFeedback] = useState<AdminFeedback | null>(null)

  const fetchUsers = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await adminService.getUsers()
      setUsers(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar usuarios')
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchUsers()
  }, [fetchUsers])

  const changeRole = async (userId: number, role: 'USER' | 'ADMIN'): Promise<User> => {
    setRoleUpdatingId(userId)
    setFeedback(null)
    try {
      const updated = await adminService.updateUserRole(userId, role)
      setUsers((prev) => prev.map((u) => (u.id === userId ? updated : u)))
      setFeedback({
        message: role === 'ADMIN' ? 'Rol cambiado a administrador' : 'Rol cambiado a usuario',
        type: 'success',
      })
      return updated
    } catch (err) {
      setFeedback({
        message: err instanceof Error ? err.message : 'Error al cambiar el rol',
        type: 'error',
      })
      throw err
    } finally {
      setRoleUpdatingId(null)
    }
  }

  const changeMembership = async (
    userId: number,
    active: boolean,
    validUntil?: string,
  ): Promise<User> => {
    setMembershipUpdatingId(userId)
    setFeedback(null)
    try {
      const updated = await adminService.updateUserMembership(userId, active, validUntil)
      setUsers((prev) => prev.map((u) => (u.id === userId ? updated : u)))
      setFeedback({
        message: active ? 'Socio activado correctamente' : 'Membresía desactivada',
        type: 'success',
      })
      return updated
    } catch (err) {
      setFeedback({
        message: err instanceof Error ? err.message : 'Error al actualizar la membresía',
        type: 'error',
      })
      throw err
    } finally {
      setMembershipUpdatingId(null)
    }
  }

  const clearFeedback = () => {
    setFeedback(null)
  }

  return {
    users,
    isLoading,
    error,
    roleUpdatingId,
    membershipUpdatingId,
    feedback,
    retry: fetchUsers,
    changeRole,
    changeMembership,
    clearFeedback,
  }
}
