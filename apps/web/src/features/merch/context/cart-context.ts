import { createContext } from 'react'
import type { CartLine } from '../types/cart.types'
import type { ProductCode, ProductSize } from '../types/merch.types'

export type CartContextValue = {
  lines: CartLine[]
  totalUnits: number
  subtotal: number
  addItem: (productCode: ProductCode, size: ProductSize, customName: string | null, quantity: number) => void
  setQuantity: (productCode: ProductCode, size: ProductSize, customName: string | null, quantity: number) => void
  removeItem: (productCode: ProductCode, size: ProductSize, customName: string | null) => void
  clear: () => void
}

export const CartContext = createContext<CartContextValue | null>(null)
