import type { ProductCode, ProductSize } from './merch.types'

export type CartLine = {
  productCode: ProductCode
  size: ProductSize
  customName: string | null
  quantity: number
}
