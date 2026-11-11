import { useEffect, useState } from 'react'
import { verifyMembership, type MembershipVerification } from '../services/membership.service'
import './VerifyMemberPage.css'

export default function VerifyMemberPage() {
  const [verification, setVerification] = useState<MembershipVerification | null>(null)
  const [invalid, setInvalid] = useState(false)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const token = decodeURIComponent(window.location.hash.replace(/^#/, ''))
    window.history.replaceState(null, '', window.location.pathname)

    if (!token) {
      setInvalid(true)
      setIsLoading(false)
      return
    }

    verifyMembership(token)
      .then((result) => {
        setVerification(result)
        setInvalid(!result.valid)
      })
      .catch(() => {
        setInvalid(true)
      })
      .finally(() => {
        setIsLoading(false)
      })
  }, [])

  if (isLoading) {
    return (
      <section className="verify-member">
        <div className="verify-member__panel">Verificando carnet...</div>
      </section>
    )
  }

  if (invalid || !verification) {
    return (
      <section className="verify-member">
        <div className="verify-member__panel verify-member__panel--invalid">
          <div className="verify-member__icon">×</div>
          <h1>Carnet no válido</h1>
          <p>El código ha caducado, no existe o el socio ya no está activo.</p>
          <p>Pide al socio que abra de nuevo su carnet digital y escanea el QR actual.</p>
        </div>
      </section>
    )
  }

  return (
    <section className="verify-member">
      <div className="verify-member__panel verify-member__panel--valid">
        <div className="verify-member__icon">✓</div>
        <p className="verify-member__eyebrow">Guateque Riders</p>
        <h1>Socio activo</h1>

        <dl className="verify-member__details">
          <div>
            <dt>Usuario</dt>
            <dd>{verification.username}</dd>
          </div>
          <div>
            <dt>N.º de socio</dt>
            <dd>{verification.memberNumber}</dd>
          </div>
        </dl>

        <div className="verify-member__code-wrap">
          <span>Código que debe coincidir con el móvil del socio</span>
          <strong>{verification.verificationCode}</strong>
        </div>

        <p className="verify-member__notice">
          Esta comprobación es temporal y no muestra DNI, email ni otros datos privados.
        </p>
      </div>
    </section>
  )
}
