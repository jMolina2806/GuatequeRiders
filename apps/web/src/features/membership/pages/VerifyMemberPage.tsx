import { useState, type FormEvent } from 'react'
import { verifyMemberCode, type MembershipVerification } from '../services/membership.service'
import './VerifyMemberPage.css'

export default function VerifyMemberPage() {
  const [memberNumber, setMemberNumber] = useState('')
  const [code, setCode] = useState('')
  const [verification, setVerification] = useState<MembershipVerification | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  const handleVerify = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsLoading(true)
    setError(null)
    setVerification(null)

    try {
      const result = await verifyMemberCode(memberNumber.trim().toUpperCase(), code.trim())
      setVerification(result)
    } catch {
      setError('No se ha podido verificar el carnet. Comprueba los datos y pide al socio que abra su carnet actualizado.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <section className="verify-member">
      <div className="verify-member__panel">
        <p className="verify-member__eyebrow">Guateque Riders</p>
        <h1>Verificar carnet de socio</h1>
        <p className="verify-member__notice">
          Para comprobar un carnet, introduce el número de socio y las seis cifras
          que aparecen ahora mismo en su pantalla. No es necesario escanear un QR.
        </p>

        <form className="verify-member__form" onSubmit={handleVerify}>
          <label htmlFor="verify-member-number">Número de socio</label>
          <input
            id="verify-member-number"
            type="text"
            autoComplete="off"
            maxLength={32}
            value={memberNumber}
            placeholder="GR-0001"
            required
            onChange={(event) => setMemberNumber(event.target.value.toUpperCase())}
          />
          <label htmlFor="verify-member-code">Código temporal (6 cifras)</label>
          <input
            id="verify-member-code"
            type="text"
            inputMode="numeric"
            autoComplete="one-time-code"
            pattern="[0-9]{6}"
            maxLength={6}
            value={code}
            placeholder="000000"
            required
            onChange={(event) => setCode(event.target.value.replace(/\D/g, ''))}
          />
          <button className="btn btn--primary" type="submit" disabled={isLoading}>
            {isLoading ? 'Comprobando...' : 'Verificar socio'}
          </button>
        </form>

        {error && <p className="verify-member__error" role="alert">{error}</p>}

        {verification && (
          <div className="verify-member__result" role="status">
            <div className="verify-member__icon">✓</div>
            <h2>Socio activo</h2>
            <dl className="verify-member__details">
              <div><dt>Usuario</dt><dd>{verification.username}</dd></div>
              <div><dt>N.º de socio</dt><dd>{verification.memberNumber}</dd></div>
            </dl>
            <p>La comprobación ha sido válida. Confirma que coincide con el carnet abierto del socio.</p>
          </div>
        )}
      </div>
    </section>
  )
}
