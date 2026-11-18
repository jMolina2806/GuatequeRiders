import { useCallback, useEffect, useMemo, useState } from 'react'
import { useAuth } from '@/features/auth/context/useAuth'
import * as collaboratorsService from '../services/collaborators.service'
import type {
  Collaborator,
  CreateCollaboratorRequest,
} from '../types/collaborator.types'

export type CollaboratorFeedback = {
  message: string
  type: 'success' | 'error'
}

type UseCollaboratorsOptions = {
  publicOnly?: boolean
}

export function useCollaborators({ publicOnly = false }: UseCollaboratorsOptions = {}) {
  const { isAdmin, isLoading: authLoading } = useAuth()
  const [collaborators, setCollaborators] = useState<Collaborator[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [feedback, setFeedback] = useState<CollaboratorFeedback | null>(null)
  const [editingCollaboratorId, setEditingCollaboratorId] = useState<number | null>(null)

  const fetchCollaborators = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await collaboratorsService.getCollaborators(publicOnly)
      setCollaborators(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar colaboradores')
    } finally {
      setIsLoading(false)
    }
  }, [publicOnly])

  useEffect(() => {
    if (authLoading && !publicOnly) return
    fetchCollaborators()
  }, [authLoading, fetchCollaborators, isAdmin, publicOnly])

  const editingCollaborator = useMemo(
    () => collaborators.find((collaborator) => collaborator.id === editingCollaboratorId) ?? null,
    [collaborators, editingCollaboratorId],
  )

  const saveCollaborator = async (
    data: CreateCollaboratorRequest,
    imageFile: File | null,
  ): Promise<boolean> => {
    setFeedback(null)
    try {
      const saved = editingCollaboratorId === null
        ? await collaboratorsService.createCollaborator(data)
        : await collaboratorsService.updateCollaborator(editingCollaboratorId, data)
      setCollaborators((previous) => replaceCollaborator(previous, saved))

      if (imageFile) {
        try {
          const withImage = await collaboratorsService.uploadCollaboratorImage(saved.id, imageFile)
          setCollaborators((previous) => replaceCollaborator(previous, withImage))
        } catch (err) {
          setFeedback({
            message: err instanceof Error
              ? `Colaborador guardado, pero no se pudo subir la imagen: ${err.message}`
              : 'Colaborador guardado, pero no se pudo subir la imagen',
            type: 'error',
          })
          return false
        }
      }

      setEditingCollaboratorId(null)
      setFeedback({ message: 'Colaborador guardado correctamente', type: 'success' })
      return true
    } catch (err) {
      setFeedback({
        message: err instanceof Error ? err.message : 'Error al guardar colaborador',
        type: 'error',
      })
      return false
    }
  }

  const deleteCollaborator = async (id: number): Promise<boolean> => {
    setFeedback(null)
    try {
      await collaboratorsService.deleteCollaborator(id)
      setCollaborators((previous) => previous.filter((collaborator) => collaborator.id !== id))
      setFeedback({ message: 'Colaborador eliminado correctamente', type: 'success' })
      return true
    } catch (err) {
      setFeedback({
        message: err instanceof Error ? err.message : 'Error al eliminar colaborador',
        type: 'error',
      })
      return false
    }
  }

  const setCollaboratorActive = async (collaborator: Collaborator, active: boolean): Promise<boolean> => {
    setFeedback(null)
    try {
      const updated = await collaboratorsService.updateCollaboratorActive(collaborator.id, active)
      setCollaborators((previous) => replaceCollaborator(previous, updated))
      setFeedback({
        message: active ? 'Colaborador activado' : 'Colaborador desactivado',
        type: 'success',
      })
      return true
    } catch (err) {
      setFeedback({
        message: err instanceof Error ? err.message : 'Error al cambiar el estado',
        type: 'error',
      })
      return false
    }
  }

  const removeCollaboratorImage = async (id: number): Promise<boolean> => {
    setFeedback(null)
    try {
      const updated = await collaboratorsService.deleteCollaboratorImage(id)
      setCollaborators((previous) => replaceCollaborator(previous, updated))
      setFeedback({ message: 'Imagen eliminada correctamente', type: 'success' })
      return true
    } catch (err) {
      setFeedback({
        message: err instanceof Error ? err.message : 'Error al eliminar la imagen',
        type: 'error',
      })
      return false
    }
  }

  const clearFeedback = () => setFeedback(null)
  const editCollaborator = (collaborator: Collaborator) => setEditingCollaboratorId(collaborator.id)
  const cancelEdit = () => setEditingCollaboratorId(null)

  return {
    collaborators,
    editingCollaborator,
    isLoading,
    error,
    feedback,
    retry: fetchCollaborators,
    saveCollaborator,
    deleteCollaborator,
    setCollaboratorActive,
    removeCollaboratorImage,
    editCollaborator,
    cancelEdit,
    clearFeedback,
  }
}

function replaceCollaborator(collaborators: Collaborator[], updated: Collaborator): Collaborator[] {
  const exists = collaborators.some((collaborator) => collaborator.id === updated.id)
  if (!exists) return [updated, ...collaborators]
  return collaborators.map((collaborator) => collaborator.id === updated.id ? updated : collaborator)
}
