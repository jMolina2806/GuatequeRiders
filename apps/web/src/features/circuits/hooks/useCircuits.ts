import { useCallback, useEffect, useMemo, useState } from 'react'
import * as circuitsService from '../services/circuits.service'
import { useAuth } from '@/features/auth/context/useAuth'
import type {
  Circuit,
  CreateCircuitRequest,
} from '@/features/circuits/types/circuit.types'

export type CircuitFeedback = {
  message: string
  type: 'success' | 'error'
}

type UseCircuitsOptions = {
  publicOnly?: boolean
}

export function useCircuits({ publicOnly = false }: UseCircuitsOptions = {}) {
  const { isAuthenticated, isLoading: authLoading } = useAuth()
  const [allCircuits, setAllCircuits] = useState<Circuit[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [editingCircuitId, setEditingCircuitId] = useState<number | null>(null)
  const [feedback, setFeedback] = useState<CircuitFeedback | null>(null)

  const fetchCircuits = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = publicOnly
        ? await circuitsService.getPublicCircuits()
        : await circuitsService.getCircuits()
      setAllCircuits(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar circuitos')
    } finally {
      setIsLoading(false)
    }
  }, [publicOnly])

  useEffect(() => {
    if (authLoading && !publicOnly) return
    fetchCircuits()
  }, [authLoading, fetchCircuits, isAuthenticated, publicOnly])

  const circuits = useMemo(() => allCircuits, [allCircuits])

  const editingCircuit = useMemo(
    () => allCircuits.find((circuit) => circuit.id === editingCircuitId) ?? null,
    [allCircuits, editingCircuitId],
  )

  const editingInitialValues = useMemo(() => {
    if (!editingCircuit) return null
    return {
      title: editingCircuit.title,
      circuitName: editingCircuit.circuitName,
      description: editingCircuit.description,
      location: editingCircuit.location,
      date: editingCircuit.date,
      startTime: editingCircuit.startTime,
      endTime: editingCircuit.endTime,
      price: editingCircuit.price,
      discount: editingCircuit.discount,
      benefit: editingCircuit.benefit,
      registrationUrl: editingCircuit.registrationUrl,
      informationUrl: editingCircuit.informationUrl,
      imageUrl: editingCircuit.imageUrl,
    } satisfies CreateCircuitRequest
  }, [editingCircuit])

  const submitCircuit = async (data: CreateCircuitRequest) => {
    try {
      if (editingCircuitId !== null) {
        await circuitsService.updateCircuit(editingCircuitId, data)
        setEditingCircuitId(null)
      } else {
        await circuitsService.createCircuit(data)
      }
      await fetchCircuits()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al guardar circuito')
    }
  }

  const editCircuit = (circuit: Circuit) => {
    setEditingCircuitId(circuit.id)
  }

  const deleteCircuit = async (circuitId: number) => {
    try {
      await circuitsService.deleteCircuit(circuitId)
      if (editingCircuitId === circuitId) setEditingCircuitId(null)
      await fetchCircuits()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al eliminar circuito')
    }
  }

  const publishCircuit = async (circuitId: number) => {
    await changeStatus(circuitId, circuitsService.publishCircuit, 'Actividad publicada correctamente')
  }

  const completeCircuit = async (circuitId: number) => {
    await changeStatus(circuitId, circuitsService.completeCircuit, 'Actividad cerrada correctamente')
  }

  const cancelCircuit = async (circuitId: number) => {
    await changeStatus(circuitId, circuitsService.cancelCircuit, 'Actividad cancelada correctamente')
  }

  const changeStatus = async (
    circuitId: number,
    action: (id: number) => Promise<Circuit>,
    successMessage: string,
  ) => {
    setFeedback(null)
    try {
      await action(circuitId)
      await fetchCircuits()
      setFeedback({ message: successMessage, type: 'success' })
    } catch (err) {
      setFeedback({
        message: err instanceof Error ? err.message : 'Error al cambiar el estado',
        type: 'error',
      })
    }
  }

  const clearFeedback = () => setFeedback(null)
  const cancelEdit = () => setEditingCircuitId(null)

  return {
    circuits,
    editingInitialValues,
    submitLabel: editingCircuit ? 'Guardar cambios' : 'Crear actividad',
    isLoading,
    error,
    feedback,
    retry: fetchCircuits,
    submitCircuit,
    editCircuit,
    deleteCircuit,
    publishCircuit,
    completeCircuit,
    cancelCircuit,
    clearFeedback,
    cancelEdit,
  }
}
