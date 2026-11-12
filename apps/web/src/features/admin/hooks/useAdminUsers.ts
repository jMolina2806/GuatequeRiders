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
  const [memberNumberUpdatingId, setMemberNumberUpdatingId] = useState<number | null>(null)
  const [founderUpdatingId, setFounderUpdatingId] = useState<number | null>(null)
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

  const changeMembership = async (userId: number, active: boolean): Promise<User> => {
    setMembershipUpdatingId(userId)
    setFeedback(null)
    try {
      const updated = await adminService.updateUserMembership(userId, active)
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

  const changeFounder = async (userId: number, founder: boolean): Promise<User> => {
    setFounderUpdatingId(userId)
    setFeedback(null)
    try {
      const updated = await adminService.updateFounder(userId, founder)
      setUsers((prev) => prev.map((u) => (u.id === userId ? updated : u)))
      setFeedback({
        message: founder ? 'Miembro marcado como fundador' : 'Se ha quitado la condición de fundador',
        type: 'success',
      })
      return updated
    } catch (err) {
      setFeedback({
        message: err instanceof Error ? err.message : 'Error al actualizar el estado de fundador',
        type: 'error',
      })
      throw err
    } finally {
      setFounderUpdatingId(null)
    }
  }

  const changeMemberNumber = async (userId: number, memberNumber: string): Promise<User> => {
    setMemberNumberUpdatingId(userId)
    setFeedback(null)
    try {
      const updated = await adminService.updateMemberNumber(userId, memberNumber)
      setUsers((prev) => prev.map((u) => (u.id === userId ? updated : u)))
      setFeedback({ message: 'Número de socio actualizado correctamente', type: 'success' })
      return updated
    } catch (err) {
      setFeedback({
        message: err instanceof Error ? err.message : 'Error al actualizar el número de socio',
        type: 'error',
      })
      throw err
    } finally {
      setMemberNumberUpdatingId(null)
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
    memberNumberUpdatingId,
    founderUpdatingId,
    feedback,
    retry: fetchUsers,
    changeRole,
    changeMembership,
    changeMemberNumber,
    changeFounder,
    clearFeedback,
  }
}
