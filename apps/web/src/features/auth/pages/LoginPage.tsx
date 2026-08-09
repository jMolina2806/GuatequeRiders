import { useLocation, useNavigate } from 'react-router-dom'
import LoginForm from '../components/LoginForm/LoginForm'

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const state = location.state as { from?: string } | null
  const from = state?.from ?? '/'

  const handleSuccess = () => {
    navigate(from, { replace: true })
  }

  return (
    <section className="auth-page">
      <LoginForm onSuccess={handleSuccess} />
    </section>
  )
}
