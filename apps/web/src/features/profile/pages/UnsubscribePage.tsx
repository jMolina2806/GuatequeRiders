import { useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { unsubscribeFromCategory } from '../services/profile.service'
import type { NotificationCategory } from '../types/profile.types'
import './UnsubscribePage.css'

const categoryLabels: Record<NotificationCategory, string> = {
  MERCH: 'Merch',
  ROUTES: 'Rutas',
  EVENTS: 'Eventos',
  CIRCUITS: 'Circuitos',
}

function isCategory(value: string | null): value is NotificationCategory {
  return value !== null && value in categoryLabels
}

export default function UnsubscribePage() {
  const [searchParams] = useSearchParams()
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDone, setIsDone] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const token = searchParams.get('token')
  const rawCategory = searchParams.get('category')
  const category = useMemo(
    () => (isCategory(rawCategory) ? rawCategory : null),
    [rawCategory],
  )

  const unsubscribe = async () => {
    if (!token || !category || isSubmitting) return
    setIsSubmitting(true)
    setError(null)

    try {
      await unsubscribeFromCategory(token, category)
      setIsDone(true)
    } catch {
      setError('El enlace no es válido o ya no puede utilizarse.')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (!token || !category) {
    return (
      <section className="unsubscribe-page">
        <div className="unsubscribe-page__card">
          <span className="unsubscribe-page__eyebrow">Notificaciones</span>
          <h1>Enlace de baja no válido</h1>
          <p>No hemos podido identificar qué avisos quieres desactivar.</p>
          <Link to="/" className="btn btn--secondary">Volver al inicio</Link>
        </div>
      </section>
    )
  }

  if (isDone) {
    return (
      <section className="unsubscribe-page">
        <div className="unsubscribe-page__card">
          <span className="unsubscribe-page__eyebrow">Notificaciones</span>
          <h1>Preferencia actualizada</h1>
          <p>
            Ya no recibirás avisos generales de <strong>{categoryLabels[category]}</strong>.
            Los correos necesarios sobre pedidos, pagos o inscripciones no se ven afectados.
          </p>
          <div className="unsubscribe-page__actions">
            <Link to="/profile#notifications" className="btn btn--secondary">
              Gestionar preferencias
            </Link>
            <Link to="/" className="btn btn--ghost">Volver al inicio</Link>
          </div>
        </div>
      </section>
    )
  }

  return (
    <section className="unsubscribe-page">
      <div className="unsubscribe-page__card">
        <span className="unsubscribe-page__eyebrow">Notificaciones</span>
        <h1>¿Dejar de recibir avisos de {categoryLabels[category]}?</h1>
        <p>
          Desactivaremos únicamente los avisos generales de esta categoría.
          Podrás volver a activarlos desde tu perfil cuando quieras.
        </p>

        {error && <p className="unsubscribe-page__error" role="alert">{error}</p>}

        <div className="unsubscribe-page__actions">
          <button
            type="button"
            className="btn btn--outline-danger"
            onClick={unsubscribe}
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Desactivando...' : 'Darme de baja de estos avisos'}
          </button>
          <Link to="/" className="btn btn--ghost">Cancelar</Link>
        </div>
      </div>
    </section>
  )
}
