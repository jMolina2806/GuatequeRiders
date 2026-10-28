import { createContext } from 'react'
import type { Campaign } from '../types/merch.types'

export type MerchContextValue = {
  campaign: Campaign | null
  isOpen: boolean
  isLoading: boolean
  error: string | null
  refresh: () => Promise<void>
  openCampaign: () => Promise<Campaign>
}

export const MerchContext = createContext<MerchContextValue | null>(null)
