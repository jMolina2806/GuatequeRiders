import { useEffect, useState } from 'react'
import './EventForm.css'
import type { CreateEventRequest } from '@/features/events/types/event.types'

type EventFormProps = {
  onSubmit: (data: CreateEventRequest) => void
  onCancel?: () => void
  initialValues?: CreateEventRequest | null
  submitLabel?: string
}

const initialFormData: CreateEventRequest = {
  title: '',
  description: '',
  location: '',
  date: '',
  startTime: '',
  endTime: '',
  maxParticipants: null,
}

export default function EventForm({
  onSubmit,
  onCancel,
  initialValues = null,
  submitLabel = 'Crear evento',
}: EventFormProps) {
  const [formData, setFormData] = useState<CreateEventRequest>(initialFormData)

  useEffect(() => {
    if (initialValues) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setFormData(initialValues)
    } else {
      setFormData(initialFormData)
    }
  }, [initialValues])

  const handleChange = (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) => {
    const { name, value } = event.target

    setFormData((prev) => ({
      ...prev,
      [name]:
        name === 'maxParticipants'
          ? value === '' ? null : Number(value)
          : name === 'endTime'
            ? value === '' ? null : value
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
    <form className="event-form" onSubmit={handleSubmit}>
      <div className="event-form__grid">
        <div className="event-form__field">
          <label htmlFor="title">Título</label>
          <input id="title" name="title" value={formData.title} onChange={handleChange} required />
        </div>

        <div className="event-form__field">
          <label htmlFor="date">Fecha</label>
          <input id="date" name="date" type="date" value={formData.date} onChange={handleChange} required />
        </div>

        <div className="event-form__field">
          <label htmlFor="location">Ubicación</label>
          <input id="location" name="location" value={formData.location} onChange={handleChange} required />
        </div>

        <div className="event-form__field">
          <label htmlFor="startTime">Hora de inicio</label>
          <input id="startTime" name="startTime" type="time" value={formData.startTime} onChange={handleChange} required />
        </div>

        <div className="event-form__field">
          <label htmlFor="endTime">Hora de fin</label>
          <input id="endTime" name="endTime" type="time" value={formData.endTime ?? ''} onChange={handleChange} />
        </div>

        <div className="event-form__field">
          <label htmlFor="maxParticipants">Plazas máximas</label>
          <input
            id="maxParticipants"
            name="maxParticipants"
            type="number"
            min="1"
            value={formData.maxParticipants ?? ''}
            onChange={handleChange}
            placeholder="Sin límite"
          />
        </div>
      </div>

      <div className="event-form__field">
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

      <div className="event-form__actions">
        <button className="event-form__submit" type="submit">
          {submitLabel}
        </button>

        {onCancel && (
          <button className="event-form__cancel" onClick={onCancel} type="button">
            Cancelar
          </button>
        )}
      </div>
    </form>
  )
}
