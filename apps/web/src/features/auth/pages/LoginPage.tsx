import { useLocation, useNavigate } from 'react-router-dom'
import LoginForm from '../components/LoginForm/LoginForm'
import type { User } from '../types/auth.types'

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const state = location.state as { from?: string } | null

  const handleSuccess = (user: User) => {
    navigate(state?.from ?? (user.role === 'COLLABORATOR' ? '/verify-member' : '/'), { replace: true })
  }

  return (
    <section className="auth-page">
      <LoginForm onSuccess={handleSuccess} />
    </section>
  )
}
