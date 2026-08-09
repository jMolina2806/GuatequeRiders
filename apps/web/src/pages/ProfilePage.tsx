import { Link } from 'react-router-dom'
import { useAuth } from '@/features/auth/context/useAuth'

export default function ProfilePage() {
  const { user, isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <section>
        <h1>Perfil</h1>
        <p>Cargando...</p>
      </section>
    )
  }

  if (!isAuthenticated || !user) {
    return (
      <section>
        <h1>Perfil</h1>
        <p>Inicia sesión para ver tu perfil.</p>
        <Link to="/login">Iniciar sesión</Link>
      </section>
    )
  }

  return (
    <section>
      <h1>Perfil</h1>
      <p>Usuario: {user.username}</p>
      <p>Email: {user.email}</p>
      <p>Rol: {user.role === 'ADMIN' ? 'Administrador' : 'Usuario'}</p>
    </section>
  )
}
