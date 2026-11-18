export type CollaboratorType =
  | 'WORKSHOP'
  | 'SHOP'
  | 'BRAND'
  | 'EQUIPMENT'
  | 'PHOTOGRAPHY'
  | 'CIRCUIT'
  | 'OTHER'

export type Collaborator = {
  id: number
  name: string
  type: CollaboratorType
  shortDescription: string
  discountPercentage: number | null
  benefit: string | null
  websiteUrl: string | null
  imageUrl: string | null
  featured: boolean
  active: boolean
  createdAt: string
  updatedAt: string
}

export type CreateCollaboratorRequest = {
  name: string
  type: CollaboratorType
  shortDescription: string
  discountPercentage: number | null
  benefit: string | null
  websiteUrl: string | null
  featured: boolean
  active: boolean
}

export const COLLABORATOR_TYPE_LABELS: Record<CollaboratorType, string> = {
  WORKSHOP: 'Talleres',
  SHOP: 'Tiendas',
  BRAND: 'Marcas',
  EQUIPMENT: 'Equipamiento',
  PHOTOGRAPHY: 'Fotografía',
  CIRCUIT: 'Circuitos',
  OTHER: 'Otros',
}

export const COLLABORATOR_TYPE_FALLBACKS: Record<CollaboratorType, string> = {
  WORKSHOP: '🔧',
  SHOP: '🧤',
  BRAND: '✦',
  EQUIPMENT: '◈',
  PHOTOGRAPHY: '📸',
  CIRCUIT: '🏁',
  OTHER: '+',
}

export function getDiscountLabel(collaborator: Collaborator): string | null {
  return collaborator.discountPercentage === null
    ? null
    : `${collaborator.discountPercentage}% de descuento para miembros`
}

export function getBenefitLabel(collaborator: Collaborator): string {
  return getDiscountLabel(collaborator) ?? collaborator.benefit ?? 'Ventaja pendiente de confirmar'
}
