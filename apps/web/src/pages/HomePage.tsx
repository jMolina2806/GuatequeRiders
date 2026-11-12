import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import logoSrc from '@/assets/branding/logo-neon.jpeg'
import heroSrc from '@/assets/hero.png'
import { useAuth } from '@/features/auth/context/useAuth'
import StatusBadge from '@/components/StatusBadge'
import { useCollaborators } from '@/features/collaborators/hooks/useCollaborators'
import {
  COLLABORATOR_TYPE_FALLBACKS,
  COLLABORATOR_TYPE_LABELS,
  getBenefitLabel,
} from '@/features/collaborators/types/collaborator.types'
import { useCircuits } from '@/features/circuits/hooks/useCircuits'
import type { Event } from '@/features/events/types/event.types'
import * as eventsService from '@/features/events/services/events.service'
import type { Route } from '@/features/routes/types/route.types'
import * as routesService from '@/features/routes/services/routes.service'
import { merchProducts } from '@/data/clubData'
import './HomePage.css'

const DIFFICULTY_LABELS: Record<string, string> = {
  EASY: 'Fácil',
  MODERATE: 'Media',
  HARD: 'Difícil',
  EXTREME: 'Extrema',
}

const QUICK_LINKS = [
  { href: '/routes', code: '01', title: 'Rutas', description: 'Salidas y carreteras compartidas', icon: '↗' },
  { href: '/events', code: '02', title: 'Eventos', description: 'Quedadas fuera del manillar', icon: '✦' },
  { href: '/circuits', code: '03', title: 'Circuitos', description: 'Paddock, tandas y asfalto', icon: '◌' },
  { href: '/collaborators', code: '04', title: 'Colaboradores', description: 'La red que nos acompaña', icon: '+' },
]

const formatDate = (date: string) => {
  const parsedDate = new Date(`${date}T00:00:00`)
  if (Number.isNaN(parsedDate.getTime())) return date
  return new Intl.DateTimeFormat('es-ES', {
    day: '2-digit',
    month: 'short',
  }).format(parsedDate)
}

const today = () => new Date().toISOString().slice(0, 10)

