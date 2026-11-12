import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import UserTable from '../components/UserTable'
import RoleChangeConfirm from '../components/RoleChangeConfirm'
import { useAdminUsers } from '../hooks/useAdminUsers'
import { useAuth } from '@/features/auth/context/useAuth'
import type { User } from '@/features/auth/types/auth.types'
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

  const handleToggleRole = (target: User) => {
    clearFeedback()
    setPendingUser(target)
  }

  const handleConfirmRoleChange = async () => {
    if (!pendingUser) return
    const targetRole = pendingUser.role === 'ADMIN' ? 'USER' : 'ADMIN'
    try {
      const updated = await changeRole(pendingUser.id, targetRole)
      setPendingUser(null)
      if (updated.id === user?.id) {
        try {
          await refreshUser()
        } catch {
          logout()
        }
        if (updated.role === 'USER') {
          navigate('/')
        }
      }
    } catch {
      return
    }
  }

  const handleCancelRoleChange = () => {
    if (roleUpdatingId !== null) return
    setPendingUser(null)
  }

  return (
    <section className="admin-users">
      <header className="admin-users__header">
        <h1>Usuarios</h1>
        <p>Los registros son miembros. Confirma primero su pertenencia al WhatsApp del grupo para activarlos como socios y darles acceso al carnet. Los fundadores se gestionan desde Socios.</p>
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
          onToggleRole={handleToggleRole}
          onToggleMembership={handleToggleMembership}
        />
      )}

      <RoleChangeConfirm
        user={pendingUser}
        isUpdating={roleUpdatingId !== null}
        onConfirm={handleConfirmRoleChange}
        onCancel={handleCancelRoleChange}
      />
    </section>
  )
}
