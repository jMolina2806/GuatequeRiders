import { useState } from 'react'
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
    memberNumberUpdatingId,
    founderUpdatingId,
    feedback,
    retry,
    changeMembership,
    changeMemberNumber,
    changeFounder,
    clearFeedback,
  } = useAdminUsers()
  const { user, refreshUser, logout } = useAuth()
  const [draftNumbers, setDraftNumbers] = useState<Record<number, string>>({})

  const updateMembership = async (target: User, active: boolean) => {
    clearFeedback()

    const prompt = active
      ? `¿Has comprobado que ${target.username} está en el grupo de WhatsApp? Confirma para activar su carnet.`
      : `¿Has comprobado que ${target.username} ha salido del grupo de WhatsApp? Confirma para desactivar el carnet.`
    if (!window.confirm(prompt)) return

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

  const saveMemberNumber = async (target: User) => {
    const requested = (draftNumbers[target.id] ?? target.memberNumber ?? '').trim()
    if (!requested) return
    clearFeedback()

    try {
      const updated = await changeMemberNumber(target.id, requested)
      setDraftNumbers((current) => {
        const next = { ...current }
        delete next[target.id]
        return next
      })

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

  const updateFounder = async (target: User) => {
    clearFeedback()
    const action = target.founder ? 'quitar la condición de fundador' : 'marcar como fundador'
    if (!window.confirm(`¿Quieres ${action} a ${target.username}?`)) return

    try {
      const updated = await changeFounder(target.id, !target.founder)
      if (!updated.founder) {
        setDraftNumbers((current) => {
          const next = { ...current }
          delete next[target.id]
          return next
        })
      }

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
          Registrarse crea una cuenta de miembro, no un socio. Confirma manualmente
          que una persona está en el grupo de WhatsApp antes de activar su carnet.
          Si sale del grupo, desactiva su condición de socio. El número se asigna
          automáticamente al activarlo; solo los fundadores pueden personalizarlo.
          Los administradores mantienen su propio carnet.
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
                <th scope="col">Fundador</th>
                <th scope="col">Email</th>
                <th scope="col">Teléfono</th>
                <th scope="col">Estado</th>
                <th scope="col">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {members.map((member) => {
                const updating = membershipUpdatingId === member.id
                const updatingNumber = memberNumberUpdatingId === member.id
                const updatingFounder = founderUpdatingId === member.id
                const numberValue = draftNumbers[member.id] ?? member.memberNumber ?? ''
                const hasNumberChange = numberValue.trim().toUpperCase() !== (member.memberNumber ?? '')
                const isAdmin = member.role === 'ADMIN'

                return (
                  <tr key={member.id}>
                    <td className="user-table__username" data-label="Socio">
                      {member.username}
                    </td>
                    <td data-label="N.º socio">
                      {member.founder ? (
                        <div className="admin-members__number-editor">
                          <input
                            type="text"
                            aria-label={`Número de socio de ${member.username}`}
                            value={numberValue}
                            maxLength={32}
                            placeholder="GR-0001"
                            autoCapitalize="characters"
                            spellCheck={false}
                            disabled={updatingNumber || updatingFounder}
                            onChange={(event) =>
                              setDraftNumbers((current) => ({
                                ...current,
                                [member.id]: event.target.value.toUpperCase(),
                              }))
                            }
                          />
                          <button
                            className="btn btn--ghost btn--sm"
                            type="button"
                            disabled={!hasNumberChange || !numberValue.trim() || updatingNumber || updatingFounder}
                            onClick={() => void saveMemberNumber(member)}
                          >
                            {updatingNumber ? 'Guardando...' : 'Guardar número'}
                          </button>
                        </div>
                      ) : (
                        <span className="admin-members__automatic-number">
                          {member.memberNumber ?? 'Pendiente de asignación'}
                        </span>
                      )}
                    </td>
                    <td data-label="Fundador">
                      <button
                        className="btn btn--ghost btn--sm"
                        type="button"
                        disabled={updatingFounder || updatingNumber}
                        onClick={() => void updateFounder(member)}
                      >
                        {updatingFounder
                          ? 'Actualizando...'
                          : member.founder
                            ? 'Quitar fundador'
                            : 'Marcar fundador'}
                      </button>
                    </td>
                    <td data-label="Email">{member.email}</td>
                    <td data-label="Teléfono">{member.phone ?? '—'}</td>
                    <td data-label="Estado">
                      {member.memberActive ? 'Socio activo' : 'Miembro registrado'}
                      {isAdmin && <span className="admin-members__admin-label"> · Admin</span>}
                    </td>
                    <td className="user-table__actions" data-label="Acciones">
                      <button
                        className="btn btn--ghost btn--sm"
                        type="button"
                        disabled={updating || isAdmin}
                        title={isAdmin ? 'Los administradores mantienen su carnet de socio' : undefined}
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