export default function HomePage() {
  const { isAdmin, isMember } = useAuth()
  const [routes, setRoutes] = useState<Route[]>([])
  const [events, setEvents] = useState<Event[]>([])
  const [contentLoading, setContentLoading] = useState(true)
  const [contentError, setContentError] = useState<string | null>(null)
  const {
    circuits,
    isLoading: circuitsLoading,
    error: circuitsError,
  } = useCircuits({ publicOnly: true })
  const {
    collaborators,
    isLoading: collaboratorsLoading,
    error: collaboratorsError,
  } = useCollaborators({ publicOnly: true })

  useEffect(() => {
    let cancelled = false

    Promise.allSettled([routesService.getPublicRoutes(), eventsService.getPublicEvents()])
      .then(([routeResult, eventResult]) => {
        if (cancelled) return
        if (routeResult.status === 'fulfilled') setRoutes(routeResult.value)
        if (eventResult.status === 'fulfilled') setEvents(eventResult.value)

        const failedRequest = [routeResult, eventResult].find((result) => result.status === 'rejected')
        if (failedRequest?.status === 'rejected') {
          setContentError(
            failedRequest.reason instanceof Error
              ? failedRequest.reason.message
              : 'No se pudo cargar el contenido',
          )
        }
      })
      .finally(() => {
        if (!cancelled) setContentLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const publishedRoutes = useMemo(
    () =>
      routes
        .filter((route) => route.status === 'PUBLISHED' && route.date >= today())
        .sort((first, second) => first.date.localeCompare(second.date))
        .slice(0, 3),
    [routes],
  )

  const publishedEvents = useMemo(
    () =>
      events
        .filter((event) => event.status === 'PUBLISHED' && event.date >= today())
        .sort((first, second) => first.date.localeCompare(second.date))
        .slice(0, 3),
    [events],
  )

  const publishedCircuits = useMemo(
    () =>
      circuits
        .filter((circuit) => circuit.status === 'PUBLISHED' && circuit.date >= today())
        .sort((first, second) => first.date.localeCompare(second.date))
        .slice(0, 2),
    [circuits],
  )

  const isLoading = contentLoading || circuitsLoading || collaboratorsLoading
  const publishedRouteCount = routes.filter((route) => route.status === 'PUBLISHED').length
  const publishedEventCount = events.filter((event) => event.status === 'PUBLISHED').length
  const publishedCircuitCount = circuits.filter((circuit) => circuit.status === 'PUBLISHED').length
  const featuredCollaborators = useMemo(
    () => collaborators.filter((collaborator) => collaborator.active && collaborator.featured).slice(0, 3),
    [collaborators],
  )
  const homeError = contentError ?? circuitsError ?? collaboratorsError

  return (
    <div className="home-dashboard">
      <section className="home-hero">
        <div className="home-hero__content">
          <span className="home-eyebrow">Guateque Riders · Madrid · {new Date().getFullYear()}</span>
          <h1>La carretera nos une.<span>Lo demás es Guateque.</span></h1>
          <p>
            Rutas, quedadas y kilómetros compartidos con quienes viven las motos como tú.
            Tu próxima salida empieza aquí.
          </p>
          <div className="home-hero__actions">
            <Link to="/routes" className="btn btn--primary">
              Ver próximas rutas
            </Link>
            <Link to="/register" className="btn btn--ghost">
              Crear cuenta
            </Link>
          </div>
          <div className="home-hero__signal">
            <span className="home-hero__signal-dot" />
            <span>Comunidad activa</span>
            <span className="home-hero__signal-line" />
            <span>Salidas abiertas</span>
          </div>
        </div>

        <div className="home-hero__visual" aria-hidden="true">
          <span className="home-hero__visual-code">GUATEQUE / RIDERS</span>
          <img className="home-hero__photo" src={heroSrc} alt="" />
          <img className="home-hero__brand" src={logoSrc} alt="" />
          <span className="home-hero__visual-label">RIDE · MEET · REPEAT</span>
          <div className="home-hero__visual-ring" />
        </div>
      </section>

      <section className="home-quick-grid" aria-label="Accesos rápidos">
        {QUICK_LINKS.map((link) => (
          <Link key={link.href} to={link.href} className="home-quick-card">
            <span className="home-quick-card__code">{link.code}</span>
            <span className="home-quick-card__icon" aria-hidden="true">
              {link.icon}
            </span>
            <span className="home-quick-card__title">{link.title}</span>
            <span className="home-quick-card__description">{link.description}</span>
          </Link>
        ))}
      </section>

      <section className="home-metrics" aria-label="Actividad del club">
        <div>
          <strong>{String(publishedRouteCount).padStart(2, '0')}</strong>
          <span>rutas publicadas</span>
        </div>
        <div>
          <strong>{String(publishedEventCount).padStart(2, '0')}</strong>
          <span>eventos abiertos</span>
        </div>
        <div>
          <strong>{String(publishedCircuitCount).padStart(2, '0')}</strong>
          <span>actividades de circuito</span>
        </div>
        <div>
          <strong>{String(collaborators.filter((collaborator) => collaborator.active).length).padStart(2, '0')}</strong>
          <span>colaboradores activos</span>
        </div>
      </section>

      <section className="home-section" id="routes-preview">
        <div className="home-section__heading">
          <div>
            <span className="home-eyebrow">Agenda abierta</span>
            <h2>Próximas rutas</h2>
            <p>Salidas publicadas para los próximos días.</p>
          </div>
          <Link to="/routes" className="text-link">
            Ver todas <span aria-hidden="true">↗</span>
          </Link>
        </div>

        {isLoading ? (
          <div className="home-state">Cargando agenda...</div>
        ) : publishedRoutes.length > 0 ? (
          <div className="home-route-grid">
            {publishedRoutes.map((route, index) => (
              <article key={route.id} className="home-route-card">
                <div className="home-card__topline">
                  <span className="home-card__index">0{index + 1}</span>
                  <StatusBadge status={route.status} />
                </div>
                <h3>{route.title}</h3>
                <p className="home-route-card__route">
                  {route.origin} <span aria-hidden="true">→</span> {route.destination}
                </p>
                <p className="home-route-card__description">{route.description}</p>
                <div className="home-card__footer">
                  <span>{formatDate(route.date)}</span>
                  <span>{route.distanceKm} km</span>
                  <span>{DIFFICULTY_LABELS[route.difficulty] ?? route.difficulty}</span>
                </div>
                <Link to="/routes" className="home-route-card__view" aria-label={`Ver detalles de la ruta ${route.title}`}>
                  Descubrir ruta <span aria-hidden="true">↗</span>
                </Link>
              </article>
            ))}
          </div>
        ) : (
          <div className="home-state">No hay rutas publicadas ahora mismo.</div>
        )}
      </section>

      <section className="home-section home-section--surface">
        <div className="home-section__heading">
          <div>
            <span className="home-eyebrow">Fuera de ruta</span>
            <h2>Eventos próximos</h2>
            <p>Quedadas y encuentros abiertos a la comunidad.</p>
          </div>
          <Link to="/events" className="text-link">
            Ver todos <span aria-hidden="true">↗</span>
          </Link>
        </div>

        {isLoading ? (
          <div className="home-state">Cargando agenda...</div>
        ) : publishedEvents.length > 0 ? (
          <div className="home-event-grid">
            {publishedEvents.map((event, index) => (
              <article key={event.id} className="home-event-card">
                <div className="home-card__topline">
                  <span className="home-card__index">EV / 0{index + 1}</span>
                  <StatusBadge status={event.status} />
                </div>
                <h3>{event.title}</h3>
                <div className="home-event-card__meta">
                  <span>{formatDate(event.date)}</span>
                  <span>{event.startTime}{event.endTime ? ` - ${event.endTime}` : ''}</span>
                  <span>{event.location}</span>
                </div>
                <p>{event.description}</p>
              </article>
            ))}
          </div>
        ) : (
          <div className="home-state">No hay eventos publicados ahora mismo.</div>
        )}
      </section>

      <section className="home-section home-section--circuit">
        <div className="home-section__heading">
          <div>
            <span className="home-eyebrow">Paddock / asfalto</span>
            <h2>Circuitos</h2>
            <p>Actividades publicadas para cambiar la carretera por el trazado.</p>
          </div>
          <Link to="/circuits" className="text-link">
            Ver circuitos <span aria-hidden="true">↗</span>
          </Link>
        </div>

        {isLoading ? (
          <div className="home-state">Cargando circuitos...</div>
        ) : publishedCircuits.length > 0 ? (
          <div className="home-circuit-grid">
            {publishedCircuits.map((circuit, index) => (
              <article key={circuit.id} className="home-circuit-card">
                <div className="home-card__topline">
                  <span className="home-card__index">TRK / 0{index + 1}</span>
                  <StatusBadge status={circuit.status} />
                </div>
                <h3>{circuit.title}</h3>
                <p className="home-circuit-card__location">
                  {circuit.circuitName} <span aria-hidden="true">·</span> {circuit.location}
                </p>
                <p>{circuit.description}</p>
                <div className="home-card__footer">
                  <span>{formatDate(circuit.date)}</span>
                  <span>{circuit.startTime}</span>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <div className="home-state">No hay actividades de circuito publicadas ahora mismo.</div>
        )}
      </section>

      <section className="home-section home-section--partners">
        <div className="home-section__heading">
          <div>
            <span className="home-eyebrow">La red importa</span>
            <h2>Colaboradores</h2>
            <p>Proyectos locales que comparten nuestro ritmo.</p>
          </div>
          <Link to="/collaborators" className="text-link">
            Explorar red <span aria-hidden="true">↗</span>
          </Link>
        </div>
        {collaboratorsLoading ? (
          <div className="home-state">Cargando colaboradores...</div>
        ) : featuredCollaborators.length > 0 ? (
          <div className="home-partner-grid">
            {featuredCollaborators.map((collaborator) => (
              <article key={collaborator.id} className="home-partner-card">
                <div className="home-partner-card__icon">
                  {collaborator.imageUrl ? (
                    <img src={collaborator.imageUrl} alt={`Logo de ${collaborator.name}`} />
                  ) : (
                    <span aria-hidden="true">{COLLABORATOR_TYPE_FALLBACKS[collaborator.type]}</span>
                  )}
                </div>
                <div>
                  <span className="home-partner-card__type">{COLLABORATOR_TYPE_LABELS[collaborator.type]}</span>
                  <h3>{collaborator.name}</h3>
                  <p>{isAdmin || isMember ? getBenefitLabel(collaborator) : "Ventajas reservadas para socios activos"}</p>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <div className="home-state">Aún no hay colaboradores destacados.</div>
        )}
      </section>

      <section className="home-section home-section--merch">
        <div className="home-section__heading">
          <div>
            <span className="home-eyebrow">Club supply</span>
            <h2>Merch del club</h2>
            <p>Tres piezas para llevar la identidad Guateque fuera de la ruta.</p>
          </div>
          <Link to="/merch" className="text-link">
            Ver colección <span aria-hidden="true">↗</span>
          </Link>
        </div>
        <div className="home-merch-grid">
          {merchProducts.map((product) => (
            <article key={product.id} className="home-merch-card">
              <div className="home-merch-card__visual">
                <img src={product.image} alt="" />
              </div>
              <div>
                <span>Catálogo histórico · Sin ventas</span>
                <h3>{product.name}</h3>
              </div>
            </article>
          ))}
        </div>
      </section>

      {homeError && (
        <p className="home-error" role="status">{homeError}</p>
      )}

      <section className="home-cta">
        <span className="home-eyebrow">La carretera empieza aquí</span>
        <h2>¿Nos vemos en la próxima?</h2>
        <p>Regístrate, elige una salida y encuentra tu próximo grupo.</p>
        <Link to="/register" className="btn btn--primary">
          Entrar al club
        </Link>
      </section>

    </div>
  )
}
