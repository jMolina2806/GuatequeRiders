import { useState } from 'react'
import type { MerchProduct } from '@/data/clubData'
import { useCart } from '../../context/useCart'
import type { ProductSize } from '../../types/merch.types'
import { normalizeCustomName, validateCustomName } from '../../utils/customName'
import './MerchProductCard.css'

type MerchProductCardProps = {
  product: MerchProduct
  isOpen: boolean
}

const euro = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })

export default function MerchProductCard({ product, isOpen }: MerchProductCardProps) {
  const { addItem } = useCart()
  const [selectedSize, setSelectedSize] = useState<ProductSize | ''>('')
  const [quantity, setQuantity] = useState(1)
  const [customName, setCustomName] = useState('')
  const [feedback, setFeedback] = useState<string | null>(null)

  const handleAdd = () => {
    if (!isOpen) return
    if (!selectedSize) {
      setFeedback('Selecciona una talla antes de añadir el producto.')
      return
    }
    const customNameError = validateCustomName(customName)
    if (customNameError) {
      setFeedback(customNameError)
      return
    }
    addItem(product.code, selectedSize, normalizeCustomName(customName), quantity)
    setFeedback('Añadido al carrito')
  }

  return (
    <article className={`merch-product-card merch-product-card--${product.id}`}>
      <div className="merch-product-card__visual">
        <img src={product.image} alt={product.name} className="merch-product-card__image" />
      </div>
      <div className="merch-product-card__body">
        <span className="merch-product-card__category">Guateque Riders / Oficial</span>
        <h2 className="merch-product-card__title">{product.name}</h2>
        <p className="merch-product-card__description">{product.description}</p>
        <strong className="merch-product-card__price">{euro.format(product.price)}</strong>

        <fieldset className="merch-product-card__sizes">
          <legend>Talla</legend>
          <div className="merch-product-card__size-list">
            {product.sizes.map((size) => (
              <button
                className={`merch-product-card__size ${selectedSize === size ? 'is-selected' : ''}`}
                key={size}
                type="button"
                aria-pressed={selectedSize === size}
                onClick={() => {
                  setSelectedSize(size)
                  setFeedback(null)
                }}
              >
                {size}
              </button>
            ))}
          </div>
        </fieldset>

        <label className="merch-product-card__custom-name">
          <span>Nombre personalizado (opcional)</span>
          <input
            type="text"
            value={customName}
            maxLength={20}
            autoComplete="off"
            onChange={(event) => {
              setCustomName(event.target.value)
              setFeedback(null)
            }}
          />
          <small>Déjalo vacío si no quieres personalización.</small>
        </label>

        <div className="merch-product-card__purchase-row">
          <div className="merch-product-card__quantity" aria-label={`Cantidad de ${product.name}`}>
            <button
              type="button"
              aria-label="Disminuir cantidad"
              onClick={() => setQuantity((value) => Math.max(1, value - 1))}
            >
              −
            </button>
            <span aria-live="polite">{quantity}</span>
            <button
              type="button"
              aria-label="Aumentar cantidad"
              onClick={() => setQuantity((value) => Math.min(10, value + 1))}
            >
              +
            </button>
          </div>
          <button
            className="btn btn--primary merch-product-card__add"
            type="button"
            onClick={handleAdd}
            disabled={!isOpen}
          >
            {isOpen ? 'Añadir al carrito' : 'Compras temporalmente cerradas'}
          </button>
        </div>
        {feedback && (
          <p className={`merch-product-card__feedback ${feedback.startsWith('Añadido') ? 'is-success' : ''}`} role="status">
            {feedback}
          </p>
        )}
      </div>
    </article>
  )
}
