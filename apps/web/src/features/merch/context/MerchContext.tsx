import { useCallback, useEffect, useState, type ReactNode } from 'react'
import * as merchService from '../services/merch.service'
import type { Campaign } from '../types/merch.types'
import { MerchContext } from './merch-context'

export function MerchProvider({ children }: { children: ReactNode }) {
  const [campaign, setCampaign] = useState<Campaign | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const refresh = useCallback(async (silent = false) => {
    if (!silent) setIsLoading(true)
    try {
      const currentCampaign = await merchService.getCurrentCampaign()
      setCampaign(currentCampaign)
      setError(null)
    } catch (err) {
      setCampaign(null)
      setError(err instanceof Error ? err.message : 'No se pudo cargar el estado del Merch')
    } finally {
      if (!silent) setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    void refresh()
    const interval = window.setInterval(() => { void refresh(true) }, 60_000)
    const refreshOnFocus = () => { void refresh(true) }
    window.addEventListener('focus', refreshOnFocus)
    return () => {
      window.clearInterval(interval)
      window.removeEventListener('focus', refreshOnFocus)
    }
  }, [refresh])

  const openCampaign = useCallback(async () => {
    try {
      const newCampaign = await merchService.openCampaign()
      setCampaign(newCampaign)
      setError(null)
      return newCampaign
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo abrir una nueva campaña')
      throw err
    }
  }, [])

  const isOpen = campaign?.status === 'OPEN'

  return (
    <MerchContext.Provider value={{ campaign, isOpen, isLoading, error, refresh, openCampaign }}>
      {children}
    </MerchContext.Provider>
  )
}
