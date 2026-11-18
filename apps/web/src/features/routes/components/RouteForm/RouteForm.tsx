import { useEffect, useState } from 'react'
import './RouteForm.css'
import type {
  CreateRouteRequest,
  Difficulty,
} from '@/features/routes/types/route.types'
import type { User } from '@/features/auth/types/auth.types'

type RouteFormProps = {
  onSubmit: (data: CreateRouteRequest) => void
  onCancel?: () => void
  initialValues?: CreateRouteRequest | null
  submitLabel?: string
  availableUsers?: User[]
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
  meetingTime: '',
  departureTime: '',
  origin: '',
  destination: '',
  distanceKm: 0,
  difficulty: 'EASY',
  maxParticipants: 10,
  organizerIds: [],
}

export default function RouteForm({
  onSubmit,
  onCancel,
  initialValues = null,
  submitLabel = 'Crear ruta',
  availableUsers = [],
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

  const toggleOrganizer = (userId: number) => {
    setFormData((prev) => ({
      ...prev,
      organizerIds: prev.organizerIds.includes(userId)
        ? prev.organizerIds.filter((id) => id !== userId)
        : [...prev.organizerIds, userId],
    }))
  }

  const timesInvalid =
    Boolean(formData.meetingTime && formData.departureTime) &&
    formData.departureTime <= formData.meetingTime

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

        <div className="route-form__date-group">
          <div className="route-form__field">
            <label htmlFor="date">Fecha</label>
            <input id="date" name="date" type="date" value={formData.date} onChange={handleChange} required />
          </div>

          <div className="route-form__field">
            <label htmlFor="meetingTime">Hora de quedada</label>
            <input
              id="meetingTime"
              name="meetingTime"
              type="time"
              value={formData.meetingTime}
              onChange={handleChange}
              required
            />
          </div>

          <div className="route-form__field">
            <label htmlFor="departureTime">Hora de salida</label>
            <input
              id="departureTime"
              name="departureTime"
              type="time"
              value={formData.departureTime}
              onChange={handleChange}
              required
            />
          </div>

          {timesInvalid && (
            <p className="route-form__field-error" role="alert">
              La hora de salida debe ser posterior a la de quedada.
            </p>
          )}
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

      <fieldset className="route-form__organizers">
        <legend>Organizadores</legend>
        <p className="route-form__organizers-hint">
          Selecciona al menos un usuario que organicen la ruta.
        </p>
        {availableUsers.length === 0 ? (
          <p className="route-form__organizers-empty">
            No hay administradores disponibles para asignar como organizadores. La
            ruta no se puede guardar sin al menos un organizador.
          </p>
        ) : (
          <div className="route-form__organizers-list">
            {availableUsers.map((user) => {
              const checked = formData.organizerIds.includes(user.id)
              return (
                <label
                  key={user.id}
                  className={`route-form__organizer ${checked ? 'route-form__organizer--selected' : ''}`}
                >
                  <input
                    type="checkbox"
                    checked={checked}
                    onChange={() => toggleOrganizer(user.id)}
                  />
                  <span className="route-form__organizer-name">{user.username}</span>
                  {user.email && (
                    <span className="route-form__organizer-email">{user.email}</span>
                  )}
                </label>
              )
            })}
          </div>
        )}
      </fieldset>

      <div className="route-form__actions">
        <button
          className="btn btn--primary btn--lg"
          type="submit"
          disabled={formData.organizerIds.length === 0 || timesInvalid}
        >
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
