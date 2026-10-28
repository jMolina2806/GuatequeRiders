import camisetaImage from '@/assets/merch/camiseta-guateque-riders.png'
import cropTopImage from '@/assets/merch/crop-top-guateque-riders.png'
import sudaderaImage from '@/assets/merch/sudadera-guateque-riders.png'
import type { ProductCode, ProductSize } from '@/features/merch/types/merch.types'

export type MerchProduct = {
  id: string
  code: ProductCode
  name: string
  description: string
  image: string
  price: number
  sizes: ProductSize[]
}

export const merchProducts: MerchProduct[] = [
  {
    id: 'camiseta',
    code: 'TSHIRT',
    name: 'Camiseta Guateque Riders',
    description: 'Diseño oficial Guateque Riders con identidad neon de la comunidad.',
    image: camisetaImage,
    price: 20,
    sizes: ['S', 'M', 'L', 'XL'],
  },
  {
    id: 'crop-top',
    code: 'CROP_TOP',
    name: 'Crop Top Guateque Riders',
    description: 'Una silueta más atrevida con el universo neón del club como protagonista.',
    image: cropTopImage,
    price: 12,
    sizes: ['S', 'M', 'L', 'XL'],
  },
  {
    id: 'sudadera',
    code: 'HOODIE',
    name: 'Sudadera Guateque Riders',
    description: 'Diseño oficial Guateque Riders con detalles neon en frontal, espalda y mangas.',
    image: sudaderaImage,
    price: 30,
    sizes: ['S', 'M', 'L', 'XL'],
  },
]

export function findMerchProduct(code: string) {
  return merchProducts.find((product) => product.code === code)
}
