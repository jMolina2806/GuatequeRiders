import { useCallback, useEffect, useMemo, useState } from 'react'
import { getMyMemberCard, type MemberCard } from '../services/membership.service'
import './MemberCardPage.css'

export default function MemberCardPage() {
  const [card, setCard] = useState<MemberCard | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [now, setNow] = useState(Date.now())

  const loadCard = useCallback(async () => {
    try {
      const next = await getMyMemberCard()
      setCard(next)
      setError(null)
    } catch (err) {
      setCard(null)
      setError(err instanceof Error ? err.message : 'No se pudo cargar el carnet')
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    loadCard()
    const refreshId = window.setInterval(loadCard, 45_000)
    const clockId = window.setInterval(() => setNow(Date.now()), 1_000)
    return () => {
      window.clearInterval(refreshId)
      window.clearInterval(clockId)
    }
  }, [loadCard])

  const secondsLeft = useMemo(() => {
    if (!card) return 0
    return Math.max(0, Math.ceil((new Date(card.verificationExpiresAt).getTime() - now) / 1000))
  }, [card, now])

  useEffect(() => {
    if (card && secondsLeft === 0) {
      loadCard()
    }
  }, [card, secondsLeft, loadCard])

  if (isLoading) {
    return (
      <section className="member-card-page">
        <div className="member-card-page__state" role="status">
          Cargando carnet...
        </div>
      </section>
    )
  }

  if (error || !card) {
    return (
      <section className="member-card-page">
        <div className="member-card-page__state member-card-page__state--error" role="alert">
          <h1>Carnet no disponible</h1>
          <p>{error ?? 'Tu membresía no está activa.'}</p>
          <button className="btn btn--ghost" type="button" onClick={loadCard}>
            Reintentar
          </button>
        </div>
      </section>
    )
  }

  return (
    <section className="member-card-page">
      <header className="member-card-page__header">
        <p className="member-card-page__eyebrow">Guateque Riders</p>
        <h1>Mi carnet de socio</h1>
        <p>Enséñalo abierto desde esta pantalla para aplicar descuentos de colaboradores.</p>
      </header>

      <article className="member-card" aria-label="Carnet digital de socio">
        <div className="member-card__glow member-card__glow--pink" />
        <div className="member-card__glow member-card__glow--cyan" />

        <div className="member-card__brand">
          <span className="member-card__brand-name">GUATEQUE</span>
          <span className="member-card__brand-riders">RIDERS</span>
        </div>

        <div className="member-card__status">
          <span className="member-card__status-dot" />
          SOCIO ACTIVO
        </div>

        <div className="member-card__identity">
          <span className="member-card__label">Usuario</span>
          <strong>{card.username}</strong>
          <span className="member-card__label">N.º de socio</span>
          <strong>{card.memberNumber}</strong>
        </div>

        <div className="member-card__verification">
          <span className="member-card__label">Código de seguridad temporal</span>
          <strong className="member-card__code">{card.verificationCode}</strong>
          <span className="member-card__timer">Se renueva en {secondsLeft}s</span>
          <span className="member-card__live">
            <span className="member-card__live-dot" />
            CARNET EN DIRECTO
          </span>
        </div>

        <p className="member-card__security">
          El código y el contador cambian automáticamente. Muestra siempre el carnet abierto desde la web.
        </p>
      </article>
    </section>
  )
}
