import { useContext } from 'react'
import { MerchContext } from './merch-context'
import type { MerchContextValue } from './merch-context'

export function useMerch(): MerchContextValue {
  const context = useContext(MerchContext)
  if (!context) throw new Error('useMerch must be used within MerchProvider')
  return context
}
