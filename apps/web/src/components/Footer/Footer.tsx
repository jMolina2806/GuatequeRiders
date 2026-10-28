import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '@/features/auth/context/useAuth'
import { getCommunityLinks } from '@/features/community/services/community.service'
import './Footer.css'

const INSTAGRAM_URL = 'https://www.instagram.com/guateque_riders/'

export default function Footer() {
  const { isAuthenticated } = useAuth()
  const [whatsappGroupUrl, setWhatsappGroupUrl] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    if (!isAuthenticated) {
      setWhatsappGroupUrl(null)
      return
    }

    getCommunityLinks()
      .then((links) => {
        if (!cancelled) {
          setWhatsappGroupUrl(links.whatsappGroupUrl)
        }
      })
      .catch(() => {
        if (!cancelled) {
          setWhatsappGroupUrl(null)
        }
      })

    return () => {
      cancelled = true
    }
  }, [isAuthenticated])

  return (
    <footer className="site-footer">
      <div className="site-footer__inner">
        <div className="site-footer__brand">
          <strong>Guateque Riders</strong>
          <span>Rutas, eventos y comunidad motera.</span>
        </div>

        <div className="site-footer__community">
          <span className="site-footer__heading">Comunidad</span>
          <a href={INSTAGRAM_URL} target="_blank" rel="noopener noreferrer">
            Instagram
          </a>
          {whatsappGroupUrl ? (
            <a href={whatsappGroupUrl} target="_blank" rel="noopener noreferrer">
              WhatsApp miembros
            </a>
          ) : isAuthenticated ? (
            <span className="site-footer__unavailable">WhatsApp no configurado</span>
          ) : (
            <Link to="/login">WhatsApp miembros · inicia sesión</Link>
          )}
        </div>

        <div className="site-footer__legal">
          <span className="site-footer__heading">Información</span>
          <Link to="/legal">Aviso legal</Link>
          <Link to="/privacy">Privacidad</Link>
          <Link to="/merch-terms">Condiciones Merch</Link>
          <Link to="/cookies">Cookies y almacenamiento</Link>
        </div>
      </div>

      <div className="site-footer__bottom">
        <span>© {new Date().getFullYear()} Guateque Riders</span>
        <span>Hecho para la comunidad.</span>
      </div>
    </footer>
  )
}
