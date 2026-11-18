export type ProductCode = 'TSHIRT' | 'CROP_TOP' | 'HOODIE'
export type ProductSize = 'S' | 'M' | 'L' | 'XL'
export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'CANCELLED'
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'IN_PRODUCTION' | 'READY' | 'DELIVERED' | 'CANCELLED'

export type CampaignStatus = 'OPEN' | 'CLOSED' | 'ORDERED'

export type CampaignOpeningReadiness = {
  canOpen: boolean
  blockers: string[]
}

export type Campaign = {
  id: number
  status: CampaignStatus
  openedAt: string
  closedAt: string | null
  orderedAt: string | null
  acceptingOrders: boolean
  acceptingPayments: boolean
}

export type CampaignHistoryEntry = {
  campaign: Campaign
  totalOrders: number
  paidOrders: number
  pendingOrders: number
  totalUnits: number
}

export type CampaignStats = {
  totalOrders: number
  paidOrders: number
  pendingOrders: number
  failedOrders: number
  cancelledOrders: number
  paidUnits: number
}

export type CampaignDashboard = {
  campaign: Campaign
  stats: CampaignStats
  provisionalPreparation: PreparationOrders | null
  supplierOrder: SupplierOrderBatch | null
  fulfillmentOrders: Order[]
  excludedOrders: Order[]
  latePaymentOrders: Order[]
}

export type CloseCampaignResponse = {
  campaign: Campaign
  supplierOrder: SupplierOrderBatch
}

export type OrderItem = {
  id: number
  productCode: ProductCode
  productName: string
  size: ProductSize
  quantity: number
  unitPrice: number
  subtotal: number
  customName: string | null
}

export type Order = {
  id: number
  userId: number
  username: string
  campaignId: number
  status: OrderStatus
  paymentStatus: PaymentStatus
  total: number
  customerName: string
  customerEmail: string
  customerPhone: string | null
  notes: string | null
  fulfillmentMethod: 'PICKUP' | 'SHIPPING' | null
  address: string | null
  city: string | null
  postalCode: string | null
  province: string | null
  paidAt: string | null
  latePayment: boolean
  createdAt: string
  updatedAt: string
  items: OrderItem[]
}

export type PreparationSummaryLine = {
  productCode: ProductCode
  productName: string
  size: ProductSize
  quantity: number
}

export type PreparationProductSummary = {
  productCode: ProductCode
  productName: string
  quantity: number
}

export type PreparationOrders = {
  orders: Order[]
  summary: PreparationSummaryLine[]
  productSummary: PreparationProductSummary[]
  totalUnits: number
}

export type SupplierOrderItem = {
  productCode: ProductCode
  productName: string
  size: ProductSize
  quantity: number
  unitPrice: number
  subtotal: number
  customName: string | null
}

export type SupplierOrder = {
  orderId: number
  customerName: string
  orderCreatedAt: string
  total: number
  items: SupplierOrderItem[]
}

export type SupplierOrderBatch = {
  id: number
  campaignId: number
  campaignNumber: number
  closedAt: string
  totalUnits: number
  totalAmount: number
  summary: PreparationSummaryLine[]
  productSummary: PreparationProductSummary[]
  personalizations: SupplierPersonalization[]
  orders: SupplierOrder[]
}

export type SupplierPersonalization = {
  orderId: number
  productCode: ProductCode
  productName: string
  size: ProductSize
  customName: string
  quantity: number
}
