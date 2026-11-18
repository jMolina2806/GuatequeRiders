import { Link } from 'react-router-dom'
import ProfileHeader from '../components/ProfileHeader'
import ProfileStats from '../components/ProfileStats'
import MyRoutes from '../components/MyRoutes'
import MyEvents from '../components/MyEvents'
import MyOrders from '../components/MyOrders'
import NotificationPreferences from '../components/NotificationPreferences'
import { useProfile } from '../hooks/useProfile'
import { useAuth } from '@/features/auth/context/useAuth'
import './ProfilePage.css'

export default function ProfilePage() {
  const { user, isAuthenticated, isMember, isLoading: isAuthLoading } = useAuth()
  const { routes, events, stats, isLoading, error, retry } = useProfile()

  if (isAuthLoading) {
    return (
      <section className="profile-page">
        <div className="profile-page__loading" role="status" aria-live="polite">
          <div className="profile-page__spinner" />
          <p>Cargando perfil...</p>
        </div>
      </section>
    )
  }

  if (!isAuthenticated || !user) {
    return (
      <section className="profile-page">
        <div className="profile-page__empty">
          <h1>Perfil</h1>
          <p>Inicia sesión para ver tu perfil.</p>
          <Link to="/login" className="profile-page__login-link">
            Iniciar sesión
          </Link>
        </div>
      </section>
    )
  }

  return (
    <section className="profile-page">
      <ProfileHeader user={user} />

      {!isMember && (
        <div className="profile-page__membership-pending">
          <strong>Miembro registrado</strong>
          <p>Tu cuenta está creada. El carnet de socio y los descuentos se activarán cuando un administrador confirme que formas parte del grupo de WhatsApp de GuatequeRiders.</p>
        </div>
      )}

      {isMember && (
        <div className="profile-page__member-card">
          <div>
            <strong>Carnet digital de socio</strong>
            <span>{user.memberNumber}</span>
          </div>
          <Link to="/member-card" className="btn btn--ghost">
            Abrir carnet
          </Link>
        </div>
      )}

      {error && (
        <div className="profile-page__error" role="alert">
          <span>{error}</span>
          <button className="btn btn--outline-danger btn--sm" onClick={retry} type="button">
            Reintentar
          </button>
        </div>
      )}

      {isLoading ? (
        <div className="profile-page__loading" role="status" aria-live="polite">
          <div className="profile-page__spinner" />
          <p>Cargando tus actividades...</p>
        </div>
      ) : error ? (
        <div className="profile-page__empty">
          <p>No se pudieron cargar tus actividades.</p>
        </div>
      ) : (
        <>
          <ProfileStats stats={stats} />
          <MyRoutes routes={routes} />
          <MyEvents events={events} />
          <MyOrders />
          <NotificationPreferences />
        </>
      )}
    </section>
  )
}
