import { useEffect, useState, type ReactNode } from 'react'
import { findMerchProduct } from '@/data/clubData'
import type { CartLine } from '../types/cart.types'
import { CartContext } from './cart-context'
import type { ProductCode, ProductSize } from '../types/merch.types'
import { normalizeCustomName, validateCustomName } from '../utils/customName'

const STORAGE_KEY = 'guateque.merch.cart'

function parseCartLine(value: unknown): CartLine | null {
  if (typeof value !== 'object' || value === null) return null
  const line = value as Partial<CartLine>
  const quantity = line.quantity
  if (
    typeof line.productCode !== 'string' ||
    typeof line.size !== 'string' ||
    typeof quantity !== 'number' ||
    !Number.isInteger(quantity) ||
    quantity <= 0 ||
    findMerchProduct(line.productCode) === undefined
  ) {
    return null
  }

  const customName = typeof line.customName === 'string' ? line.customName : null
  if (validateCustomName(customName)) return null
  return {
    productCode: line.productCode as ProductCode,
    size: line.size as ProductSize,
    customName: normalizeCustomName(customName),
    quantity,
  }
}

function readCart(): CartLine[] {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw) as unknown
    if (!Array.isArray(parsed)) return []
    return parsed.flatMap((line) => {
      const parsedLine = parseCartLine(line)
      return parsedLine ? [parsedLine] : []
    })
  } catch {
    return []
  }
}

function sameLine(line: CartLine, productCode: ProductCode, size: ProductSize, customName: string | null) {
  return line.productCode === productCode && line.size === size && line.customName === customName
}

export function CartProvider({ children }: { children: ReactNode }) {
  const [lines, setLines] = useState<CartLine[]>(readCart)

  useEffect(() => {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(lines))
  }, [lines])

  const addItem = (productCode: ProductCode, size: ProductSize, customName: string | null, quantity: number) => {
    const normalizedCustomName = normalizeCustomName(customName)
    setLines((current) => {
      const existing = current.find((line) => sameLine(line, productCode, size, normalizedCustomName))
      if (existing) {
        return current.map((line) =>
          sameLine(line, productCode, size, normalizedCustomName)
            ? { ...line, quantity: Math.min(10, line.quantity + quantity) }
            : line,
        )
      }
      return [...current, { productCode, size, customName: normalizedCustomName, quantity: Math.min(10, quantity) }]
    })
  }

  const setQuantity = (productCode: ProductCode, size: ProductSize, customName: string | null, quantity: number) => {
    if (quantity <= 0) {
      setLines((current) => current.filter((line) => !sameLine(line, productCode, size, customName)))
      return
    }
    setLines((current) =>
      current.map((line) =>
        sameLine(line, productCode, size, customName) ? { ...line, quantity: Math.min(10, quantity) } : line,
      ),
    )
  }

  const removeItem = (productCode: ProductCode, size: ProductSize, customName: string | null) => {
    setLines((current) => current.filter((line) => !sameLine(line, productCode, size, customName)))
  }

  const totalUnits = lines.reduce((total, line) => total + line.quantity, 0)
  const subtotal = lines.reduce((total, line) => {
    const product = findMerchProduct(line.productCode)
    return total + (product?.price ?? 0) * line.quantity
  }, 0)

  return (
    <CartContext.Provider
      value={{ lines, totalUnits, subtotal, addItem, setQuantity, removeItem, clear: () => setLines([]) }}
    >
      {children}
    </CartContext.Provider>
  )
}
