import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import UserTable from '../components/UserTable'
import RoleChangeConfirm from '../components/RoleChangeConfirm'
import { useAdminUsers } from '../hooks/useAdminUsers'
import { useAuth } from '@/features/auth/context/useAuth'
import type { User, UserRole } from '@/features/auth/types/auth.types'
import './AdminUsersPage.css'

export default function AdminUsersPage() {
  const {
    users,
    isLoading,
    error,
    roleUpdatingId,
    membershipUpdatingId,
    feedback,
    retry,
    changeRole,
    changeMembership,
    clearFeedback,
  } = useAdminUsers()
  const { user, refreshUser, logout } = useAuth()
  const navigate = useNavigate()
  const [pendingUser, setPendingUser] = useState<User | null>(null)
  const [pendingRole, setPendingRole] = useState<UserRole | null>(null)

  const handleToggleMembership = async (target: User) => {
    clearFeedback()
    const prompt = target.memberActive
      ? `¿Has comprobado que ${target.username} ha salido del grupo de WhatsApp y quieres quitarle el estado de socio?`
      : `¿Has comprobado que ${target.username} pertenece al grupo de WhatsApp? Confirma para activar su carnet de socio.`
    if (!window.confirm(prompt)) return

    try {
      const updated = await changeMembership(target.id, !target.memberActive)
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

  const handleChangeRole = (target: User, role: UserRole) => {
    clearFeedback()
    setPendingUser(target)
    setPendingRole(role)
  }

  const handleConfirmRoleChange = async () => {
    if (!pendingUser || !pendingRole) return
    const targetRole = pendingRole
    try {
      const updated = await changeRole(pendingUser.id, targetRole)
      setPendingUser(null)
      setPendingRole(null)
      if (updated.id === user?.id) {
        try {
          await refreshUser()
        } catch {
          logout()
        }
        if (updated.role !== 'ADMIN') {
          navigate(updated.role === 'COLLABORATOR' ? '/verify-member' : '/')
        }
      }
    } catch {
      return
    }
  }

  const handleCancelRoleChange = () => {
    if (roleUpdatingId !== null) return
    setPendingUser(null)
    setPendingRole(null)
  }

  return (
    <section className="admin-users">
      <header className="admin-users__header">
        <h1>Usuarios</h1>
        <p>Verifica WhatsApp antes de activar el carnet de socio. Para dar acceso a una tienda, comprueba que la cuenta pertenece a ese negocio y pulsa «Hacer colaborador»: solo podrá verificar carnets.</p>
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
          <div className="admin-users__spinner" />
          <p>Cargando usuarios...</p>
        </div>
      ) : users.length === 0 ? (
        <div className="admin-users__empty">
          <p>{error ? 'No se pudieron cargar los usuarios.' : 'No hay usuarios registrados.'}</p>
        </div>
      ) : (
        <UserTable
          users={users}
          roleUpdatingId={roleUpdatingId}
          membershipUpdatingId={membershipUpdatingId}
          onChangeRole={handleChangeRole}
          onToggleMembership={handleToggleMembership}
        />
      )}

      <RoleChangeConfirm
        user={pendingUser}
        targetRole={pendingRole}
        isUpdating={roleUpdatingId !== null}
        onConfirm={handleConfirmRoleChange}
        onCancel={handleCancelRoleChange}
      />
    </section>
  )
}
