import { useEffect, useState } from 'react'
import type { CreateCircuitRequest } from '@/features/circuits/types/circuit.types'
import './CircuitForm.css'

type CircuitFormProps = {
  onSubmit: (data: CreateCircuitRequest) => void
  onCancel?: () => void
  initialValues?: CreateCircuitRequest | null
  submitLabel?: string
}

const initialFormData: CreateCircuitRequest = {
  title: '',
  circuitName: '',
  description: '',
  location: '',
  date: '',
  startTime: '',
  endTime: null,
  price: null,
  discount: null,
  benefit: null,
  registrationUrl: '',
  informationUrl: null,
  imageUrl: null,
}

const isHttpsUrl = (value: string) => /^https:\/\/\S+$/.test(value)

export default function CircuitForm({
  onSubmit,
  onCancel,
  initialValues = null,
  submitLabel = 'Crear actividad',
}: CircuitFormProps) {
  const [formData, setFormData] = useState<CreateCircuitRequest>(initialFormData)

  useEffect(() => {
    if (initialValues) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setFormData(initialValues)
    } else {
      setFormData(initialFormData)
    }
  }, [initialValues])

  const handleChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = event.target
    setFormData((previous) => ({
      ...previous,
      [name]: name === 'price'
        ? value === '' ? null : Number(value)
        : ['endTime', 'discount', 'benefit', 'informationUrl', 'imageUrl'].includes(name)
          ? value === '' ? null : value
          : value,
    }))
  }

  const timesInvalid = Boolean(formData.endTime && formData.startTime)
    && formData.endTime! <= formData.startTime
  const registrationUrlInvalid = formData.registrationUrl !== '' && !isHttpsUrl(formData.registrationUrl)
  const informationUrlInvalid = Boolean(formData.informationUrl) && !isHttpsUrl(formData.informationUrl!)
  const imageUrlInvalid = Boolean(formData.imageUrl) && !isHttpsUrl(formData.imageUrl!)
  const hasUrlError = registrationUrlInvalid || informationUrlInvalid || imageUrlInvalid

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (timesInvalid || hasUrlError) return
    onSubmit({
      ...formData,
      endTime: formData.endTime || null,
      discount: formData.discount || null,
      benefit: formData.benefit || null,
      informationUrl: formData.informationUrl || null,
      imageUrl: formData.imageUrl || null,
    })

    if (!initialValues) setFormData(initialFormData)
  }

  return (
    <form className="circuit-form" onSubmit={handleSubmit}>
      <div className="circuit-form__grid">
        <div className="circuit-form__field">
          <label htmlFor="title">Nombre de la actividad</label>
          <input id="title" name="title" value={formData.title} onChange={handleChange} required />
        </div>

        <div className="circuit-form__field">
          <label htmlFor="circuitName">Circuito</label>
          <input id="circuitName" name="circuitName" value={formData.circuitName} onChange={handleChange} required />
        </div>

        <div className="circuit-form__field">
          <label htmlFor="location">Ubicación</label>
          <input id="location" name="location" value={formData.location} onChange={handleChange} required />
        </div>

        <div className="circuit-form__field">
          <label htmlFor="date">Fecha</label>
          <input id="date" name="date" type="date" value={formData.date} onChange={handleChange} required />
        </div>

        <div className="circuit-form__field">
          <label htmlFor="startTime">Hora inicio</label>
          <input id="startTime" name="startTime" type="time" value={formData.startTime} onChange={handleChange} required />
        </div>

        <div className="circuit-form__field">
          <label htmlFor="endTime">Hora fin</label>
          <input id="endTime" name="endTime" type="time" value={formData.endTime ?? ''} onChange={handleChange} />
          {timesInvalid && <span className="circuit-form__error">Debe ser posterior a la hora de inicio.</span>}
        </div>

        <div className="circuit-form__field">
          <label htmlFor="price">Precio</label>
          <input id="price" name="price" type="number" min="0" step="0.01" value={formData.price ?? ''} onChange={handleChange} placeholder="Opcional" />
        </div>

        <div className="circuit-form__field">
          <label htmlFor="discount">Descuento Guateque Riders</label>
          <input id="discount" name="discount" value={formData.discount ?? ''} onChange={handleChange} placeholder="Pendiente de confirmar" />
        </div>
      </div>

      <div className="circuit-form__field">
        <label htmlFor="description">Descripción</label>
        <textarea id="description" name="description" rows={4} value={formData.description} onChange={handleChange} required />
      </div>

      <div className="circuit-form__field">
        <label htmlFor="benefit">Beneficio</label>
        <textarea id="benefit" name="benefit" rows={2} value={formData.benefit ?? ''} onChange={handleChange} placeholder="Ventaja pendiente de confirmar" />
      </div>

      <div className="circuit-form__grid">
        <div className="circuit-form__field">
          <label htmlFor="registrationUrl">URL de inscripción</label>
          <input
            id="registrationUrl"
            name="registrationUrl"
            type="url"
            value={formData.registrationUrl}
            onChange={handleChange}
            placeholder="https://..."
            pattern="https://.*"
            required
            aria-invalid={registrationUrlInvalid}
          />
          {registrationUrlInvalid && <span className="circuit-form__error">Debe ser una URL HTTPS válida.</span>}
        </div>

        <div className="circuit-form__field">
          <label htmlFor="informationUrl">URL de información</label>
          <input
            id="informationUrl"
            name="informationUrl"
            type="url"
            value={formData.informationUrl ?? ''}
            onChange={handleChange}
            placeholder="Opcional · https://..."
            pattern="https://.*"
            aria-invalid={informationUrlInvalid}
          />
          {informationUrlInvalid && <span className="circuit-form__error">Debe ser una URL HTTPS válida.</span>}
        </div>

        <div className="circuit-form__field">
          <label htmlFor="imageUrl">Imagen opcional</label>
          <input
            id="imageUrl"
            name="imageUrl"
            type="url"
            value={formData.imageUrl ?? ''}
            onChange={handleChange}
            placeholder="Opcional · https://..."
            pattern="https://.*"
            aria-invalid={imageUrlInvalid}
          />
          {imageUrlInvalid && <span className="circuit-form__error">Debe ser una URL HTTPS válida.</span>}
        </div>
      </div>

      <div className="circuit-form__actions">
        <button className="btn btn--primary btn--lg" type="submit" disabled={timesInvalid || hasUrlError}>
          {submitLabel}
        </button>
        {onCancel && (
          <button className="btn btn--ghost" onClick={onCancel} type="button">
            Cancelar
          </button>
        )}
      </div>
    </form>
  )
}
