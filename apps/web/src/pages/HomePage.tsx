import { Link } from 'react-router-dom'
import type { Route } from '@/features/routes/types/route.types'
import logoSrc from '@/assets/branding/logo-neon.jpeg'
import './HomePage.css'

const previewRoutes: Route[] = [
  {
    id: 1,
    title: 'Ruta del Valle',
    description: 'Recorrido escénico por la sierra madrileña con paradas en miradores naturales.',
    date: '2026-06-03',
    origin: 'Madrid',
    destination: 'Puerto de Navacerrada',
    distanceKm: 45,
    difficulty: 'EASY',
    maxParticipants: 20,
    currentParticipants: 5,
    status: 'PUBLISHED',
    organizers: [],
  },
  {
    id: 2,
    title: 'Ascenso a la Montaña',
    description: 'Desafío de montaña con curvas cerradas y paisajes impresionantes.',
    date: '2026-06-10',
    origin: 'Madrid',
    destination: 'Puerto de Cotos',
    distanceKm: 120,
    difficulty: 'HARD',
    maxParticipants: 15,
    currentParticipants: 12,
    status: 'PUBLISHED',
    organizers: [],
  },
]

const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Abierta',
  IN_PROGRESS: 'En curso',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
}

const DIFFICULTY_LABELS: Record<string, string> = {
  EASY: 'Fácil',
  MODERATE: 'Media',
  HARD: 'Difícil',
  EXTREME: 'Extrema',
}

const stats = [
  { value: '150+', label: 'Riders', icon: '🏍️' },
  { value: '24', label: 'Rutas realizadas', icon: '🛣️' },
  { value: '3.200+', label: 'Kilómetros', icon: '📏' },
  { value: '12', label: 'Eventos', icon: '🎉' },
]

const features = [
  {
    title: 'Rutas organizadas',
    desc: 'Salidas en grupo por carretera y montaña para todos los niveles.',
    icon: '🛵',
  },
  {
    title: 'Comunidad activa',
    desc: 'Riders de Madrid y de toda la Comunidad de Madrid. Quedadas, eventos y mucha ruta.',
    icon: '🤝',
  },
  {
    title: 'Equipación exclusiva',
    desc: 'Merchandising oficial del club. Camisetas, pegatinas y más.',
    icon: '🧢',
  },
]

const previewEvents = [
  {
    title: 'Quedada nocturna',
    date: 'Sábado 14 Junio',
    desc: 'Ruta nocturna por la sierra madrileña con parada para cenar.',
    icon: '🌙',
  },
  {
    title: 'Desayuno rider',
    date: 'Domingo 22 Junio',
    desc: 'Quedada matutina con desayuno en ruta.',
    icon: '☕',
  },
]

const merchItems = [
  { name: 'Camiseta', desc: 'Logo oficial Guateque Riders', icon: '👕' },
  { name: 'Pegatinas', desc: 'Para tu casco o baúl', icon: '🔖' },
  { name: 'Buff', desc: 'Multiusos con diseño exclusivo', icon: '🧣' },
]

