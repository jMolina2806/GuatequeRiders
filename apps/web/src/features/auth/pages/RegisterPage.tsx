import { useNavigate } from 'react-router-dom'
import RegisterForm from '../components/RegisterForm/RegisterForm'

export default function RegisterPage() {
  const navigate = useNavigate()

  const handleSuccess = () => {
    navigate('/', { replace: true })
  }

  return (
    <section className="auth-page">
      <RegisterForm onSuccess={handleSuccess} />
    </section>
  )
}
