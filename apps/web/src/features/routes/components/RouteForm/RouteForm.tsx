import { useEffect, useState } from 'react'
import './RouteForm.css'
import type {
  CreateRouteRequest,
  Difficulty,
} from '@/features/routes/types/route.types'

type RouteFormProps = {
  onSubmit: (data: CreateRouteRequest) => void
  onCancel?: () => void
  initialValues?: CreateRouteRequest | null
  submitLabel?: string
}

const DIFFICULTY_OPTIONS: { value: Difficulty; label: string }[] = [
  { value: 'EASY', label: 'Fácil' },
  { value: 'MODERATE', label: 'Media' },
  { value: 'HARD', label: 'Difícil' },
  { value: 'EXTREME', label: 'Extrema' },
]

const initialFormData: CreateRouteRequest = {
  title: '',
  description: '',
  date: '',
  origin: '',
  destination: '',
  distanceKm: 0,
  difficulty: 'EASY',
  maxParticipants: 10,
}

export default function RouteForm({
  onSubmit,
  onCancel,
  initialValues = null,
  submitLabel = 'Crear ruta',
}: RouteFormProps) {
  const [formData, setFormData] = useState<CreateRouteRequest>(initialFormData)

  useEffect(() => {
    if (initialValues) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setFormData(initialValues)
    } else {
      setFormData(initialFormData)
    }
  }, [initialValues])

  const handleChange = (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>,
  ) => {
    const { name, value } = event.target

    setFormData((prev) => ({
      ...prev,
      [name]:
        name === 'distanceKm' || name === 'maxParticipants'
          ? Number(value)
          : value,
    }))
  }

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    onSubmit(formData)

    if (!initialValues) {
      setFormData(initialFormData)
    }
  }

  return (
    <form className="route-form" onSubmit={handleSubmit}>
      <div className="route-form__grid">
        <div className="route-form__field">
          <label htmlFor="title">Título</label>
          <input id="title" name="title" value={formData.title} onChange={handleChange} required />
        </div>

        <div className="route-form__field">
          <label htmlFor="date">Fecha</label>
          <input id="date" name="date" type="date" value={formData.date} onChange={handleChange} required />
        </div>

        <div className="route-form__field">
          <label htmlFor="origin">Origen</label>
          <input id="origin" name="origin" value={formData.origin} onChange={handleChange} required />
        </div>

        <div className="route-form__field">
          <label htmlFor="destination">Destino</label>
          <input id="destination" name="destination" value={formData.destination} onChange={handleChange} required />
        </div>

        <div className="route-form__field">
          <label htmlFor="distanceKm">Distancia (km)</label>
          <input
            id="distanceKm"
            name="distanceKm"
            type="number"
            min="0"
            value={formData.distanceKm}
            onChange={handleChange}
            required
          />
        </div>

        <div className="route-form__field">
          <label htmlFor="difficulty">Dificultad</label>
          <select
            id="difficulty"
            name="difficulty"
            value={formData.difficulty}
            onChange={handleChange}
          >
            {DIFFICULTY_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        <div className="route-form__field">
          <label htmlFor="maxParticipants">Plazas máximas</label>
          <input
            id="maxParticipants"
            name="maxParticipants"
            type="number"
            min="1"
            value={formData.maxParticipants}
            onChange={handleChange}
            required
          />
        </div>
      </div>

      <div className="route-form__field">
        <label htmlFor="description">Descripción</label>
        <textarea
          id="description"
          name="description"
          rows={4}
          value={formData.description}
          onChange={handleChange}
          required
        />
      </div>

      <div className="route-form__actions">
        <button className="route-form__submit" type="submit">
          {submitLabel}
        </button>

        {onCancel && (
          <button className="route-form__cancel" onClick={onCancel} type="button">
            Cancelar
          </button>
        )}
      </div>
    </form>
  )
}