export default function HomePage() {
  const upcomingRoutes = previewRoutes

  return (
    <>
      {/* HERO */}
      <section className="hero">
        <img src={logoSrc} alt="" className="hero-bg-logo" aria-hidden="true" />
        <div className="hero-glow" />
        <div className="hero-glow hero-glow--right" />
        <div className="hero-inner">
          <h1 className="hero-logo">Guateque Riders</h1>
          <p className="hero-tagline">La comunidad rider de Madrid</p>
          <p className="hero-sub">
            Rutas en grupo, eventos, y la mejor vibra sobre dos ruedas.
          </p>
          <Link to="/routes" className="hero-cta">
            Ver rutas
          </Link>
        </div>
      </section>

      {/* STATS */}
      <section className="section">
        <div className="section-inner">
          <div className="stats-grid">
            {stats.map((stat) => (
              <article key={stat.label} className="stat-card">
                <span className="stat-icon">{stat.icon}</span>
                <span className="stat-value">{stat.value}</span>
                <span className="stat-label">{stat.label}</span>
              </article>
            ))}
          </div>
        </div>
      </section>

      {/* PRÓXIMAS RUTAS */}
      <section className="section section--dark">
        <div className="section-inner">
          <h2 className="section-title">Próximas rutas</h2>
          <p className="section-desc">Salidas programadas para los próximos días.</p>
          <div className="preview-grid">
            {upcomingRoutes.map((route) => (
              <article key={route.id} className="preview-card">
                <div className="preview-card__top">
                  <h3 className="preview-card__title">{route.title}</h3>
                  <span className="preview-card__status preview-card__status--open">
                    {STATUS_LABELS[route.status] ?? route.status}
                  </span>
                </div>
                <p className="preview-card__meta">
                  📅 {route.date}
                </p>
                <p className="preview-card__meta">
                  📍 {route.origin} → {route.destination}
                </p>
                <p className="preview-card__desc">{route.description}</p>
                <div className="preview-card__footer">
                  <span className="preview-card__difficulty">{DIFFICULTY_LABELS[route.difficulty]}</span>
                  <span className="preview-card__dist">{route.distanceKm} km</span>
                </div>
              </article>
            ))}
          </div>
        </div>
      </section>

      {/* COMUNIDAD */}
      <section className="section">
        <div className="section-inner">
          <h2 className="section-title">Somos Guateque Riders</h2>
          <p className="section-desc">
            Un grupo de apasionados de las motos que disfrutamos rodando juntos.
          </p>
          <div className="preview-grid">
            {features.map((feature) => (
              <article key={feature.title} className="feature-card">
                <span className="feature-card__icon">{feature.icon}</span>
                <h3 className="feature-card__title">{feature.title}</h3>
                <p className="feature-card__desc">{feature.desc}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      {/* EVENTOS */}
      <section className="section section--dark">
        <div className="section-inner">
          <h2 className="section-title">Eventos</h2>
          <p className="section-desc">No te pierdas las próximas quedadas.</p>
          <div className="preview-grid">
            {previewEvents.map((event) => (
              <article key={event.title} className="preview-card">
                <div className="preview-card__top">
                  <h3 className="preview-card__title">
                    <span>{event.icon}</span> {event.title}
                  </h3>
                </div>
                <p className="preview-card__meta">{event.date}</p>
                <p className="preview-card__desc">{event.desc}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      {/* MERCH */}
      <section className="section">
        <div className="section-inner">
          <h2 className="section-title">Merchandising</h2>
          <p className="section-desc">Lleva Guateque Riders a todas partes.</p>
          <div className="preview-grid">
            {merchItems.map((item) => (
              <article key={item.name} className="feature-card">
                <span className="feature-card__icon">{item.icon}</span>
                <h3 className="feature-card__title">{item.name}</h3>
                <p className="feature-card__desc">{item.desc}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      {/* CTA FINAL */}
      <section className="cta-section">
        <div className="section-inner">
          <h2 className="cta-title">Únete a la comunidad</h2>
          <p className="cta-desc">
            La carretera es mejor cuando se comparte.
          </p>
          <Link to="/routes" className="hero-cta">
            Empezar ahora
          </Link>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="footer">
        <div className="section-inner">
          <div className="footer-inner">
            <div className="footer-brand">
              <span className="footer-logo">Guateque Riders</span>
              <p className="footer-desc">
                Comunidad rider de Madrid. Rodamos juntos.
              </p>
            </div>
            <div className="footer-social">
              <span className="footer-social__label">Síguenos</span>
              <div className="footer-social__links">
                <a href="#" aria-label="Instagram" className="footer-social__link">
                  📷
                </a>
                <a href="#" aria-label="WhatsApp" className="footer-social__link">
                  💬
                </a>
                <a href="#" aria-label="Telegram" className="footer-social__link">
                  ✈️
                </a>
              </div>
            </div>
          </div>
          <p className="footer-copy">
            &copy; {new Date().getFullYear()} Guateque Riders. Hecho con ❤️ sobre dos ruedas.
          </p>
        </div>
      </footer>
    </>
  )
}
