import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/useAuth'
import '../auth-form.css'

type RegisterFormProps = {
  onSuccess?: () => void
}

export default function RegisterForm({ onSuccess }: RegisterFormProps) {
  const { register } = useAuth()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [privacyAccepted, setPrivacyAccepted] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)
    setIsSubmitting(true)
    try {
      await register({ username, email, phone: phone.trim().replace(/[ -]/g, ''), password, privacyAccepted })
      onSuccess?.()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al registrarse')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="auth-form">
      <h1 className="auth-form__title">Crear cuenta</h1>
      <p className="auth-form__subtitle">Únete al club y apúntate a las rutas.</p>

      {error && <div className="auth-form__error" role="alert">{error}</div>}

      <form className="auth-form__fields" onSubmit={handleSubmit}>
        <div className="auth-form__field">
          <label htmlFor="register-username">Usuario</label>
          <input
            id="register-username"
            name="username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            autoComplete="username"
            minLength={3}
            required
          />
        </div>

        <div className="auth-form__field">
          <label htmlFor="register-email">Email</label>
          <input
            id="register-email"
            name="email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="email"
            required
          />
        </div>

        <div className="auth-form__field">
          <label htmlFor="register-phone">Teléfono</label>
          <span className="auth-form__hint">Ejemplo: 600111222 o +34600111222.</span>
          <input
            id="register-phone"
            name="phone"
            type="tel"
            value={phone}
            onChange={(event) => setPhone(event.target.value)}
            autoComplete="tel"
            inputMode="tel"
            placeholder="600111222"
            pattern="(?:[6789][0-9]{8}|\\+[1-9][0-9]{7,14})"
            maxLength={16}
            required
          />
        </div>

        <div className="auth-form__field">
          <label htmlFor="register-password">Contraseña</label>
          <span className="auth-form__hint">Mínimo 10 caracteres.</span>
          <input
            id="register-password"
            name="password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="new-password"
            minLength={10}
            maxLength={72}
            required
          />
        </div>

        <label className="auth-form__consent">
          <input
            type="checkbox"
            checked={privacyAccepted}
            onChange={(event) => setPrivacyAccepted(event.target.checked)}
            required
          />
          <span>
            He leído la <Link to="/privacy" target="_blank" rel="noopener noreferrer">Política de privacidad</Link> y entiendo cómo se tratarán mis datos.
          </span>
        </label>

        <button className="btn btn--primary btn--lg" type="submit" disabled={isSubmitting || !privacyAccepted}>
          {isSubmitting ? 'Creando cuenta...' : 'Crear cuenta'}
        </button>
      </form>

      <p className="auth-form__switch">
        ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
      </p>
    </div>
  )
}
