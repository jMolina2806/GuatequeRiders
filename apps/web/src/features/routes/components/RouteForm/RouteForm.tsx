import { useEffect, useState } from 'react'
import './RouteForm.css'
import type {
  CreateRouteFormData,
  RouteDifficulty,
  RouteStatus,
} from '../../types/route.types'

type RouteFormProps = {
  onSubmit: (data: CreateRouteFormData) => void
  initialValues?: CreateRouteFormData | null
  submitLabel?: string
}

const initialFormData: CreateRouteFormData = {
  title: '',
  description: '',
  date: '',
  time: '',
  origin: '',
  destination: '',
  distanceKm: 0,
  difficulty: 'Fácil',
  status: 'Abierta',
  maxParticipants: 10,
}

export default function RouteForm({
  onSubmit,
  initialValues = null,
  submitLabel = 'Crear ruta',
}: RouteFormProps) {
  const [formData, setFormData] = useState<CreateRouteFormData>(initialFormData)

  useEffect(() => {
    if (initialValues) {
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
          <label htmlFor="time">Hora</label>
          <input id="time" name="time" type="time" value={formData.time} onChange={handleChange} required />
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
            {(['Fácil', 'Media', 'Difícil'] as RouteDifficulty[]).map((difficulty) => (
              <option key={difficulty} value={difficulty}>
                {difficulty}
              </option>
            ))}
          </select>
        </div>

        <div className="route-form__field">
          <label htmlFor="status">Estado</label>
          <select
            id="status"
            name="status"
            value={formData.status}
            onChange={handleChange}
          >
            {(['Abierta', 'Cerrada'] as RouteStatus[]).map((status) => (
              <option key={status} value={status}>
                {status}
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

      <button className="route-form__submit" type="submit">
        {submitLabel}
      </button>
    </form>
  )
}