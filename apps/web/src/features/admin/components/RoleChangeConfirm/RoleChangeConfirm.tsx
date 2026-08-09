import Modal from '@/components/Modal'
import RoleBadge from '../RoleBadge'
import type { User, UserRole } from '@/features/auth/types/auth.types'
import './RoleChangeConfirm.css'

type RoleChangeConfirmProps = {
  user: User | null
  isUpdating: boolean
  onConfirm: () => void
  onCancel: () => void
}

function nextRole(role: UserRole): UserRole {
  return role === 'ADMIN' ? 'USER' : 'ADMIN'
}

export default function RoleChangeConfirm({
  user,
  isUpdating,
  onConfirm,
  onCancel,
}: RoleChangeConfirmProps) {
  if (!user) return null
  const targetRole = nextRole(user.role)

  return (
    <Modal isOpen={user !== null} onClose={onCancel} title="Cambiar rol">
      <div className="role-change-confirm">
        <p className="role-change-confirm__text">
          ¿Cambiar el rol de <strong>{user.username}</strong> de <RoleBadge role={user.role} /> a{' '}
          <RoleBadge role={targetRole} />?
        </p>
        <p className="role-change-confirm__hint">
          {targetRole === 'USER'
            ? 'Perderá el acceso de administrador a la plataforma.'
            : 'Obtendrá acceso de administrador a la plataforma.'}
        </p>
        <div className="role-change-confirm__actions">
          <button
            className="btn btn--ghost"
            onClick={onCancel}
            disabled={isUpdating}
            type="button"
          >
            Cancelar
          </button>
          <button
            className="btn btn--primary"
            onClick={onConfirm}
            disabled={isUpdating}
            type="button"
          >
            {isUpdating ? 'Cambiando...' : 'Confirmar'}
          </button>
        </div>
      </div>
    </Modal>
  )
}
