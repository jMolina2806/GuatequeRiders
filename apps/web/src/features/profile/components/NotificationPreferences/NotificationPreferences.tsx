import { useEffect, useState } from 'react'
import {
  getNotificationPreferences,
  updateNotificationPreferences,
} from '../../services/profile.service'
import type { NotificationPreferences as NotificationPreferencesType } from '../../types/profile.types'
import './NotificationPreferences.css'

const labels: Array<{
  key: keyof NotificationPreferencesType
  title: string
  description: string
}> = [
  {
    key: 'merchNotifications',
    title: 'Merch',
    description: 'Nuevas campañas y avisos generales de Merch.',
  },
  {
    key: 'routeNotifications',
    title: 'Rutas',
    description: 'Nuevas rutas publicadas por Guateque Riders.',
  },
  {
    key: 'eventNotifications',
    title: 'Eventos',
    description: 'Nuevos eventos publicados.',
  },
  {
    key: 'circuitNotifications',
    title: 'Circuitos',
    description: 'Nuevas actividades y novedades de circuito.',
  },
]

export default function NotificationPreferences() {
  const [preferences, setPreferences] = useState<NotificationPreferencesType | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState<string | null>(null)

  useEffect(() => {
    let active = true

    getNotificationPreferences()
      .then((data) => {
        if (active) setPreferences(data)
      })
      .catch(() => {
        if (active) setFeedback('No se pudieron cargar tus preferencias de email.')
      })
      .finally(() => {
        if (active) setIsLoading(false)
      })

    return () => {
      active = false
    }
  }, [])

  const save = async (next: NotificationPreferencesType, message: string) => {
    setIsSaving(true)
    setFeedback(null)
    try {
      const updated = await updateNotificationPreferences(next)
      setPreferences(updated)
      setFeedback(message)
    } catch {
      setFeedback('No se pudieron guardar tus preferencias.')
    } finally {
      setIsSaving(false)
    }
  }

  const toggle = (key: keyof NotificationPreferencesType) => {
    if (!preferences || isSaving) return
    const next = { ...preferences, [key]: !preferences[key] }
    void save(next, 'Preferencias actualizadas.')
  }

  const disableAll = () => {
    if (!preferences || isSaving) return
    void save(
      {
        merchNotifications: false,
        routeNotifications: false,
        eventNotifications: false,
        circuitNotifications: false,
      },
      'Has desactivado todos los avisos generales.',
    )
  }

  if (isLoading) {
    return (
      <section className="notification-preferences" id="notifications">
        <p className="notification-preferences__loading">Cargando preferencias de email...</p>
      </section>
    )
  }

  if (!preferences) {
    return (
      <section className="notification-preferences" id="notifications">
        <p className="notification-preferences__error">
          {feedback ?? 'No se pudieron cargar tus preferencias.'}
        </p>
      </section>
    )
  }

  const hasAnyEnabled = Object.values(preferences).some(Boolean)

  return (
    <section className="notification-preferences" id="notifications">
      <div className="notification-preferences__heading">
        <div>
          <span className="notification-preferences__eyebrow">Email</span>
          <h2>Notificaciones por email</h2>
          <p>
            Elige qué avisos generales quieres recibir. Los emails necesarios sobre tus
            pagos, pedidos o inscripciones seguirán llegando.
          </p>
        </div>
      </div>

      <div className="notification-preferences__list">
        {labels.map((item) => (
          <label className="notification-preferences__item" key={item.key}>
            <span>
              <strong>{item.title}</strong>
              <small>{item.description}</small>
            </span>
            <input
              type="checkbox"
              checked={preferences[item.key]}
              onChange={() => toggle(item.key)}
              disabled={isSaving}
              aria-label={`Notificaciones de ${item.title}`}
            />
          </label>
        ))}
      </div>

      <div className="notification-preferences__footer">
        <span className="notification-preferences__feedback" aria-live="polite">
          {feedback}
        </span>
        <button
          type="button"
          className="btn btn--outline-danger btn--sm"
          onClick={disableAll}
          disabled={isSaving || !hasAnyEnabled}
        >
          {isSaving ? 'Guardando...' : 'Desactivar todos los avisos'}
        </button>
      </div>
    </section>
  )
}
