const CART_ORDER_KEY = 'guateque.merch.cart-order-id'

export function markCartOrder(orderId: number) {
  window.localStorage.setItem(CART_ORDER_KEY, String(orderId))
}

export function consumeCartOrder(orderId: number): boolean {
  const storedOrderId = window.localStorage.getItem(CART_ORDER_KEY)
  if (storedOrderId !== String(orderId)) {
    return false
  }
  window.localStorage.removeItem(CART_ORDER_KEY)
  return true
}
