import { useAuth } from '@/features/auth/context/useAuth'
import type { User } from '@/features/auth/types/auth.types'
import { useAdminUsers } from '../hooks/useAdminUsers'
import './AdminUsersPage.css'
import '../components/UserTable/UserTable.css'

export default function AdminMembersPage() {
  const {
    users,
    isLoading,
    error,
    membershipUpdatingId,
    feedback,
    retry,
    changeMembership,
    clearFeedback,
  } = useAdminUsers()
  const { user, refreshUser, logout } = useAuth()

  const updateMembership = async (target: User, active: boolean) => {
    clearFeedback()

    const action = active ? 'activar como socio' : 'quitar como socio'
    if (!window.confirm(`¿Quieres ${action} a ${target.username}?`)) return

    try {
      const updated = await changeMembership(target.id, active)

      if (updated.id === user?.id) {
        try {
          await refreshUser()
        } catch {
          logout()
        }
      }
    } catch {
      return
    }
  }

  const members = [...users].sort((a, b) => {
    if (a.memberActive !== b.memberActive) return a.memberActive ? -1 : 1
    return a.username.localeCompare(b.username, 'es')
  })

  return (
    <section className="admin-users">
      <header className="admin-users__header">
        <h1>Socios</h1>
        <p>
          Gestiona quién forma parte del club. El estado de socio no caduca y solo cambia
          cuando un administrador lo activa o lo desactiva.
        </p>
      </header>

      {feedback && (
        <div
          className={`admin-users__feedback admin-users__feedback--${feedback.type}`}
          role="status"
        >
          <span>{feedback.message}</span>
          <button
            className="admin-users__feedback-close"
            onClick={clearFeedback}
            type="button"
            aria-label="Cerrar"
          >
            ✕
          </button>
        </div>
      )}

      {error && (
        <div className="admin-users__error" role="alert">
          <span>{error}</span>
          <button className="btn btn--outline-danger btn--sm" onClick={retry} type="button">
            Reintentar
          </button>
        </div>
      )}

      {isLoading ? (
        <div className="admin-users__loading" role="status" aria-live="polite">
          <p>Cargando socios...</p>
        </div>
      ) : (
        <div className="user-table__wrapper">
          <table className="user-table">
            <thead>
              <tr>
                <th scope="col">Socio</th>
                <th scope="col">N.º socio</th>
                <th scope="col">Email</th>
                <th scope="col">Teléfono</th>
                <th scope="col">Estado</th>
                <th scope="col">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {members.map((member) => {
                const updating = membershipUpdatingId === member.id

                return (
                  <tr key={member.id}>
                    <td className="user-table__username" data-label="Socio">
                      {member.username}
                    </td>
                    <td data-label="N.º socio">{member.memberNumber ?? '—'}</td>
                    <td data-label="Email">{member.email}</td>
                    <td data-label="Teléfono">{member.phone ?? '—'}</td>
                    <td data-label="Estado">{member.memberActive ? 'Socio' : 'No socio'}</td>
                    <td className="user-table__actions" data-label="Acciones">
                      <button
                        className="btn btn--ghost btn--sm"
                        type="button"
                        disabled={updating}
                        onClick={() => updateMembership(member, !member.memberActive)}
                      >
                        {updating
                          ? 'Actualizando...'
                          : member.memberActive
                            ? 'Quitar socio'
                            : 'Hacer socio'}
                      </button>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
